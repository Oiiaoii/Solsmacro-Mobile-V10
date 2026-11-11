package com.solmacro

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewOutlineProvider
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

private const val FILL = ViewGroup.LayoutParams.MATCH_PARENT
private const val HUG = ViewGroup.LayoutParams.WRAP_CONTENT

class MainActivity : Activity() {
    private val cBg = 0xFF0B111C.toInt()
    private val cCard = 0xFF151E30.toInt()
    private val cBd = 0xFF243050.toInt()
    private val cTx = 0xFFE8EEFC.toInt()
    private val cMut = 0xFF8B9BBD.toInt()
    private val cAcc = 0xFF3EA6FF.toInt()
    private val cOk = 0xFF38D39F.toInt()
    private val cBad = 0xFFFF6B6B.toInt()

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var root: LinearLayout
    private lateinit var pill: TextView
    private lateinit var stateTv: TextView
    private lateinit var statsTv: TextView
    private lateinit var posTv: TextView
    private lateinit var perfTv: TextView
    private lateinit var logo: ImageView
    private lateinit var prevIcon: ImageView
    private var cardN = 0
    private lateinit var logTv: TextView

    private val tick = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 500)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun shape(color: Int, r: Int, stroke: Int = 0) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(r).toFloat()
        if (stroke != 0) setStroke(dp(1), stroke)
    }

    private fun tv(t: String, size: Float, color: Int = cTx, bold: Boolean = false) = TextView(this).apply {
        text = t
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun add(p: LinearLayout, v: View, top: Int = 8) {
        p.addView(v, LinearLayout.LayoutParams(FILL, HUG).apply { topMargin = dp(top) })
    }

    private fun card(title: String?): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = shape(cCard, 16, cBd)
            setPadding(dp(16), dp(14), dp(16), dp(16))
        }
        add(root, c, 12)
        c.alpha = 0f
