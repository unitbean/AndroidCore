package com.ub.utils.ui.main

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.ub.utils.NetworkSpec

@Immutable
data class MainState(
    val displayingImage: Bitmap? = null,
    val networkSpec: NetworkSpec? = null
)

@Immutable
data class MainError(
    val message: String,
    val action: String?,
    val intent: Intent?
)

@Immutable
sealed class MainEvent {
    data object ClearCache : MainEvent()
    data object PickImage : MainEvent()
    data object NextPush : MainEvent()
    data class Snackbar(val intent: Intent) : MainEvent()
}