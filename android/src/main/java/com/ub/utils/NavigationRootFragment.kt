package com.ub.utils

import android.os.Bundle
import androidx.annotation.ContentView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * Class that includes the logic for working with bottom navigation with the inner container of fragments
 *
 * Root fragments are initialized by a lazy way
 *
 * The class that inherits from this class must implement the [fragmentContainerId] property
 *
 * Also provide the [isInInitialState] property, which can be useful for initial fulfillment on container
 */
abstract class NavigationRootFragment : Fragment {

    constructor() : super()

    @ContentView
    constructor(@LayoutRes layoutId: Int) : super(layoutId)

    abstract val fragmentContainerId: Int
    private var currentFragmentTag: String? = null

    val isInInitialState: Boolean
        get() {
            val hostFragment = childFragmentManager.findFragmentById(fragmentContainerId)
            return hostFragment?.parentFragment?.childFragmentManager?.fragments?.isEmpty() != false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentFragmentTag = savedInstanceState?.getString(FRAGMENT_TAG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FRAGMENT_TAG, currentFragmentTag)
    }

    /**
     * Switch root fragment in [fragmentContainerId]
     *
     * Intended to work with BottomNavigationView to switch [Fragment]'s in associated container
     *
     * @param fragmentInstance instance of [Fragment] to show
     * @param tag unique tag of class fragment
     * @param isRecreate is need recreate fragment on call. Otherwise already added fragment, found by [tag] will be reused
     * @param arguments optional arguments for transfer it to the [fragmentInstance]
     * @param isReorderingAllowed optimization flag for Android Framework
     */
    protected fun switchToFragment(
        fragmentInstance: Fragment,
        tag: String,
        isRecreate: Boolean = false,
        arguments: Bundle? = null,
        isReorderingAllowed: Boolean = false
    ) {
        childFragmentManager.beginTransaction().run {
            val existingFragment = childFragmentManager.findFragmentByTag(tag)
            val fragmentToShow = if (isRecreate) {
                existingFragment?.let {
                    remove(it)
                }
                fragmentInstance.apply {
                    this.arguments = arguments
                }
            } else {
                val fragmentToShow = existingFragment ?: fragmentInstance
                fragmentToShow.apply {
                    this.arguments = arguments
                }
            }
            childFragmentManager.findFragmentByTag(currentFragmentTag)?.let { fragment ->
                detach(fragment)
            }
            add(fragmentContainerId, fragmentToShow, tag)
            setReorderingAllowed(isReorderingAllowed)
            attach(fragmentToShow)
            commitNow()
        }
        currentFragmentTag = tag
    }

    private companion object {
        private const val FRAGMENT_TAG = "fragment_tag"
    }
}