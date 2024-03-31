package com.ub.utils.ui.main

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.ub.utils.NetworkSpec

@Immutable
data class MainState(
    val displayingImage: Uri? = null,
    val networkSpec: NetworkSpec? = null
)

@Immutable
sealed class MainEvent {
    data object ClearCache : MainEvent()
    data object PickImage : MainEvent()
    data object NextPush : MainEvent()
}