package com.ub.utils.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ub.utils.BaseApplication
import com.ub.utils.CNetwork
import com.ub.utils.R
import com.ub.utils.UbNotify
import com.ub.utils.databinding.FragmentMainBinding
import com.ub.utils.launchAndRepeatWithViewLifecycle
import com.ub.utils.provideFactory
import com.ub.utils.spannableBuilder
import kotlinx.coroutines.launch
import java.util.Random

class MainFragment : Fragment(R.layout.fragment_main), View.OnClickListener {

    private val viewModel: MainViewModel by viewModels {
        val images: Array<String> by lazy {
            arrayOf(
                "https://tagline.ru/file/company/logo/unitbean-logo_tagline.png",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Kotlin_logo_2021.svg/2880px-Kotlin_logo_2021.svg.png"
            )
        }
        provideFactory {
            BaseApplication.createMainComponent().provider.create(
                urlToLoad = images[random.nextInt(images.size)]
            )
        }
    }

    private val random = Random()
    private var binding: FragmentMainBinding? = null

    private val permissionCaller = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            showPush()
        }
    }

    private val imagePickerCaller = registerForActivityResult(ActivityResultContracts.GetContent()) { image ->
        viewModel.cachePickedImage(image ?: return@registerForActivityResult)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)

        binding?.btnTextAction?.setOnClickListener(this)
        binding?.btnTextPush?.setOnClickListener(this)
        binding?.btnPickImage?.setOnClickListener(this)
        binding?.btnClearCache?.setOnClickListener(this)

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
                viewModel.isEquals.collect { onIsEquals(it) }
            }
            launch {
                viewModel.image.collect { showImage(it) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun onDone() {
        AlertDialog.Builder(requireContext())
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
                viewModel.isEquals()
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

    private fun onShowConnectivityChange(state: String) {
        requireActivity().window.statusBarColor = when (state) {
            CNetwork.NetworkState.ESTABLISH -> Color.MAGENTA
            CNetwork.NetworkState.ACTIVE -> ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            CNetwork.NetworkState.DISABLE -> Color.RED
            CNetwork.NetworkState.CAPTIVE -> Color.GREEN
            else -> Color.YELLOW
        }
    }

    private fun onIsEquals(equals: Boolean) {
        AlertDialog.Builder(requireContext())
            .setMessage("${requireContext().getString(R.string.app_name)}. Equals $equals")
            .show()
    }

    private fun showPush() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionCaller.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.generatePushContent()
        }
    }

    private fun hideTest() {
        if (binding?.tvText?.isGone == true) {
            binding?.tvText?.isVisible = true
            binding?.btnTextAction?.setText(R.string.hide_text)
        } else {
            binding?.tvText?.isGone = true
            binding?.btnTextAction?.setText(R.string.show_text)
        }
    }

    private fun showImage(image: Bitmap) {
        binding?.ivImage?.setImageBitmap(image)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_text_action -> hideTest()
            R.id.btn_text_push -> showPush()
            R.id.btn_pick_image -> imagePickerCaller.launch("image/*")
            R.id.btn_clear_cache -> viewModel.removeCachedFiles()
        }
    }
}