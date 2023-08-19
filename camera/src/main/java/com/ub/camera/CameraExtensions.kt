package com.ub.camera

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.FocusMeteringAction
import androidx.camera.view.PreviewView

/**
 * Setup useful features for camera session
 *
 * @receiver viewfinder presentation
 * @param session camera session to handle commands in internal state
 * @param pinchToZoomEnabled is enabled pinch-to-zoom gesture
 * @param tapToFocusEnabled is enabled tap-to-focus gesture
 */
fun PreviewView.setupExtensions(session: CameraSession?, pinchToZoomEnabled: Boolean, tapToFocusEnabled: Boolean) {

    val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            val currentZoomRatio: Float = session?.camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1F

            val delta = detector.scaleFactor

            session?.camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)

            return true
        }
    }

    val scaleGestureDetector = ScaleGestureDetector(this.context, listener)

    @SuppressLint("ClickableViewAccessibility")
    val touchListener = View.OnTouchListener { _, event ->
        if (pinchToZoomEnabled) {
            scaleGestureDetector.onTouchEvent(event)
        }

        if (tapToFocusEnabled) {
            if (event.action != MotionEvent.ACTION_UP) {
                return@OnTouchListener true
            }

            val factory = this.meteringPointFactory

            val (x, y) = event.x to event.y

            val point = factory.createPoint(x, y)

            val action = FocusMeteringAction.Builder(point).build()

            session?.camera?.cameraControl?.startFocusAndMetering(action)
        }

        true
    }

    this.setOnTouchListener(touchListener)
}