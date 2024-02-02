package com.ub.utils.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ub.utils.BaseApplication
import com.ub.utils.NetworkSpec
import com.ub.utils.R
import com.ub.utils.UbNotify
import com.ub.utils.VpnAware
import com.ub.utils.colorize
import com.ub.utils.databinding.FragmentMainBinding
import com.ub.utils.isDarkMode
import com.ub.utils.launchAndRepeatWithViewLifecycle
import com.ub.utils.provideFactory
import com.ub.utils.settingsIntent
import com.ub.utils.spannableBuilder
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.launch
import java.util.Random

class MainFragment : Fragment(R.layout.fragment_main), View.OnClickListener {

    private val viewModel: MainViewModel by viewModels {
        val images: Array<String> by lazy {
            arrayOf(
                "https://tagline.ru/file/company/logo/unitbean-logo_tagline.png",
                "https://kotlinlang.org/docs/images/kotlin-logo.png",
                "https://developer.android.com/static/images/brand/Android_Robot_200.png"
            )
        }
        provideFactory {
            BaseApplication.createMainComponent().mainViewModelFactory().invoke(
                images[random.nextInt(images.size)]
            )
        }
    }

    private val random = Random()
    private var binding: FragmentMainBinding? = null

    private val permissionCaller = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        when {
            isGranted -> showPush()
            !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) -> {
                binding?.mainRoot?.let { root ->
                    Snackbar
                        .make(root, R.string.permission_is_not_granted_text, Snackbar.LENGTH_LONG)
                        .setAction(R.string.permission_is_not_granted_action) {
                            val intent = root.context.settingsIntent
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private val imagePickerCaller = registerForActivityResult(ActivityResultContracts.GetContent()) { image ->
        viewModel.cachePickedImage(image ?: return@registerForActivityResult)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)

        binding?.btnTextPush?.setOnClickListener(this)
        binding?.btnPickImage?.setOnClickListener(this)
        binding?.btnClearCache?.setOnClickListener(this)

        binding?.mainRoot?.applyInsetter {
            type(statusBars = true, navigationBars = true) {
                padding(top = true)
            }
            consume(Insetter.CONSUME_ALL)
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                viewModel.done.collect { onDone() }
            }
            launch {
                viewModel.showPush.collect { onShowPush(it) }
            }
            launch {
                viewModel.connectivity.collect { onShowConnectivityChange(it) }
            }
            launch {
                viewModel.myIp.collect { onMyIp(it) }
            }
            launch {
                viewModel.image.collect { showImage(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onDone() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(requireContext().spannableBuilder {
                append("20sp text with underline and strikethrough")
                partialSpan("20sp text") {
                    size(20F)
                }
                partialSpan("underline") {
                    underline()
                }
                partialSpan("strikethrough") {
                    strikethrough()
                }
                appendLn("Bold text blue yeti")
                partialSpan("Bold text") {
                    typeface(Typeface.DEFAULT_BOLD)
                }
                partialSpan("blue yeti") {
                    color(android.R.color.holo_blue_light)
                    size(10F)
                }
                appendLn("Small text") {
                    size(7F)
                    lineHeight(7F)
                }
                appendLn("Partial clickable clickable span")
                partialSpan("clickable", searchFromIndex = 80) {
                    click(
                        isNeedUnderline = true
                    ) {
                        Toast.makeText(requireContext(), "Click on span", Toast.LENGTH_LONG).show()
                    }
                }
            })
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.myIp()
            }
            .show()
    }

    private fun onShowPush(content: Pair<String, String>) {
        UbNotify
            .create(requireContext(), android.R.drawable.ic_dialog_alert, content.first, content.second)
            .setChannelParams(content.first, content.second, null)
            .setParams {
                setAutoCancel(true)
                setStyle(NotificationCompat.BigTextStyle().bigText(content.second))
            }
            .show(id = random.nextInt())
    }

    private fun onShowConnectivityChange(spec: NetworkSpec) {
        binding?.statuses?.apply {
            removeAllViews()
            val isDarkMode = this.resources?.isDarkMode ?: false
            val connectivityIcon = when (spec) {
                NetworkSpec.Connecting -> ResourcesCompat.getDrawable(resources, R.drawable.outline_hourglass_empty_24, context?.theme)
                is NetworkSpec.Active -> ResourcesCompat.getDrawable(resources, R.drawable.baseline_signal_cellular_alt_24, context?.theme)
                NetworkSpec.Disabled -> ResourcesCompat.getDrawable(resources, R.drawable.baseline_error_outline_24, context?.theme)
                is NetworkSpec.Captive -> ResourcesCompat.getDrawable(resources, R.drawable.baseline_login_24, context?.theme)
                else -> ResourcesCompat.getDrawable(resources, R.drawable.baseline_device_unknown_24, context?.theme)
            }
            if ((spec as? VpnAware)?.isVpn == true) {
                val isVpn = ResourcesCompat.getDrawable(resources, R.drawable.ic_vector_vpn_key, context?.theme)
                val vpn = ImageView(requireContext()).apply {
                    val color = if (isDarkMode) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                    isVpn?.colorize(color)
                    setImageDrawable(isVpn)
                }
                addView(vpn)
            }
            val connectivity = ImageView(requireContext()).apply {
                val color = if (isDarkMode) {
                    Color.WHITE
                } else {
                    Color.BLACK
                }
                connectivityIcon?.colorize(color)
                setImageDrawable(connectivityIcon)
            }
            addView(connectivity)
        }
    }

    private fun onMyIp(myIp: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.app_name)
            .setMessage("My ip $myIp")
            .show()
    }

    private fun showPush() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionCaller.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.generatePushContent()
        }
    }

    private fun showImage(image: Bitmap) {
        binding?.ivImage?.setImageBitmap(image)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_text_push -> showPush()
            R.id.btn_pick_image -> imagePickerCaller.launch("image/*")
            R.id.btn_clear_cache -> viewModel.removeCachedFiles()
        }
    }
}