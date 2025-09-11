package com.devpush.coreDatabase.di

import app.cash.sqldelight.db.SqlDriver
import com.devpush.coreDatabase.SqlDriverFactory
import com.devpush.coreDatabase.AppDatabase // Added import
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getCoreDatabaseModule(): Module {
    return module {
        // You might also want to explicitly specify the type for SqlDriver for consistency
        single<SqlDriver> { SqlDriverFactory().getSqlDriver() } 
        // Explicitly specify AppDatabase as the type provided by this single
        single<AppDatabase> { AppDatabase.invoke(get<SqlDriver>()) }
    }

}
