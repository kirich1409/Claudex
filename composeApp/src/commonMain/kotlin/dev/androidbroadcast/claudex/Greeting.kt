package dev.androidbroadcast.claudex

public class Greeting {
    private val platform = getPlatform()

    public fun greet(): String = "Hello, ${platform.name}!"
}
