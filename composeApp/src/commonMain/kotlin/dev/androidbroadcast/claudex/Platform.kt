package dev.androidbroadcast.claudex

public interface Platform {
    public val name: String
}

public expect fun getPlatform(): Platform