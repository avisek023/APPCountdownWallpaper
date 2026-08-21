package com.ojs.countdownwallpaper

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class CountdownState(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isTargetReached: Boolean
)

object CountdownModel {
    private val TARGET_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")
    
    private val TARGET_DATE_TIME: ZonedDateTime = ZonedDateTime.of(
        2026, 11, 1, 9, 0, 0, 0, TARGET_ZONE
    )

    fun calculateRemainingTime(now: Instant = Instant.now()): CountdownState {
        val currentZoned = now.atZone(TARGET_ZONE)
        val duration = Duration.between(currentZoned, TARGET_DATE_TIME)

        return if (duration.isNegative || duration.isZero) {
            CountdownState(0, 0, 0, 0, true)
        } else {
            val totalSeconds = duration.seconds
            CountdownState(
                days = totalSeconds / 86400,
                hours = (totalSeconds % 86400) / 3600,
                minutes = (totalSeconds % 3600) / 60,
                seconds = totalSeconds % 60,
                isTargetReached = false
            )
        }
    }
}
