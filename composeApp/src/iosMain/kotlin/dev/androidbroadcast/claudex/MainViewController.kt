package dev.androidbroadcast.claudex

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionNaming")
public fun MainViewController(): UIViewController = ComposeUIViewController { App() }
