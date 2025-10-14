package com.devpush.kmp.service

import com.devpush.features.game.domain.service.CollectionInitializationService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service responsible for initializing the app on startup
 * Handles all initialization tasks that need to happen before the app is ready
 */
interface AppInitializationService {
    /**
     * Initializes the app on startup
     * This should be called once when the app starts
     */
    suspend fun initializeApp(): Result<Unit>
    
    /**
     * Initializes the app asynchronously in the background
     * This is useful for non-blocking app startup
     */
    fun initializeAppAsync(onComplete: (Result<Unit>) -> Unit = {})
}

class AppInitializationServiceImpl(
    private val collectionInitializationService: CollectionInitializationService
) : AppInitializationService {
    
    private val initializationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override suspend fun initializeApp(): Result<Unit> {
        return try {
            Napier.d("Starting app initialization...")
            
            // Initialize collections system
            val collectionsResult = collectionInitializationService.initializeOnAppStart()
            
            collectionsResult.fold(
                onSuccess = { result ->
                    Napier.d("Collections initialization completed successfully in ${result.initializationTimeMs}ms")
                    if (result.defaultCollectionsCreated.isNotEmpty()) {
                        Napier.d("Created ${result.defaultCollectionsCreated.size} default collections")
                    }
                    if (result.wasAlreadyInitialized) {
                        Napier.d("Collections were already initialized")
                    }
                },
                onFailure = { error ->
                    Napier.e("Collections initialization failed", error)
                    return Result.failure(
                        Exception("Failed to initialize collections: ${error.message}", error)
                    )
                }
            )
            
            // Future: Add other initialization tasks here
            // - User preferences initialization
            // - Cache warming
            // - Migration tasks
            // - etc.
            
            Napier.d("App initialization completed successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Napier.e("App initialization failed", e)
            Result.failure(e)
        }
    }
    
    override fun initializeAppAsync(onComplete: (Result<Unit>) -> Unit) {
        initializationScope.launch {
            val result = initializeApp()
            onComplete(result)
        }
    }
}