package com.ojs.countdownwallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.ContextCompat

class CountdownRenderer(context: Context) {

    private val colorBg = ContextCompat.getColor(context, R.color.bg_black)
    private val colorWhite = ContextCompat.getColor(context, R.color.text_white)
    private val colorRed = ContextCompat.getColor(context, R.color.accent_red)
    private val colorLabel = ContextCompat.getColor(context, R.color.label_gray)
    private val colorColon = ContextCompat.getColor(context, R.color.colon_gray)

    private val bgPaint = Paint()
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val numberWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorWhite
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val numberRedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorRed
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val colonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorColon
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorLabel
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.1f
    }

    private var currentWidth = 0
    private var currentHeight = 0

    fun updateDimensions(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        currentWidth = width
        currentHeight = height

        bgPaint.shader = RadialGradient(
            width * 0.5f, height * 0.45f,
            height * 0.75f,
            intArrayOf(Color.parseColor("#1C1D21"), Color.parseColor("#0C0D0F"), colorBg),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )

        val scale = (width / 400f).coerceIn(0.8f, 3.5f)
        val numberSize = 34f * scale
        val labelSize = 8.5f * scale

        numberWhitePaint.textSize = numberSize
        numberRedPaint.textSize = numberSize
        colonPaint.textSize = numberSize * 0.9f
        labelPaint.textSize = labelSize

        glowPaint.shader = RadialGradient(
            width * 0.85f, height * 0.74f,
            width * 0.35f,
            intArrayOf(Color.argb(70, 229, 9, 20), Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
    }

    fun draw(canvas: Canvas, state: CountdownState) {
        if (currentWidth == 0 || currentHeight == 0) return

        canvas.drawRect(0f, 0f, currentWidth.toFloat(), currentHeight.toFloat(), bgPaint)

        if (state.isConfigured && !state.isTargetReached) {
            canvas.drawRect(0f, 0f, currentWidth.toFloat(), currentHeight.toFloat(), glowPaint)
        }

        val baselineY = currentHeight * 0.74f
        val labelY = baselineY + (numberWhitePaint.textSize * 0.45f)

        val daysStr = if (!state.isConfigured) "--" else String.format("%02d", state.days)
        val hoursStr = if (!state.isConfigured) "--" else String.format("%02d", state.hours)
        val minsStr = if (!state.isConfigured) "--" else String.format("%02d", state.minutes)
        val secsStr = if (!state.isConfigured) "--" else String.format("%02d", state.seconds)

        val colWidth = currentWidth / 4.2f
        val startX = (currentWidth - (colWidth * 3f)) / 2f

        val xDays = startX
        val xHours = startX + colWidth
        val xMins = startX + (colWidth * 2f)
        val xSecs = startX + (colWidth * 3f)

        val colon1X = (xDays + xHours) / 2f
        val colon2X = (xHours + xMins) / 2f
        val colon3X = (xMins + xSecs) / 2f

        val lastUnitPaint = if (state.isConfigured) numberRedPaint else numberWhitePaint

        canvas.drawText(daysStr, xDays, baselineY, numberWhitePaint)
        canvas.drawText(hoursStr, xHours, baselineY, numberWhitePaint)
        canvas.drawText(minsStr, xMins, baselineY, numberWhitePaint)
        canvas.drawText(secsStr, xSecs, baselineY, lastUnitPaint)

        val colonOffsetY = numberWhitePaint.textSize * 0.05f
        canvas.drawText(":", colon1X, baselineY - colonOffsetY, colonPaint)
        canvas.drawText(":", colon2X, baselineY - colonOffsetY, colonPaint)
        canvas.drawText(":", colon3X, baselineY - colonOffsetY, colonPaint)

        canvas.drawText("DAYS", xDays, labelY, labelPaint)
        canvas.drawText("HOURS", xHours, labelY, labelPaint)
        canvas.drawText("MINUTES", xMins, labelY, labelPaint)
        canvas.drawText("SECONDS", xSecs, labelY, labelPaint)
    }
}
