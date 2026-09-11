package com.ojs.countdownwallpaper

import android.content.Context
import android.content.SharedPreferences
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class CountdownState(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isTargetReached: Boolean,
    val isConfigured: Boolean
)

object CountdownModel {
    val TARGET_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")
    private const val PREFS_NAME = "ojs_countdown_prefs"
    private const val KEY_TARGET_EPOCH_MILLIS = "target_epoch_millis"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isConfigured(context: Context): Boolean {
        return getPrefs(context).contains(KEY_TARGET_EPOCH_MILLIS)
    }

    fun getTargetDateTime(context: Context): ZonedDateTime? {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_TARGET_EPOCH_MILLIS)) return null
        val epochMillis = prefs.getLong(KEY_TARGET_EPOCH_MILLIS, 0L)
        return Instant.ofEpochMilli(epochMillis).atZone(TARGET_ZONE)
    }

    fun setTargetDateTime(context: Context, zonedDateTime: ZonedDateTime) {
        getPrefs(context).edit()
            .putLong(KEY_TARGET_EPOCH_MILLIS, zonedDateTime.toInstant().toEpochMilli())
            .apply()
    }

    fun calculateRemainingTime(context: Context, now: Instant = Instant.now()): CountdownState {
        val target = getTargetDateTime(context) ?: return CountdownState(
            days = 0,
            hours = 0,
            minutes = 0,
            seconds = 0,
            isTargetReached = false,
            isConfigured = false
        )

        val currentZoned = now.atZone(TARGET_ZONE)
        val duration = Duration.between(currentZoned, target)

        return if (duration.isNegative || duration.isZero) {
            CountdownState(
                days = 0,
                hours = 0,
                minutes = 0,
                seconds = 0,
                isTargetReached = true,
                isConfigured = true
            )
        } else {
            val totalSeconds = duration.seconds
            CountdownState(
                days = totalSeconds / 86400,
                hours = (totalSeconds % 86400) / 3600,
                minutes = (totalSeconds % 3600) / 60,
                seconds = totalSeconds % 60,
                isTargetReached = false,
                isConfigured = true
            )
        }
    }
}
