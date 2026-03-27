// Timestamps are stored as epoch-milliseconds (Long) to match SQLDelight INTEGER columns.
// kotlinx-datetime is used in the component/process layers for Clock.System.now().toEpochMilliseconds().
package dev.androidbroadcast.claudex.domain.model

public data class Project(
    val id: String,
    val name: String,
    val path: String,
    val gitUrl: String?,
    val createdAt: Long,
)
