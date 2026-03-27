package dev.androidbroadcast.claudex

import android.os.Build

internal class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

public actual fun getPlatform(): Platform = AndroidPlatform()
