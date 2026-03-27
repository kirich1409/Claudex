package dev.androidbroadcast.claudex

internal class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version") ?: "unknown"}"
}

public actual fun getPlatform(): Platform = JVMPlatform()
