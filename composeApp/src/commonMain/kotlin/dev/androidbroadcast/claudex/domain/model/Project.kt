package dev.androidbroadcast.claudex.domain.model

public data class Project(
    val id: String,
    val name: String,
    val path: String,
    val gitUrl: String?,
    val createdAt: Long,
)
