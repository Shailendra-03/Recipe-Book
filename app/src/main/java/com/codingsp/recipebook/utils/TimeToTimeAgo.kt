package com.codingsp.recipebook.utils

class TimeToTimeAgo {

    companion object {
        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private const val DAY_MILLIS = 24 * HOUR_MILLIS
    }

    fun getTimeAgo(time: Long): String {

        val now: Long = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return ""
        }

        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> "<1m "
            diff < 2 * MINUTE_MILLIS -> "1m"
            diff < 60 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS}m"
            diff < 2 * HOUR_MILLIS -> "1h"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS}h"
            diff < 48 * HOUR_MILLIS -> "1d"
            else -> "${diff / DAY_MILLIS}d"
        }
    }
}