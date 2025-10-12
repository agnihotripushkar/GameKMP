package com.devpush.kmp

import com.devpush.kmp.service.AppInitializationService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * App initialization functions that can be called from platform-specific code
 */
object AppInitialization : KoinComponent {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val appInitializationService: AppInitializationService by inject()
    
    /**
     * Initializes the app asynchronously
     * This should be called after Koin is initialized
     */
    fun initializeAppAsync() {
        applicationScope.launch {
            try {
                val result = appInitializationService.initializeApp()
                result.fold(
                    onSuccess = {
                        Napier.d("App initialization completed successfully")
                    },
                    onFailure = { error ->
                        Napier.e("App initialization failed", error)
                        // App can still continue, but some features might not work properly
                    }
                )
            } catch (e: Exception) {
                Napier.e("Unexpected error during app initialization", e)
            }
        }
    }
    
    /**
     * Initializes the app synchronously
     * Use this only if you need to block until initialization is complete
     */
    suspend fun initializeApp(): Result<Unit> {
        return try {
            appInitializationService.initializeApp()
        } catch (e: Exception) {
            Napier.e("Unexpected error during app initialization", e)
            Result.failure(e)
        }
    }
}