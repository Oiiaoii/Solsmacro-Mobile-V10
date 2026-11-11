        add(how, tv("""1. Turn on the service, then open Roblox. A floating bar appears (START, PAUSE, SET, COLOR, TEST).
2. SET: tap the 8 spots it asks for (5 buttons, joystick, camera area, Shift Lock).
3. COLOR: start fishing, the screen freezes after 8 seconds. Tap the bar corners, the green zone and the white marker.
4. Optional: start fast capture below for about 30 checks a second.
5. TEST while the bar shows. Turn on the debug overlay to see boxes.
6. START. Drag the bar to move it.""", 12f, cMut), 6)

        val fish = card("Fishing")
        row(fish, btn("Fast", cBd) { preset(700, 3000, 15, 80) }, btn("Balanced", cBd) { preset(900, 4000, 12, 120) }, btn("Safe", cBd) { preset(1000, 5500, 10, 160) })
        field(fish, "Cast hold (ms)", "cast")
        field(fish, "Bite wait (ms), timed mode", "wait")
        field(fish, "Reel taps, timed mode", "taps")
        field(fish, "Tap gap (ms), timed mode", "gap")
        field(fish, "Stop after N catches (0 = never)", "maxcatch")

        val det = card("Screen detection (Android 11+)")
        sw(det, "Use screen detection instead of timing", "detect")
        sw(det, "Debug overlay (green, yellow, red box)", "debug")
        field(det, "Color match tolerance (0-255)", "tol")
        field(det, "Minimum matching pixels", "minpx")
        field(det, "Max seconds to wait for the bar", "maxwait")
        field(det, "Bar direction (0 left/right, 1 up/down)", "axis")
        field(det, "Holding moves the marker (0 left/up, 1 right/down)", "holddir")
        field(det, "Dead zone (pixels)", "deadband")
        field(det, "Hold time per move (ms)", "holdms")

        val fl = card("Floating button")
        add(fl, tv("A round button stays on screen over Roblox. Tap it to open the control bar and tap it again to close it. Drag it to move it.", 12f, cMut), 6)
        prevIcon = roundIcon(72)
        applyIcon(prevIcon)
        fl.addView(prevIcon)
        row(fl, btn("Use This Icon", cAcc) { pickIcon() }, btn("Use default icon", cBd) { resetIcon() })

        val cap = card("Fast screen capture")
        add(cap, tv("Reads the screen about 30 times a second instead of 3. Android asks for permission each time you start it.", 12f, cMut), 6)
        add(cap, btn("Start fast capture", cOk) { requestCapture() }, 10)
        add(cap, btn("Stop fast capture", cBd) { Cap.release(); stopService(Intent(this, CaptureService::class.java)) }, 8)

        val mv = card("Movement, camera and Shift Lock")
        add(mv, tv("Scripts run with swipes on Roblox's on-screen joystick and camera area. One command per line:\nmove up 1.5   (also down, left, right, w, a, s, d)\ncamera left 250\nshiftlock\ntap inv\nwait 1\n# comment", 12f, cMut), 6)
        field(mv, "Joystick push distance (pixels)", "joyr")
        script(mv, "Run once when you press START", "script_start")
        script(mv, "Run before selling (walk to the seller)", "script_pre")
        script(mv, "Run after selling (walk back)", "script_post")

        val sell = card("Selling")
        sw(sell, "Auto sell", "sell")
        field(sell, "Sell every N catches", "every")
        field(sell, "Menu delay (ms)", "mdelay")

        val wh = card("Discord webhook")
        sw(wh, "Send webhook messages", "whon")
        add(wh, tv("Webhook URL", 12f, cMut), 10)
        val url = EditText(this).apply {
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setText(Cfg.gets(this@MainActivity, "webhook"))
            setTextColor(cTx)
            background = shape(cBg, 10, cBd)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { Cfg.puts(this@MainActivity, "webhook", s.toString().trim()) }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            })
        }
        add(wh, url, 4)
        field(wh, "Progress message every N catches (0 = off)", "whevery")
        add(wh, btn("Send test message", cAcc) {
            Hook.send(this, "Test", "Sol Macro webhook works", 0x38D39F)
            Toast.makeText(this, "Sent, check the log below", Toast.LENGTH_SHORT).show()
        }, 12)

        val lg = card("Log")
        logTv = tv("", 11f, cMut).apply { typeface = Typeface.MONOSPACE }
        add(lg, logTv, 8)
        add(lg, btn("Copy log", cBd) {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Sol Macro log", Dbg.tail(150)))
            Toast.makeText(this, "Log copied", Toast.LENGTH_SHORT).show()
        }, 10)

        setContentView(ScrollView(this).apply {
            setBackgroundColor(cBg)
            addView(root)
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    private fun refresh() {
        val on = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
            .contains("$packageName/")
        pill.text = if (on) "Service ON" else "Service OFF"
        pill.background = shape(if (on) cOk else cBad, 20)
        pill.setTextColor(if (on) 0xFF06251C.toInt() else Color.WHITE)
        stateTv.text = "State: " + Dbg.state
        val run = if (Dbg.startMs > 0 && Dbg.state != "Idle") (SystemClock.uptimeMillis() - Dbg.startMs) / 1000 else 0L
        statsTv.text = "Catches ${Dbg.catches}    Sells ${Dbg.sells}    Run ${run / 60}:${(run % 60).toString().padStart(2, '0')}"
        perfTv.text = "Capture: " + (if (Cap.active) "fast" else "slow (screenshots)") + "    ${Dbg.fps} checks/s    ${Dbg.latency} ms"
        posTv.text = Cfg.keys.joinToString("   ") { k -> "${Cfg.label[k]}: ${Cfg.get(this, k + "_x")},${Cfg.get(this, k + "_y")}" }
        logTv.text = Dbg.tail(25).ifEmpty { "No log yet" }
    }
}
