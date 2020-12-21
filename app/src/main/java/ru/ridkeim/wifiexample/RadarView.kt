package ru.ridkeim.wifiexample

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.util.AttributeSet
import android.view.View
import kotlin.math.min


class RadarView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val maxSignalLevel = 100
    // Центр радара
    private var mCenter : Point = Point()
    // Максимальный радиус окружности радара
    private var mRadius = 0f
    // Размер шрифта
    private var mTextSize = 0f
    // Размер маркера
    private var mMarkerSize = 0f
    // Границы внешнего и внутреннего колец
    lateinit var mOuterBox: RectF
    lateinit var mInnerBox: RectF

    private var mCirclePaint: Paint
    private var mAxisPaint: Paint
    private var mTextPaint: Paint

    // Радар
    private lateinit var mRadarRadialGradient: RadialGradient
    private var mRadarGradientColors: IntArray
    private var mRadarGradientPositions: FloatArray
    private var mRadarGradientPaint: Paint

    // Стекло
    private lateinit var mGlassRadialGradient: RadialGradient
    private var mGlassGradientColors: IntArray
    private var mGlassGradientPositions: FloatArray
    private var mGlassPaint: Paint

    private var mAccessPoints: List<AccessPoint> = emptyList()

    init {
        isFocusable = true
        // Окружности
        mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.BLACK
            style = Paint.Style.STROKE
        }
        // Сетка
        mAxisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(0x60, 0xFF, 0xFF, 0xFF)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        // Текст и маркеры
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFakeBoldText = true
            isSubpixelText = true
            textAlign = Align.LEFT
        }
        // Радар
        mRadarGradientColors = IntArray(4)
        mRadarGradientColors[0] = Color.rgb(0xB8, 0xE0, 0xFF)
        mRadarGradientColors[1] = Color.rgb(0xA1, 0xCF, 0xFF)
        mRadarGradientColors[2] = Color.rgb(0x62, 0xAA, 0xFF)
        mRadarGradientColors[3] = Color.BLACK

        mRadarGradientPositions = FloatArray(4)
        mRadarGradientPositions[0] = 0.0f
        mRadarGradientPositions[1] = 0.2f
        mRadarGradientPositions[2] = 0.9f
        mRadarGradientPositions[3] = 1.0f

        mRadarGradientPaint = Paint()
        // Стекло
        val glassColor = 0xF5
        mGlassGradientColors = IntArray(5)
        mGlassGradientColors[0] = Color.argb(0, glassColor, glassColor, glassColor)
        mGlassGradientColors[1] = Color.argb(0, glassColor, glassColor, glassColor)
        mGlassGradientColors[2] = Color.argb(50, glassColor, glassColor, glassColor)
        mGlassGradientColors[3] = Color.argb(100, glassColor, glassColor, glassColor)
        mGlassGradientColors[4] = Color.argb(65, glassColor, glassColor, glassColor)

        mGlassGradientPositions = FloatArray(5)
        mGlassGradientPositions[0] = 0.00f
        mGlassGradientPositions[1] = 0.80f
        mGlassGradientPositions[2] = 0.90f
        mGlassGradientPositions[3] = 0.94f
        mGlassGradientPositions[4] = 1.00f

        mGlassPaint = Paint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = measure(widthMeasureSpec)
        val measuredHeight = measure(heightMeasureSpec)
        val dimension = min(measuredWidth, measuredHeight)
        setMeasuredDimension(dimension, dimension)
    }

    private fun measure(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return if (specMode == MeasureSpec.UNSPECIFIED) {
            100
        } else {
            specSize
        }
    }

    fun setData(accessPoints: List<AccessPoint>) {
        mAccessPoints = accessPoints
        this.invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val px = w / 2
        val py = h / 2
        mCenter = Point(px, py)
        mRadius = min(px, py) - 20f // Отступ от краев канвы
        val ringWidth = mRadius / 20 // Ширина кольца
        mTextSize = mRadius / 16
        mMarkerSize = ringWidth / 2
        mOuterBox = RectF(
            mCenter.x - mRadius,
            mCenter.y - mRadius,
            mCenter.x + mRadius,
            mCenter.y + mRadius
        )
        mInnerBox = RectF(
            mCenter.x - mRadius + ringWidth,
            mCenter.y - mRadius + ringWidth,
            mCenter.x + mRadius - ringWidth,
            mCenter.y + mRadius - ringWidth
        )

        mRadarRadialGradient = RadialGradient(
            mCenter.x.toFloat(), mCenter.y.toFloat(), mRadius,
            mRadarGradientColors, mRadarGradientPositions, Shader.TileMode.CLAMP
        )
        mGlassRadialGradient = RadialGradient(
            mCenter.x.toFloat(), mCenter.y.toFloat(), mRadius - ringWidth,
            mGlassGradientColors, mGlassGradientPositions, Shader.TileMode.CLAMP
        )
    }
    override fun onDraw(canvas: Canvas) {
        // Радиальный градиентный шейдер радара
        mRadarGradientPaint.shader = mRadarRadialGradient
        canvas.drawOval(mOuterBox, mRadarGradientPaint)

        // Сетка
        canvas.drawCircle(
            mCenter.x.toFloat(),
            mCenter.y.toFloat(),
            mRadius * 0.10f,
            mAxisPaint
        )
        canvas.drawCircle(
            mCenter.x.toFloat(),
            mCenter.y.toFloat(),
            mRadius * 0.40f,
            mAxisPaint
        )
        canvas.drawCircle(
            mCenter.x.toFloat(),
            mCenter.y.toFloat(),
            mRadius * 0.70f,
            mAxisPaint
        )
        canvas.save()
        canvas.rotate(15f, mCenter.x.toFloat(), mCenter.y.toFloat())
        for (i in 0..11) {
            canvas.drawLine(
                mCenter.x.toFloat(),
                mCenter.y.toFloat(),
                mCenter.x + mRadius * 0.75f,
                mCenter.y.toFloat(),
                mAxisPaint
            )
            canvas.rotate(30f, mCenter.x.toFloat(), mCenter.y.toFloat())
        }
        canvas.restore()

        // Данные
        drawData(canvas)

        // Шейдер стекла
        mGlassPaint.shader = mGlassRadialGradient
        canvas.drawOval(mInnerBox, mGlassPaint)

        // Внешняя окружность кольца
        mCirclePaint.strokeWidth = 1f
        canvas.drawOval(mOuterBox, mCirclePaint)

        // Внутренняя окружность кольца
        mCirclePaint.strokeWidth = 2f
        canvas.drawOval(mInnerBox, mCirclePaint)
    }

    private fun drawData(canvas: Canvas) {
        if (mAccessPoints.isEmpty()) return
        mTextPaint.textSize = mTextSize
        val zoom: Float = mRadius * 0.75f / maxSignalLevel
        for (accessPoint in mAccessPoints) {
            val channel: Int = accessPoint.channel
            if (channel < 1 || channel > 12) continue
            val level: Int = accessPoint.level
            val security: Int = accessPoint.security
            val wps: Boolean = accessPoint.wps
            val ssid: String = accessPoint.ssid
            val alpha = (30 * channel)
            val dx =
                ((maxSignalLevel - level) * Math.cos(alpha * Math.PI / 180)).toFloat()
            val dy =
                ((maxSignalLevel - level) * Math.sin(alpha * Math.PI / 180)).toFloat()
            val x = mCenter.x + dx * zoom
            val y = mCenter.y - dy * zoom
            val transparentValue: Int = level * 200 / maxSignalLevel + 55

            // Открытая сеть или WEP
            if (security == 0 || security == 1) {
                mTextPaint.color = Color.argb(transparentValue, 0x00, 0x60, 0x00)
                canvas.drawText(ssid, x + mMarkerSize, y - mMarkerSize, mTextPaint)
            } else  // WPA или WPA2, но с поддержкой WPS
                if (security == 2 && wps) {
                    mTextPaint.color = Color.argb(transparentValue, 0x00, 0x00, 0x80)
                    canvas.drawText(ssid, x + mMarkerSize, y - mMarkerSize, mTextPaint)
                } else  // WPA или WPA2 без с поддержки WPS
                    mTextPaint.color = Color.argb(transparentValue, 0xFF, 0xFF, 0xFF)
            canvas.drawCircle(x, y, mMarkerSize, mTextPaint)
        }
    }

}