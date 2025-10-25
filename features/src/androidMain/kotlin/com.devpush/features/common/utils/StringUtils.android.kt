package com.devpush.features.common.utils

/**
 * Android implementation of StringUtils using platform-agnostic string operations
 */
actual object StringUtils {
    
    actual fun String.toLowerCaseCompat(): String {
        return this.lowercase()
    }
    
    actual fun String.toUpperCaseCompat(): String {
        return this.uppercase()
    }
    
    actual fun String.compareIgnoreCase(other: String): Int {
        return this.compareTo(other, ignoreCase = true)
    }
    
    actual fun String.equalsIgnoreCase(other: String): Boolean {
        return this.equals(other, ignoreCase = true)
    }
}