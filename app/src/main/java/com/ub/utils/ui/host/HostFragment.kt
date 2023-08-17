package com.ub.utils.ui.host

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.ub.camera.CameraFragment
import com.ub.utils.NavigationRootFragment
import com.ub.utils.R
import com.ub.utils.databinding.FragmentHostBinding
import com.ub.utils.ui.biometric.BiometricFragment
import com.ub.utils.ui.main.MainFragment
import com.ub.utils.ui.map.MapFragment

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
                    fragmentInstance = MapFragment()
                )
                R.id.menu_camera -> switchToFragment(
                    tag = "navigation#4",
                    fragmentInstance = CameraFragment(),
                    arguments = bundleOf("layout_id" to R.layout.fragment_camera)
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