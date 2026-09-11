package com.ojs.countdownwallpaper

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ojs.countdownwallpaper.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var selectedTarget: ZonedDateTime? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a 'IST'")

    private val countdownRunnable = object : Runnable {
        override fun run() {
            val state = CountdownModel.calculateRemainingTime(this@MainActivity)
            updateUi(state)
            if (isRunning) {
                val delay = 1000L - (System.currentTimeMillis() % 1000L)
                handler.postDelayed(this, delay)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedTarget = CountdownModel.getTargetDateTime(this)
        displayTargetInfo()

        binding.btnPickDate.setOnClickListener { showDatePickerFlow() }
        binding.btnPickTime.setOnClickListener { showTimePickerFlow() }
        binding.btnSetWallpaper.setOnClickListener {
            if (selectedTarget == null) {
                Toast.makeText(this, "Please set target date and time first", Toast.LENGTH_SHORT).show()
                showDatePickerFlow()
            } else {
                launchWallpaperPicker()
            }
        }

        if (selectedTarget == null) {
            binding.root.post { showDatePickerFlow() }
        }
    }

    override fun onResume() {
        super.onResume()
        isRunning = true
        handler.post(countdownRunnable)
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
        handler.removeCallbacks(countdownRunnable)
    }

    private fun displayTargetInfo() {
        val target = selectedTarget
        if (target == null) {
            binding.tvSelectedDate.text = "NO TARGET SET"
            binding.tvSelectedTime.text = "Tap below to select exam date & time"
            binding.btnSetWallpaper.alpha = 0.5f
        } else {
            binding.tvSelectedDate.text = target.format(dateFormatter)
            binding.tvSelectedTime.text = target.format(timeFormatter)
            binding.btnSetWallpaper.alpha = 1.0f
        }
    }

    private fun showDatePickerFlow() {
        val base = selectedTarget ?: ZonedDateTime.now(CountdownModel.TARGET_ZONE).plusDays(1)
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                val newTime = selectedTarget?.toLocalTime() ?: LocalTime.of(9, 0)
                selectedTarget = ZonedDateTime.of(newDate, newTime, CountdownModel.TARGET_ZONE)
                saveTarget()

                if (!CountdownModel.isConfigured(this)) {
                    showTimePickerFlow()
                }
            },
            base.year,
            base.monthValue - 1,
            base.dayOfMonth
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showTimePickerFlow() {
        val base = selectedTarget ?: ZonedDateTime.now(CountdownModel.TARGET_ZONE).plusDays(1)
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val newDate = selectedTarget?.toLocalDate() ?: LocalDate.now(CountdownModel.TARGET_ZONE).plusDays(1)
                selectedTarget = ZonedDateTime.of(newDate, LocalTime.of(hourOfDay, minute, 0), CountdownModel.TARGET_ZONE)
                saveTarget()
            },
            base.hour,
            base.minute,
            false
        )
        timePicker.show()
    }

    private fun saveTarget() {
        val target = selectedTarget ?: return
        CountdownModel.setTargetDateTime(this, target)
        displayTargetInfo()
        val state = CountdownModel.calculateRemainingTime(this)
        updateUi(state)
        Toast.makeText(this, "Target saved", Toast.LENGTH_SHORT).show()
    }

    private fun updateUi(state: CountdownState) {
        if (!state.isConfigured) {
            binding.tvDays.text = "--"
            binding.tvHours.text = "--"
            binding.tvMinutes.text = "--"
            binding.tvSeconds.text = "--"
        } else if (state.isTargetReached) {
            binding.tvDays.text = "00"
            binding.tvHours.text = "00"
            binding.tvMinutes.text = "00"
            binding.tvSeconds.text = "00"
        } else {
            binding.tvDays.text = String.format("%02d", state.days)
            binding.tvHours.text = String.format("%02d", state.hours)
            binding.tvMinutes.text = String.format("%02d", state.minutes)
            binding.tvSeconds.text = String.format("%02d", state.seconds)
        }
    }

    private fun launchWallpaperPicker() {
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@MainActivity, OJSWallpaperService::class.java)
                )
            }
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Toast.makeText(this, "Select from wallpaper options.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
