package com.ub.utils.ui.map

import android.app.Application
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.map.CameraPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val geocoder by lazy { Geocoder(getApplication()) }

    private val _geocodedLocation = Channel<String>(capacity = Channel.BUFFERED)
    val geocodedLocation = _geocodedLocation.receiveAsFlow()

    fun geocodeLocation(cameraPosition: CameraPosition) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude,
                    1
                ) { locations ->
                    val location = locations.firstOrNull()?.run {
                        if (subAdminArea == null) return@run ""
                        "$subAdminArea\n$thoroughfare, $subThoroughfare"
                    } ?: ""
                    _geocodedLocation.trySend(location)
                }
                return@launch
            } else {
                @Suppress("DEPRECATION")
                val locations = geocoder.getFromLocation(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude,
                    1
                )
                val location = locations?.firstOrNull()?.run {
                    if (subAdminArea == null) return@run ""
                    "$subAdminArea\n$thoroughfare, $subThoroughfare"
                } ?: ""
                _geocodedLocation.send(location)
            }
        }
    }
}