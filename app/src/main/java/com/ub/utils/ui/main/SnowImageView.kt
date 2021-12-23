package com.ub.utils.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import com.ub.utils.SnowflakesEffect

class SnowImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
): ImageView(context, attributeSet, defStyleAttr) {

    private val snowEffect: SnowflakesEffect = SnowflakesEffect(
        Color.WHITE,
        Color.WHITE,
        dpToPx(1.5F),
        dpToPx(0.5F),
        0,
        resources.displayMetrics.density
    )

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        snowEffect.onDraw(this, canvas)
    }

    private fun dpToPx(dpValue: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, this.context.resources.displayMetrics)
    }
}