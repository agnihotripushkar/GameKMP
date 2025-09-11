package com.devpush.coreDatabase.di

import app.cash.sqldelight.db.SqlDriver
import com.devpush.coreDatabase.SqlDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module
import com.devpush.coreDatabase.AppDatabase
import android.content.Context

actual fun getCoreDatabaseModule(): Module {
    return module {
        single { SqlDriverFactory(get<Context>()).getSqlDriver() }
        single { AppDatabase.invoke(get<SqlDriver>()) }

    }

}