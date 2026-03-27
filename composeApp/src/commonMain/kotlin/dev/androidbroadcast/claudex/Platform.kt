package dev.androidbroadcast.claudex

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
