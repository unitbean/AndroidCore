package com.ub.utils.ui.host

import android.os.Bundle
import android.view.View
import com.ub.utils.BuildConfig
import com.ub.utils.NavigationRootFragment
import com.ub.utils.R
import com.ub.utils.databinding.FragmentHostBinding
import com.ub.utils.ui.biometric.BiometricFragment
import com.ub.utils.ui.main.MainFragment
import com.ub.yandex.YandexMapFragment

class HostFragment : NavigationRootFragment(R.layout.fragment_host) {

    private var binding: FragmentHostBinding? = null

    override val fragmentContainerId: Int
        get() = R.id.fragment_container

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHostBinding.bind(view)

        binding?.bottomNavigation?.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_main -> switchToFragment(
                    tag = "navigation#1",
                    fragmentInstance = MainFragment()
                )
                R.id.menu_biometry -> switchToFragment(
                    tag = "navigation#2",
                    fragmentInstance = BiometricFragment()
                )
                R.id.menu_yandex -> switchToFragment(
                    tag = "navigation#3",
                    fragmentInstance = YandexMapFragment().apply {
                        apiKeyDelegate.setApiKey(
                            BuildConfig.YANDEX_KEY
                        )
                    }
                )
            }

            true
        }

        if (isInInitialState) {
            binding?.bottomNavigation?.selectedItemId = R.id.menu_main
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}