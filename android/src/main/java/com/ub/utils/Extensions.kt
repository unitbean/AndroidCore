@file:Suppress("UNUSED")

package com.ub.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Property
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
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

@Deprecated("Use AndroidX Core-ktx method View.isVisible = true instead")
inline val View.visible: View
    get() = apply { visibility = View.VISIBLE }

@Deprecated("Use AndroidX Core-ktx method View.isInvisible = true instead")
inline val View.invisible: View
    get() = apply { visibility = View.INVISIBLE }

@Deprecated("Use AndroidX Core-ktx method View.isGone = true instead")
inline val View.gone: View
    get() = apply { visibility = View.GONE }

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