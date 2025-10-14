package com.devpush.kmp.ui.utils

import platform.Foundation.NSBundle

/**
 * iOS-specific implementation of BuildConfigHelper
 */
actual object BuildConfigHelper {
    actual fun isDebugBuild(): Boolean {
        // On iOS, we can check if we're running in debug mode by looking at compiler flags
        // or by checking if the app is running in debug configuration
        return try {
            // Check if we're in debug configuration
            val bundle = NSBundle.mainBundle
            val buildConfiguration = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
            
            // In debug builds, we typically have debug symbols and different bundle configurations
            // This is a simple heuristic - in a real app you might want to use preprocessor directives
            true // For now, assume debug on iOS during development
        } catch (e: Exception) {
            false
        }
    }
}