@file:Suppress("UNUSED")

package com.ub.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Property
import android.util.TypedValue
import android.view.View
import androidx.activity.ComponentDialog
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*

fun Context.spToPx(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, this.resources.displayMetrics)
}

fun View.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), this.context.resources.displayMetrics)
}

fun Context.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), this.resources.displayMetrics)
}

@Deprecated("Use AndroidX Core-ktx method View.isVisible = true instead", level = DeprecationLevel.ERROR)
inline val View.visible: View
    get() = throw UnsupportedOperationException("Please remove this method. See lint hints")

@Deprecated("Use AndroidX Core-ktx method View.isInvisible = true instead", level = DeprecationLevel.ERROR)
inline val View.invisible: View
    get() = throw UnsupportedOperationException("Please remove this method. See lint hints")

@Deprecated("Use AndroidX Core-ktx method View.isGone = true instead", level = DeprecationLevel.ERROR)
inline val View.gone: View
    get() = throw UnsupportedOperationException("Please remove this method. See lint hints")

fun AlertDialog.isNotShowing(): Boolean = !isShowing

fun Disposable.isNotDisposed(): Boolean = !isDisposed

fun <T> MutableList<T>.renew(list: Collection<T>): MutableList<T> {
    clear()
    addAll(list)
    return this
}

fun <K, V> MutableMap<K, V>.renew(map: Map<K, V>): MutableMap<K, V> {
    clear()
    putAll(map)
    return this
}

fun View.delayOnLifecycle(
    durationInMillis: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: (View) -> Unit
): Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
        delay(durationInMillis)
        block.invoke(this@delayOnLifecycle)
    }
}

fun Collection<String>.containsIgnoreCase(value: String): Boolean {
    return this
        .firstOrNull()
        ?.let { (it.contains(value, ignoreCase = true)) }
        ?: false
}

fun <T : View> T.animator(property: Property<T, Float>, vararg values: Float): ObjectAnimator = ObjectAnimator.ofFloat(this, property, *values)

fun <T : View> T.animator(property: String, vararg values: Float): ObjectAnimator = ObjectAnimator.ofFloat(this, property, *values)

fun <T : View> T.animator(property: Property<T, Int>, vararg values: Int): ObjectAnimator = ObjectAnimator.ofInt(this, property, *values)

fun <T : View> T.animator(property: String, vararg values: Int): ObjectAnimator = ObjectAnimator.ofInt(this, property, *values)

fun Drawable.colorize(@ColorInt colorInt: Int) {
    colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(colorInt, BlendModeCompat.SRC_IN)
}

/**
 * Launches a new coroutine and repeats [block] every time the [Fragment]'s [Fragment.getViewLifecycleOwner]
 * is in and out of [minActiveState] lifecycle state.
 */
inline fun Fragment.launchAndRepeatWithViewLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            block()
        }
    }
}

/**
 * Launches a new coroutine and repeats [block] every time the [ComponentDialog]'s [ComponentDialog.lifecycleScope]
 * is in and out of [minActiveState] lifecycle state.
 */
inline fun ComponentDialog.launchAndRepeatWithViewLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(minActiveState) {
            block()
        }
    }
}

/**
 * @author https://blog.protein.tech/android-how-to-write-the-best-usecase-interactors-ever-59e6d6944867
 */
fun ViewModel.withUseCaseScope(
    loadingUpdater: (suspend (Boolean) -> Unit)? = null,
    onError: (suspend (Exception) -> Unit)? = null,
    onComplete: (suspend () -> Unit)? = null,
    block: (suspend () -> Unit)
): Job {
    return viewModelScope.launch {
        loadingUpdater?.invoke(true)
        try {
            block()
        } catch (e: Exception) {
            onError?.invoke(e)
        } finally {
            loadingUpdater?.invoke(false)
            onComplete?.invoke()
        }
    }
}

inline fun <T : ViewModel>provideFactory(
    crossinline customFactory: () -> T
): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return customFactory.invoke() as T
        }
    }

fun <T : ViewModel>provideSavedFactory(
    customFactory: (SavedStateHandle) -> T
): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return customFactory.invoke(extras.createSavedStateHandle()) as T
        }
    }

@Suppress("DEPRECATION")
fun <T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, clazz)
    } else {
        getParcelable(key)
    }
}