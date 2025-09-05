package com.devpush.coreDatabase.di

import app.cash.sqldelight.db.SqlDriver
import com.devpush.coreDatabase.SqlDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getCoreDatabaseModule(): Module {
    return module {
        single { SqlDriverFactory().getSqlDriver() }
        single {
            val driver = get<SqlDriver>()
            AppDatabase.invoke(driver) }
    }

}

