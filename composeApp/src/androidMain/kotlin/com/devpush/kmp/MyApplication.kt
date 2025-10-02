package com.devpush.kmp

import android.app.Application
import org.koin.dsl.module
import com.devpush.kmp.di.intiKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        intiKoin {
            it.modules(
                module {
                    single { this@MyApplication.applicationContext }
                })
        }

    }
}