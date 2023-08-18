package com.ub.camera

import java.io.File
import java.io.OutputStream

sealed class CameraOutputVariant

class CameraScopedStorage(
    val filename: String,
    val relativeFolder: String
) : CameraOutputVariant()

class CameraOutputStream(
    val outputStream: OutputStream
) : CameraOutputVariant()

class CameraFile(
    val file: File
) : CameraOutputVariant()