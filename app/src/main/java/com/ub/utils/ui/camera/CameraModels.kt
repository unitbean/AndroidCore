package com.ub.utils.ui.camera

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
internal data class CameraState(
    val isLightAvailable: Boolean = false,
    val isSwitchAvailable: Boolean = false,
    val isLightIsActive: Boolean = false,
    val zoom: String = ""
)

@Immutable
internal sealed class CameraEvent {
    data object Switch : CameraEvent()
    data object Light : CameraEvent()
    data class MakePhotoToInternal(val uri: Uri) : CameraEvent()
    data class MakePhotoToExternal(val uri: Uri) : CameraEvent()
}