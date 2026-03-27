// commonMain
package dev.androidbroadcast.claudex.ui.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Converts an epoch-millisecond timestamp to a short human-readable relative label.
 * Returns "Today", "Yesterday", or "MMM d" (e.g. "Mar 27") for older dates.
 */
internal fun Long.toRelativeTimestamp(): String {
    val tz = TimeZone.currentSystemDefault()
    val date = Instant.fromEpochMilliseconds(this).toLocalDateTime(tz).date
    val today = Clock.System.now().toLocalDateTime(tz).date
    return when {
        date == today -> "Today"
        date == today.minus(1, DateTimeUnit.DAY) -> "Yesterday"
        else -> "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }} ${date.dayOfMonth}"
    }
}
