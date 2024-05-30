package com.js.floatinglogger

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

class FloatingLoggerService : Service() {

    private lateinit var windowManager: WindowManager

    private lateinit var floatingView: View
    private lateinit var fab: FloatingActionButton
    private lateinit var floatingExpandLayout: ConstraintLayout
    private lateinit var loggerTextView: TextView

    private lateinit var params: WindowManager.LayoutParams

    private var isFabOpen = false

    private val CLICK_DRAG_TOLERANCE = 10f

    private var layoutFlag: Int = 0

    private val pid = android.os.Process.myPid()
    private val commandArray = mutableListOf("logcat", "-v", "time", "--pid=$pid")
    private val logcat: Process = Runtime.getRuntime().exec(commandArray.toTypedArray())
    private val br = BufferedReader(InputStreamReader(logcat.inputStream), 4 * 1024)
    private val separator = System.lineSeparator()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initFloatingWindowView()

        initCoroutine()

        initFabTouchEvent()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun initFloatingWindowView() {
        layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.layout_floating_logger, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).also {
            it.gravity = Gravity.TOP or Gravity.END
            it.x = 0
            it.y = 100
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)

        fab = floatingView.findViewById(R.id.floatingButton)
        floatingExpandLayout = floatingView.findViewById(R.id.floatingExpandLayout)
        val minimizeImageButton = floatingView.findViewById<ImageButton>(R.id.minimizeImageButton)
        val clearImageButton = floatingView.findViewById<ImageButton>(R.id.clearImageButton)
        val closeImageButton = floatingView.findViewById<ImageButton>(R.id.closeImageButton)
        loggerTextView = floatingView.findViewById<TextView>(R.id.loggerTextView)

        minimizeImageButton.setOnClickListener {
            fabMinimize()
        }

        clearImageButton.setOnClickListener {
            loggerTextView.text = ""
        }

        closeImageButton.setOnClickListener {
            stopSelf()
        }
    }

    private fun initCoroutine() {
        scope.launch {
            getData().collect { str ->
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    loggerTextView.append("\n$str")
                }
            }
        }
    }

    private fun getData(): Flow<String> = flow {
        var line = ""
        while (br.readLine().also { line = it } != null) {
            emit(line + separator)
        }
    }.flowOn(Dispatchers.IO)

    @SuppressLint("ClickableViewAccessibility")
    private fun initFabTouchEvent() {
        var initialX = 0
        var initialY = 0

        var initialTouchX = 0.0f
        var initialTouchY = 0.0f

        fab.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y

                    initialTouchX = motionEvent.rawX
                    initialTouchY = motionEvent.rawY

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    params.x = initialX + (initialTouchX - motionEvent.rawX).toInt()
                    params.y = initialY + (motionEvent.rawY - initialTouchY).toInt()

                    if (abs(initialTouchX - motionEvent.rawX) < CLICK_DRAG_TOLERANCE
                        && abs(initialTouchY - motionEvent.rawY) < CLICK_DRAG_TOLERANCE
                    ) {
                        if (isFabOpen) {
                            fabMinimize()
                        } else {
                            fabExpand()
                        }

                        return@setOnTouchListener true // to handle Click
                    }

                    return@setOnTouchListener false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!::floatingView.isInitialized) return@setOnTouchListener false

                    params.x = initialX + (initialTouchX - motionEvent.rawX).toInt()
                    params.y = initialY + (motionEvent.rawY - initialTouchY).toInt()

                    windowManager.updateViewLayout(floatingView, params)

                    return@setOnTouchListener true
                }
            }

            return@setOnTouchListener false
        }

        floatingExpandLayout.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y

                    initialTouchX = motionEvent.rawX
                    initialTouchY = motionEvent.rawY

                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!::floatingView.isInitialized) return@setOnTouchListener false

                    params.x = initialX + (initialTouchX - motionEvent.rawX).toInt()
                    params.y = initialY + (motionEvent.rawY - initialTouchY).toInt()

                    windowManager.updateViewLayout(floatingView, params)

                    return@setOnTouchListener true
                }
            }

            return@setOnTouchListener false
        }
    }

    private fun fabExpand() {
        isFabOpen = true

        fab.visibility = View.GONE
        floatingExpandLayout.visibility = View.VISIBLE

        windowManager.removeView(floatingView)
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            400.dp,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).also {
            it.gravity = Gravity.TOP or Gravity.END
            it.x = 0
            it.y = 100
        }
        windowManager.addView(floatingView, params)
    }

    private fun fabMinimize() {
        isFabOpen = false

        fab.visibility = View.VISIBLE
        floatingExpandLayout.visibility = View.GONE

        windowManager.removeView(floatingView)
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).also {
            it.gravity = Gravity.TOP or Gravity.END
            it.x = 0
            it.y = 100
        }
        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::floatingView.isInitialized)
            windowManager.removeView(floatingView)
    }
}

