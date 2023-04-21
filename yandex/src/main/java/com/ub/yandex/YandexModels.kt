package com.ub.yandex

import android.os.Parcelable
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CameraPositionParcelable(
    val target: PointParcelable,
    val zoom: Float,
    val azimuth: Float,
    val tilt: Float
) : Parcelable {
    @Parcelize
    internal class PointParcelable(
        val latitude: Double,
        val longitude: Double
    ) : Parcelable

    constructor(cameraPosition: CameraPosition) :
        this(
            PointParcelable(cameraPosition.target.latitude, cameraPosition.target.longitude),
            cameraPosition.zoom,
            cameraPosition.azimuth,
            cameraPosition.tilt
        )

    fun toCameraPosition(): CameraPosition {
        return CameraPosition(Point(target.latitude, target.longitude), zoom, azimuth, tilt)
    }
}

/**
 * Object that represents event from [com.yandex.mapkit.map.CameraListener]
 */
data class YandexCameraEvent(
    val map: Map,
    val cameraPosition: CameraPosition,
    val cameraUpdateReason: CameraUpdateReason,
    val isFinished: Boolean
)