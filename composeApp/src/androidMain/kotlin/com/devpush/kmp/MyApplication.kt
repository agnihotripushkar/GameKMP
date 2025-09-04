package com.devpush.kmp

import android.app.Application
import org.koin.dsl.module
import com.devpush.kmp.di.intiKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        intiKoin {
            it.modules(
                module {
                    single { this@MyApplication.applicationContext }
                })
        }

    }
}