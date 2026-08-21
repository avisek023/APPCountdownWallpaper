package com.ojs.countdownwallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ojs.countdownwallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val countdownRunnable = object : Runnable {
        override fun run() {
            val state = CountdownModel.calculateRemainingTime()
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

        binding.btnSetWallpaper.setOnClickListener {
            launchWallpaperPicker()
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

    private fun updateUi(state: CountdownState) {
        binding.tvDays.text = String.format("%02d", state.days)
        binding.tvHours.text = String.format("%02d", state.hours)
        binding.tvMinutes.text = String.format("%02d", state.minutes)
        binding.tvSeconds.text = String.format("%02d", state.seconds)
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
                Toast.makeText(this, "Select OJS Countdown from wallpaper options.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
