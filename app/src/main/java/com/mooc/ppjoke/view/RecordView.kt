package com.mooc.ppjoke.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.mooc.libcommon.utils.PixUtils
import com.mooc.ppjoke.R

@SuppressLint("ClickableViewAccessibility")
class RecordView : View, View.OnLongClickListener, View.OnClickListener {
    private var startRecordTime: Long = 0
    private var isRecording: Boolean = false
    private var progressValue: Int = 0
    private lateinit var progressPaint: Paint
    private lateinit var fillPaint: Paint
    private var progressMaxValue: Int
    private val PROGRESS_INTERVAL = 100
    private var maxDuration: Int
    private var fillColor: Int
    private var progressColor: Int
    private var progressWidth: Int
    private var radius: Int

    private var performClick: () -> Unit = {}
    private var performLongClick: () -> Unit = {}
    private var performFinish: () -> Unit = {}

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        val typeArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.RecordView,
            defStyleAttr,
            defStyleRes
        )
        radius = typeArray.getDimensionPixelOffset(R.styleable.RecordView_radius, 0)
        progressWidth = typeArray.getDimensionPixelOffset(
            R.styleable.RecordView_progress_width, PixUtils.dp2px(
                3
            )
        )
        progressColor = typeArray.getColor(R.styleable.RecordView_progress_color, Color.RED)
        fillColor = typeArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE)
        maxDuration  = typeArray.getInt(R.styleable.RecordView_duration, 10)
        progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL
        typeArray.recycle()

        fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = fillColor

        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressPaint.style = Paint.Style.STROKE
        progressPaint.color = progressColor
        progressPaint.strokeWidth = progressWidth.toFloat()

        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                progressValue++
                if (progressValue <= progressMaxValue) sendEmptyMessageDelayed(0, PROGRESS_INTERVAL.toLong())
                else finishRecord()
            }
        }
        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                isRecording = true
                startRecordTime = System.currentTimeMillis()
                handler.sendEmptyMessage(0)
            } else {
                val now = System.currentTimeMillis()
                if (now - startRecordTime > ViewConfiguration.getLongPressTimeout()) finishRecord()
                handler.removeCallbacksAndMessages(null)
                isRecording = false
                startRecordTime = 0
                progressValue = 0
                postInvalidate()
            }
            return@setOnTouchListener false
        }

        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isRecording) {
            canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), fillPaint)
            val left = (progressWidth / 2).toFloat()
            val top = (progressWidth / 2).toFloat()
            val right = (width - progressWidth / 2).toFloat()
            val bottom = (width - progressWidth / 2).toFloat()
            val sweepAngle = (progressValue.toFloat() / progressMaxValue) * 360
            canvas?.drawArc(left, top, right, bottom, -90F, sweepAngle, false, progressPaint)
        } else {
            canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius.toFloat(), fillPaint)
        }
    }

    private fun setMaxDuration(maxDuration: Int) {
        progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL
    }

    private fun finishRecord() {
        performFinish.invoke()
    }

    override fun onLongClick(v: View?): Boolean {
        performLongClick.invoke()
        return true
    }

    override fun onClick(v: View?) {
        performClick.invoke()
    }

    fun onClick(block: () -> Unit) {
        performClick = block
    }

    fun onLongClick(block: () -> Unit) {
        performLongClick = block
    }

    fun onFinish(block: () -> Unit) {
        performFinish = block
    }
}