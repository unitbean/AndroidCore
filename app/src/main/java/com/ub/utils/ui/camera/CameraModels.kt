package com.ub.utils.ui.camera

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
    data object MakePhotoToInternal : CameraEvent()
    data object MakePhotoToExternal : CameraEvent()
}