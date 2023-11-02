package com.ub

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.View.OnKeyListener
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import com.ub.ubutils.R

class CodeInputView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attributeSet, defStyleAttr) {

    private val inputManager = ContextCompat.getSystemService(getContext(), InputMethodManager::class.java)
    private var code: String = ""

    private var count: Int = 4
    private var textSize: Float = 15F
    private var textColor: Int = Color.BLACK
    private var selectedColor: Int = Color.GRAY
    private var backgroundColor: Int = Color.LTGRAY
    private var innerPadding: Float = 0F

    private val paint: Paint = Paint()

    private var inputListener: InputListener? = null

    private val drawingRect = RectF()

    init {
        context.obtainStyledAttributes(attributeSet, R.styleable.CodeInputView, defStyleAttr, 0).use { typedArray ->
            textSize = typedArray.getDimension(R.styleable.CodeInputView_android_textSize, spToPx(15F))
            textColor = typedArray.getColor(R.styleable.CodeInputView_android_textColor, textColor)
            selectedColor = typedArray.getColor(R.styleable.CodeInputView_cellSelectedColor, selectedColor)
            backgroundColor = typedArray.getColor(R.styleable.CodeInputView_cellBackgroundColor, backgroundColor)
            count = typedArray.getInteger(R.styleable.CodeInputView_count, count)
            innerPadding = typedArray.getDimension(R.styleable.CodeInputView_innerPadding, innerPadding)
        }

        setOnClickListener {
            inputManager?.showSoftInput(it, 0)
        }

        if (isInEditMode) {
            code = "1234"
        }

        isFocusableInTouchMode = true

        setOnKeyListener(OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    if (code.length < 4) {
                        playSoundEffect(SoundEffectConstants.CLICK)
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        code += event.unicodeChar.toChar()
                        invalidate()
                        inputListener?.onCodeChange(code)
                    }
                    return@OnKeyListener true
                } else if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (code.isNotEmpty()) {
                        playSoundEffect(SoundEffectConstants.CLICK)
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        code = code.dropLast(1)
                        invalidate()
                        inputListener?.onCodeChange(code)
                    }
                    return@OnKeyListener true
                }
            }
            false
        })
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            imeOptions = EditorInfo.IME_ACTION_DONE
            initialSelEnd = 0.also { outAttrs.initialSelStart = it }
        }
        return BaseInputConnection(this, true)
    }

    override fun onCheckIsTextEditor(): Boolean = true

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int = measureWidth(widthMeasureSpec)
        val height: Int = measureHeight(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        for (cell in 0 until count) {
            if (cell == code.length && isFocused) {
                paint.changeToSelected()
            } else {
                paint.changeToBackground()
            }
            val singleCellWidth = (width - paddingLeft - paddingRight - count.minus(1) * innerPadding) / count
            val singleCellHeight = height - paddingTop - paddingBottom
            drawingRect.set(
                0F,
                0F,
                singleCellWidth,
                singleCellHeight.toFloat()
            )
            drawingRect.offsetTo(
                cell * singleCellWidth + cell * innerPadding + paddingLeft.toFloat(),
                0F + paddingTop.toFloat()
            )
            canvas.drawRoundRect(
                drawingRect,
                dpToPx(8F),
                dpToPx(8F),
                paint
            )
            paint.changeToText()
            if (code.length >= cell.plus(1)) {
                canvas.drawText(
                    code.getOrNull(cell)?.toString() ?: continue,
                    drawingRect.left + dpToPx(4F),
                    drawingRect.bottom - dpToPx(4F),
                    paint
                )
            }
        }
    }

    fun setListener(inputListener: InputListener) {
        this.inputListener = inputListener
    }

    fun setText(newText: String?) {
        code = newText ?: ""
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        if (count == 0) {
            return 0
        }
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)
        var width: Int
        if (mode == MeasureSpec.EXACTLY) {
            width = size
        } else {
            val cellSize = paint.getTextWidth() + dpToPx(8F)
            width = paddingLeft + paddingRight + (count * cellSize).toInt() + (count.minus(1) * innerPadding).toInt()
            if (mode == MeasureSpec.AT_MOST) {
                width = width.coerceAtMost(size)
            }
        }
        return width
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        if (count == 0) {
            return 0
        }
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)
        var height: Int
        if (mode == MeasureSpec.EXACTLY) {
            height = size
        } else {
            val cellSize = textSize + dpToPx(8F)
            height = paddingTop + paddingBottom + cellSize.toInt()
            if (mode == MeasureSpec.AT_MOST) {
                height = height.coerceAtMost(size)
            }
        }
        return height
    }

    interface InputListener {

        fun onCodeFinished(code: String)

        fun onCodeChange(code: String)
    }

    private fun Paint.changeToText() {
        reset()
        isAntiAlias = true
        color = textColor
        textSize = this@CodeInputView.textSize
    }

    private fun Paint.changeToBackground() {
        reset()
        isAntiAlias = true
        color = backgroundColor
    }

    private fun Paint.changeToSelected() {
        reset()
        isAntiAlias = true
        color = selectedColor
    }

    private fun Paint.getTextHeight(): Float {
        reset()
        textSize = this@CodeInputView.textSize
        val height = fontMetrics.run {
            bottom - top + leading
        }
        reset()
        return height
    }

    private fun Paint.getTextWidth(): Float {
        reset()
        textSize = this@CodeInputView.textSize
        val width = measureText("0")
        reset()
        return width
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics)

    private fun spToPx(sp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, this.resources.displayMetrics)
}