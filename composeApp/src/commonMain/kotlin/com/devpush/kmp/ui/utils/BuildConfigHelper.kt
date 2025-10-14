package com.devpush.kmp.ui.utils

/**
 * Helper object to detect debug builds across different platforms
 */
expect object BuildConfigHelper {
    /**
     * Returns true if this is a debug build, false otherwise
     */
    fun isDebugBuild(): Boolean
}