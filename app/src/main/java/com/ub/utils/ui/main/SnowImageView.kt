package com.ub.utils.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import com.ub.utils.SnowflakesEffect
import com.ub.utils.dpToPx
import com.ub.utils.isDarkMode

class SnowImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attributeSet, defStyleAttr) {

    private val snowEffect: SnowflakesEffect = SnowflakesEffect(
        if (context.resources.isDarkMode == true) Color.WHITE else Color.BLACK,
        if (context.resources.isDarkMode == true) Color.WHITE else Color.BLACK,
        context.dpToPx(1.5F),
        context.dpToPx(0.5F),
        0,
        resources.displayMetrics.density
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        snowEffect.onDraw(this, canvas)
    }
}