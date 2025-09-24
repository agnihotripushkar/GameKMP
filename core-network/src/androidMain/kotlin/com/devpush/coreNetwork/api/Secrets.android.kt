package com.devpush.coreNetwork.api

import com.devpush.coreNetwork.BuildConfig

actual object Secrets {
    actual val apiKey: String = BuildConfig.API_KEY
}