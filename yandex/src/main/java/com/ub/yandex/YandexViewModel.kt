package com.ub.yandex

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.map.CameraPosition

internal class YandexViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var apiKey: String?
        set(value) { savedStateHandle[API_KEY] = value }
        get() = savedStateHandle.get<String>(API_KEY)

    var savedCameraPosition: CameraPosition?
        set(value) { savedStateHandle[CAMERA_POSITION] = value?.let(::CameraPositionParcelable) }
        get() = savedStateHandle.get<CameraPositionParcelable>(CAMERA_POSITION)?.let(CameraPositionParcelable::toCameraPosition)

    private companion object {
        const val API_KEY = "api_key"
        const val CAMERA_POSITION = "camera_position"
    }
}