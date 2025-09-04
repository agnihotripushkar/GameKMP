package com.devpush.coreDatabase.di

import app.cash.sqldelight.db.SqlDriver
import com.devpush.coreDatabase.SqlDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module
import com.devpush.coreDatabase.AppDatabase

actual fun getCoreDatabaseModule(): Module {
    return module {
        single { SqlDriverFactory().getSqlDriver() }
        single { AppDatabase.invoke(get<SqlDriver>()) }

    }

}