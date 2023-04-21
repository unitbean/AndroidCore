package com.ub.yandex

import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * A suspending function that awaits for the [Map] to be loaded. Uses
 * [YandexMapFragment.setMapReady].
 */
suspend fun YandexMapFragment.awaitMap(): Map = suspendCancellableCoroutine { continuation ->
    (this.view as? MapView)?.map?.let { map ->
        continuation.resumeWith(Result.success(map))
        return@suspendCancellableCoroutine
    }
    val callback = YandexMapReadyCallback {
        continuation.resumeWith(Result.success(it))
    }
    this.setMapReady(callback)
}

/**
 * Returns a [Flow] of [YandexCameraEvent] items so that camera movements can be observed
 */
val Map.mapIdleFlow: Flow<YandexCameraEvent>
    get() = callbackFlow {
        val cameraListener = CameraListener { map, cameraPosition, updateReason, isFinished ->
            if (isFinished) {
                trySend(YandexCameraEvent(map, cameraPosition, updateReason, isFinished))
            }
        }
        addCameraListener(cameraListener)
        awaitClose {
            if (isValid) {
                removeCameraListener(cameraListener)
            }
        }
    }