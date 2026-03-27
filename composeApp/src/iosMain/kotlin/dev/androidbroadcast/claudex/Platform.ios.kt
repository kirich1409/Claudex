package dev.androidbroadcast.claudex

import platform.UIKit.UIDevice

internal class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

public actual fun getPlatform(): Platform = IOSPlatform()