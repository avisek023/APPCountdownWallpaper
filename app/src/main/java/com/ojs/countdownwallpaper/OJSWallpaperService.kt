package com.ojs.countdownwallpaper

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class OJSWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return CountdownEngine()
    }

    private inner class CountdownEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private lateinit var renderer: CountdownRenderer
        private var isEngineVisible = false

        private val tickRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (isEngineVisible) {
                    val now = SystemClock.uptimeMillis()
                    val nextTick = now + (1000L - (System.currentTimeMillis() % 1000L))
                    handler.postAtTime(this, nextTick)
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            renderer = CountdownRenderer(applicationContext)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isEngineVisible = visible
            if (visible) {
                handler.removeCallbacks(tickRunnable)
                handler.post(tickRunnable)
            } else {
                handler.removeCallbacks(tickRunnable)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            renderer.updateDimensions(width, height)
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            isEngineVisible = false
            handler.removeCallbacks(tickRunnable)
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(tickRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            var canvas = try {
                holder.lockCanvas()
            } catch (e: Exception) {
                null
            }

            if (canvas != null) {
                try {
                    val state = CountdownModel.calculateRemainingTime()
                    renderer.draw(canvas, state)
                } finally {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) { }
                }
            }
        }
    }
}
