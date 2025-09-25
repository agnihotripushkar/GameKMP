package com.devpush.coreNetwork.api

import platform.Foundation.NSBundle

actual object Secrets {
    actual val apiKey: String = NSBundle.mainBundle.objectForInfoDictionaryKey("API_KEY") as String
}