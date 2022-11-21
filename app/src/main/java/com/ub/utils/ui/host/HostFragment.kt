package com.ub.utils.ui.host

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.ub.utils.R
import com.ub.utils.databinding.FragmentHostBinding
import com.ub.utils.ui.biometric.BiometricFragment
import com.ub.utils.ui.main.MainFragment

class HostFragment : Fragment(R.layout.fragment_host) {

    private var binding: FragmentHostBinding? = null
    private var selectedFragment: Fragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHostBinding.bind(view)

        binding?.bottomNavigation?.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_main -> switchToFragment(
                    index = 1,
                    fragment = MainFragment()
                )
                R.id.menu_biometry -> switchToFragment(
                    index = 2,
                    fragment = BiometricFragment()
                )
            }

            true
        }

        val fragment = childFragmentManager.findFragmentById(R.id.fragment_container)
        val firstLaunch = fragment?.childFragmentManager?.fragments?.isEmpty() != false
        if (firstLaunch) {
            binding?.bottomNavigation?.selectedItemId = R.id.menu_main
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun switchToFragment(index: Int, fragment: Fragment) {
        val newFragment = obtainNavHostFragment(
            childFragmentManager,
            "navigation#$index",
            fragment,
            R.id.fragment_container,
            false,
            arguments
        )
        val transaction = childFragmentManager.beginTransaction()
        with(transaction) {
            selectedFragment?.let { fragment ->
                detach(fragment)
            }
            attach(newFragment)
            commitNow()
        }
        selectedFragment = newFragment
    }

    private fun obtainNavHostFragment(
        fragmentManager: FragmentManager,
        fragmentTag: String,
        fragmentInstance: Fragment,
        @IdRes containerId: Int,
        isRecreate: Boolean,
        arguments: Bundle?
    ): Fragment {
        val existingFragment =
            fragmentManager.findFragmentByTag(fragmentTag)
        if (!isRecreate) {
            existingFragment?.let { return it }
        }
        val rootFragment = fragmentInstance.apply {
            this.arguments = arguments
        }
        fragmentManager.beginTransaction()
            .apply {
                if (isRecreate && existingFragment != null) {
                    remove(existingFragment)
                }
            }
            .add(containerId, rootFragment, fragmentTag)
            .commitNow()
        return rootFragment
    }
}