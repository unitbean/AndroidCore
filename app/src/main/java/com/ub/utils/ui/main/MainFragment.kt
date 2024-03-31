package com.ub.utils.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ub.utils.BaseApplication
import com.ub.utils.R
import com.ub.utils.UbNotify
import com.ub.utils.launchAndRepeatWithViewLifecycle
import com.ub.utils.provideFactory
import com.ub.utils.settingsIntent
import com.ub.utils.spannableBuilder
import com.ub.utils.ui.theme.CoreTheme
import kotlinx.coroutines.launch
import java.util.Random

class MainFragment : Fragment() {

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

    private val permissionCaller = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        when {
            isGranted -> showPush()
            !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) -> {
                viewModel.propagateError(
                    getString(R.string.permission_is_not_granted_text),
                    getString(R.string.permission_is_not_granted_action),
                    requireActivity().settingsIntent
                )
            }
        }
    }

    private val imagePickerCaller = registerForActivityResult(ActivityResultContracts.GetContent()) { image ->
        viewModel.cachePickedImage(image ?: return@registerForActivityResult)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            setContent {
                CoreTheme {
                    val state by viewModel.state.collectAsStateWithLifecycle(lifecycleOwner = viewLifecycleOwner)
                    MainScreen(
                        state = state,
                        error = viewModel.error,
                        onEvent = { event ->
                            when (event) {
                                MainEvent.NextPush -> showPush()
                                MainEvent.PickImage -> imagePickerCaller.launch("image/*")
                                MainEvent.ClearCache -> viewModel.removeCachedFiles()
                                is MainEvent.Snackbar -> startActivity(event.intent)
                            }
                        },
                        modifier = Modifier.statusBarsPadding()
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchAndRepeatWithViewLifecycle {
            launch {
                viewModel.done.collect { onDone() }
            }
            launch {
                viewModel.showPush.collect { onShowPush(it) }
            }
            launch {
                viewModel.myIp.collect { onMyIp(it) }
            }
        }
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
}