package com.kastinetech.torch

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var torchOn = false
    private var sosOn = false
    private var strobeOn = false
    private var pulseState = false
    private var batteryText: TextView? = null
    private var statusText: TextView? = null
    private var powerButton: Button? = null
    private val handler = Handler(Looper.getMainLooper())
    private var sosIndex = 0
    private var sosRunnable: Runnable? = null
    private var strobeRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = try {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) { null }
        buildUi()
        updateBattery()
        updateUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(16, 17, 22))
        }

        val brand = TextView(this).apply {
            text = "KASTINE TECH"
            textSize = 13f
            letterSpacing = 0.22f
            setTextColor(Color.rgb(255, 200, 87))
            gravity = Gravity.CENTER
        }
        root.addView(brand, matchWrap())

        val title = TextView(this).apply {
            text = "TORCH"
            textSize = 30f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(4))
        }
        root.addView(title, matchWrap())

        batteryText = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.rgb(170, 174, 188))
            gravity = Gravity.CENTER
        }
        root.addView(batteryText, matchWrap())

        val spacer = View(this)
        root.addView(spacer, LinearLayout.LayoutParams(1, dp(34)))

        powerButton = Button(this).apply {
            text = "⏻"
            contentDescription = "Turn flashlight on"
            textSize = 54f
            typeface = Typeface.DEFAULT
            isAllCaps = false
            setTextColor(Color.rgb(16, 17, 22))
            background = rounded(Color.rgb(255, 200, 87), 1000)
            elevation = dp(8).toFloat()
            setOnClickListener {
                stopSpecialModes()
                setTorch(!torchOn)
                vibrate()
            }
        }
        val powerParams = LinearLayout.LayoutParams(dp(190), dp(190))
        powerParams.gravity = Gravity.CENTER
        root.addView(powerButton, powerParams)

        statusText = TextView(this).apply {
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            textSize = 15f
            setTextColor(Color.rgb(170, 174, 188))
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(26))
        }
        root.addView(statusText, matchWrap())

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val sosButton = makeButton("SOS") { toggleSos() }
        val strobeButton = makeButton("STROBE") { toggleStrobe() }
        row.addView(sosButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
            marginEnd = dp(8)
        })
        row.addView(strobeButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
            marginStart = dp(8)
        })
        root.addView(row, LinearLayout.LayoutParams(-1, dp(54)))

        val note = TextView(this).apply {
            text = "Use responsibly. Flash patterns may affect people sensitive to flashing lights."
            textSize = 11f
            setTextColor(Color.rgb(125, 130, 145))
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(24), dp(8), 0)
        }
        root.addView(note, matchWrap())
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener { view, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                view.setPadding(
                    dp(24) + bars.left,
                    dp(24) + bars.top,
                    dp(24) + bars.right,
                    dp(24) + bars.bottom
                )
                insets
            }
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        }
        setContentView(scroll)
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(42, 44, 54), 18)
            setOnClickListener { action() }
        }

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun setTorch(enabled: Boolean): Boolean {
        val id = cameraId
        if (id == null) {
            torchOn = false
            updateUi()
            if (enabled) Toast.makeText(this, "No compatible flashlight found on this device.", Toast.LENGTH_LONG).show()
            return false
        }
        if (enabled && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            stopSpecialModes()
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            Toast.makeText(this, "Allow camera permission, then tap the torch button again.", Toast.LENGTH_LONG).show()
            updateUi()
            return false
        }
        return try {
            cameraManager.setTorchMode(id, enabled)
            torchOn = enabled
            updateUi()
            true
        } catch (_: CameraAccessException) {
            torchOn = false
            updateUi()
            Toast.makeText(this, "Flashlight is unavailable. Close other camera apps and try again.", Toast.LENGTH_LONG).show()
            false
        } catch (_: SecurityException) {
            torchOn = false
            updateUi()
            Toast.makeText(this, "Android did not allow flashlight access.", Toast.LENGTH_LONG).show()
            false
        } catch (_: Exception) {
            torchOn = false
            updateUi()
            Toast.makeText(this, "Couldn't change flashlight state on this device.", Toast.LENGTH_LONG).show()
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
            Toast.makeText(
                this,
                if (granted) "Permission granted. Tap the flashlight button to turn it on."
                else "Camera permission is needed for the phone's LED.",
                Toast.LENGTH_LONG
            ).show()
            updateUi()
        }
    }

    private fun updateUi() {
        powerButton?.text = "⏻"
        powerButton?.contentDescription = if (torchOn) "Turn flashlight off" else "Turn flashlight on"
        powerButton?.background = rounded(
            if (torchOn) Color.WHITE else Color.rgb(255, 200, 87), 1000
        )
        val status = when {
            sosOn -> "SOS MODE ACTIVE"
            strobeOn -> "STROBE MODE ACTIVE"
            torchOn -> "FLASHLIGHT ON"
            else -> "FLASHLIGHT OFF"
        }
        if (statusText?.text != status) statusText?.text = status
    }

    private fun toggleSos() {
        if (sosOn) {
            stopSpecialModes()
            setTorch(false)
            return
        }
        stopSpecialModes()
        sosOn = true
        sosIndex = 0
        runSosStep()
        updateUi()
    }

    private fun runSosStep() {
        if (!sosOn) return
        val step = FlashPatterns.sosSteps[sosIndex % FlashPatterns.sosSteps.size]
        if (!setTorch(step.isOn)) {
            stopSpecialModes()
            updateUi()
            return
        }
        sosRunnable = Runnable {
            if (sosOn) {
                sosIndex++
                runSosStep()
            }
        }
        handler.postDelayed(sosRunnable!!, step.durationMillis)
    }

    private fun toggleStrobe() {
        if (strobeOn) {
            stopSpecialModes()
            setTorch(false)
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Flashing light warning")
            .setMessage("Strobe mode flashes repeatedly and may cause discomfort or trigger seizures in people with photosensitivity. Continue only if it is safe.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ -> startStrobe() }
            .show()
    }

    private fun startStrobe() {
        stopSpecialModes()
        strobeOn = true
        pulseState = false
        val task = object : Runnable {
            override fun run() {
                if (!strobeOn) return
                pulseState = !pulseState
                if (!setTorch(pulseState)) {
                    stopSpecialModes()
                    updateUi()
                    return
                }
                handler.postDelayed(this, FlashPatterns.strobeIntervalMillis)
            }
        }
        strobeRunnable = task
        handler.post(task)
        updateUi()
    }

    private fun stopSpecialModes() {
        sosOn = false
        strobeOn = false
        sosRunnable?.let { handler.removeCallbacks(it) }
        strobeRunnable?.let { handler.removeCallbacks(it) }
        sosRunnable = null
        strobeRunnable = null
        pulseState = false
    }

    private fun updateBattery() {
        val manager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        batteryText?.text = if (level in 0..100) "Battery  •  $level%" else "Battery level unavailable"
    }

    private fun vibrate() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= 31) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        } catch (_: Exception) { }
    }

    override fun onPause() {
        super.onPause()
        // Keep the selected light running when the app is briefly backgrounded.
        // Android/device policies may still turn the torch off.
    }

    override fun onDestroy() {
        stopSpecialModes()
        if (torchOn) setTorch(false)
        super.onDestroy()
    }
}
