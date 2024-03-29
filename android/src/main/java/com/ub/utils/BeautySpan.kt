@file:Suppress("UNUSED")

package com.ub.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextPaint
import android.text.TextUtils.concat
import android.text.style.*
import android.util.TypedValue
import android.view.View
import androidx.annotation.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.util.regex.Pattern
import kotlin.math.roundToInt

fun Context.spannableBuilder(builder: (SpannableStringCreator.() -> Unit)): SpannableString {
    return SpannableStringCreator(this).apply {
        builder.invoke(this)
    }.toSpannableString()
}

class SpannableStringCreator(private val context: Context) {
    private val parts = ArrayList<CharSequence>()
    private var length = 0
    private val spanMap: MutableMap<IntRange, Iterable<Any>> = HashMap()

    fun appendSpace(newText: CharSequence) = append(" ").append(newText)

    fun appendSpace(newText: CharSequence, spans: (ResSpans.() -> Unit)? = null) = append(" ").append(newText, spans)

    fun appendLnNotBlank(newText: CharSequence, spans: (ResSpans.() -> Unit)? = null) = applyIf({ newText.isNotBlank() }) { appendLn(newText, spans) }

    fun appendLn(newText: CharSequence, spans: (ResSpans.() -> Unit)? = null) = append("\n").append(newText, spans)

    fun append(newText: CharSequence, spans: (ResSpans.() -> Unit)? = null) = apply {
        val end = newText.length
        parts.add(newText)
        spans?.let {
            spanMap[(length..length + end)] = ResSpans(context, it).spans
        }
        length += end
    }

    fun append(newText: CharSequence) = apply {
        parts.add(newText)
        length += newText.length
    }

    inline fun applyIf(predicate: () -> Boolean, action: SpannableStringCreator.() -> SpannableStringCreator) = if (predicate()) action() else this

    fun toSpannableString() = SpannableString(concat(*parts.toTypedArray())).apply {
        spanMap.forEach { spanItem ->
            val range = spanItem.key
            spanItem.value.forEach {
                setSpan(it, range.first, range.last, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun partialSpan(patternToSpan: Pattern, searchFromIndex: Int = 0, spans: ResSpans.() -> Unit) {
        val fullString = concat(*parts.toTypedArray())
        val matcher = patternToSpan.matcher(fullString)
        val isMatchSuccess = matcher.find(searchFromIndex)
        if (isMatchSuccess) {
            val startIndexStringToSpan = matcher.start()
            val endIndexStringToSpan = matcher.end()
            spanMap[(startIndexStringToSpan..endIndexStringToSpan)] = ResSpans(context, spans).spans
        }
    }

    fun partialSpan(stringToSpan: String, searchFromIndex: Int = 0, spans: ResSpans.() -> Unit) {
        val fullString = concat(*parts.toTypedArray())
        val startIndexStringToSpan = fullString.indexOf(stringToSpan, searchFromIndex)
        val endIndexStringToSpan = startIndexStringToSpan + stringToSpan.length
        if (startIndexStringToSpan != -1 && endIndexStringToSpan <= fullString.length) {
            spanMap[(startIndexStringToSpan..endIndexStringToSpan)] = ResSpans(context, spans).spans
        }
    }
}

class SpannableAppendable(
    private val creator: SpannableStringCreator,
    vararg spanParts: Pair<Any, ResSpans.() -> Unit>) : Appendable {

    private val spansMap = spanParts.toMap().mapKeys { part -> part.key.let { it as? CharSequence ?: it.toString() } }

    override fun append(csq: CharSequence?) = apply { creator.appendSmart(csq, spansMap) }

    override fun append(csq: CharSequence?, start: Int, end: Int) = apply {
        if (csq != null) {
            if (start in 0 until end && end <= csq.length) {
                append(csq.subSequence(start, end))
            } else {
                throw IndexOutOfBoundsException("start " + start + ", end " + end + ", s.length() " + csq.length)
            }
        }
    }

    override fun append(c: Char) = apply { creator.append(c.toString()) }

    private fun SpannableStringCreator.appendSmart(csq: CharSequence?, spanDict: Map<CharSequence, ResSpans.() -> Unit>) {
        if (csq != null) {
            if (csq in spanDict) {
                append(csq, spanDict.getValue(csq))
            } else {
                val possibleMatchDict = spanDict.filter { it.key.toString() == csq }
                if (possibleMatchDict.isNotEmpty()) {
                    val spanDictEntry = possibleMatchDict.entries.toList()[0]
                    append(spanDictEntry.key, spanDictEntry.value)
                } else {
                    append(csq)
                }
            }
        }
    }
}

class ResSpans(private val context: Context) : Iterable<Any> {

    val spans = ArrayList<Any>()

    constructor(context: Context, spans: ResSpans.() -> Unit) : this(context) {
        spans.invoke(this)
    }

    override fun iterator() = spans.iterator()

    fun appearance(@StyleRes id: Int) =
        spans.add(TextAppearanceSpan(context, id))

    fun size(@DimenRes id: Int) =
        spans.add(AbsoluteSizeSpan(context.resources.getDimension(id).toInt()))

    fun size(@Dimension(unit = Dimension.SP) sizeInSp: Float) =
        spans.add(AbsoluteSizeSpan(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sizeInSp,
            context.resources.displayMetrics
        ).toInt()))

    fun underline() =
        spans.add(UnderlineSpan())

    fun strikethrough() =
        spans.add(StrikethroughSpan())

    fun color(@ColorRes id: Int) =
        spans.add(ForegroundColorSpan(ContextCompat.getColor(context, id)))

    fun colorInt(@ColorInt color: Int) =
        spans.add(ForegroundColorSpan(color))

    fun icon(@DrawableRes id: Int, size: Int, verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM) =
        AppCompatResources.getDrawable(context, id)?.let { drawable ->
            spans.add(
                ImageSpan(
                    drawable.apply {
                        setBounds(0, 0, size, size)
                    },
                    verticalAlignment
                )
            )
        } ?: false

    fun icon(icon: Drawable, verticalAlignment: Int = DynamicDrawableSpan.ALIGN_BOTTOM) =
        spans.add(ImageSpan(icon, verticalAlignment))

    fun typeface(@FontRes family: Int) = ResourcesCompat.getFont(context, family)?.let { font ->
        spans.add(CustomTypefaceSpan("", font))
    } ?: false
    fun typeface(typeface: Typeface) = spans.add(CustomTypefaceSpan("", typeface))

    fun click(isNeedUnderline: Boolean = false, action: () -> Unit) = spans.add(
        clickableSpan(isNeedUnderline, action)
    )

    fun lineHeight(@Dimension(unit = Dimension.SP) sp: Float) =
        spans.add(AbsoluteHeightSpan(context.spToPx(sp)))

    fun custom(span: Any) = spans.add(span)
}

fun clickableSpan(isNeedUnderline: Boolean, action: () -> Unit) = object : ClickableSpan() {
    override fun onClick(view: View) = action()

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = isNeedUnderline
    }
}

class AbsoluteHeightSpan(@Dimension(unit = Dimension.SP) private val height: Float) : LineHeightSpan {
    override fun chooseHeight(
        text: CharSequence,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: Paint.FontMetricsInt
    ) {
        val originHeight = fm.descent - fm.ascent
        // If original height is not positive, do nothing
        if (originHeight <= 0) return
        val ratio = height * 1.0f / originHeight
        fm.descent = (fm.descent * ratio).roundToInt()
        fm.ascent = fm.descent - height.toInt()
    }
}

private class CustomTypefaceSpan(family: String, private val newType: Typeface) : TypefaceSpan(family) {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0

        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = tf
    }
}