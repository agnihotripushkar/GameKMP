package com.devpush.features.common.utils

import platform.Foundation.NSString
import platform.Foundation.localizedCaseInsensitiveCompare

/**
 * iOS implementation of StringUtils using Foundation's locale-aware string operations
 */
actual object StringUtils {
    
    actual fun String.toLowerCaseCompat(): String {
        return this.lowercase()
    }
    
    actual fun String.toUpperCaseCompat(): String {
        return this.uppercase()
    }
    
    actual fun String.compareIgnoreCase(other: String): Int {
        val nsString = this as NSString
        val result = nsString.localizedCaseInsensitiveCompare(other)
        return result.toInt()
    }
    
    actual fun String.equalsIgnoreCase(other: String): Boolean {
        return this.compareIgnoreCase(other) == 0
    }
}