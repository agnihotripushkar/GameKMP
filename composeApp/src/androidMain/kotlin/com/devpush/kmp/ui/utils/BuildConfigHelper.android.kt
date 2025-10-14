package com.devpush.kmp.ui.utils

import com.devpush.kmp.BuildConfig

/**
 * Android-specific implementation of BuildConfigHelper
 */
actual object BuildConfigHelper {
    actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG
}