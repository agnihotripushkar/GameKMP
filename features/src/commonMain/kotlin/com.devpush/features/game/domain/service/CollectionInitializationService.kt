package com.devpush.features.game.domain.service

import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionError
import com.devpush.features.game.domain.usecase.InitializeDefaultCollectionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Service responsible for initializing collections on app startup
 * Ensures default collections are always available and handles migration logic
 */
interface CollectionInitializationService {
    /**
     * Initializes the collections system on app startup
     * This should be called once when the app starts
     * @return Result indicating success or failure of initialization
     */
    suspend fun initializeOnAppStart(): Result<InitializationResult>
    
    /**
     * Checks if the collections system has been initialized
     * @return true if initialized, false otherwise
     */
    suspend fun isInitialized(): Result<Boolean>
    
    /**
     * Forces re-initialization of the collections system
     * Useful for testing or recovery scenarios
     * @return Result indicating success or failure of re-initialization
     */
    suspend fun forceReinitialize(): Result<InitializationResult>
}

/**
 * Result of collections initialization
 */
data class InitializationResult(
    val defaultCollectionsCreated: List<GameCollection>,
    val wasAlreadyInitialized: Boolean,
    val migrationPerformed: Boolean = false,
    val initializationTimeMs: Long
)

class CollectionInitializationServiceImpl(
    private val initializeDefaultCollectionsUseCase: InitializeDefaultCollectionsUseCase
) : CollectionInitializationService {
    
    private val initializationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initializationMutex = Mutex()
    private var isInitializedFlag = false
    
    override suspend fun initializeOnAppStart(): Result<InitializationResult> {
        return initializationMutex.withLock {
            try {
                val startTime = System.currentTimeMillis()
                
                // Check if already initialized
                if (isInitializedFlag) {
                    return@withLock Result.success(
                        InitializationResult(
                            defaultCollectionsCreated = emptyList(),
                            wasAlreadyInitialized = true,
                            initializationTimeMs = System.currentTimeMillis() - startTime
                        )
                    )
                }
                
                // Check if default collections already exist
                val checkResult = initializeDefaultCollectionsUseCase.areDefaultCollectionsInitialized()
                if (checkResult.isFailure) {
                    return@withLock Result.failure(
                        checkResult.exceptionOrNull() ?: CollectionError.UnknownError("Failed to check initialization status")
                    )
                }
                
                val alreadyInitialized = checkResult.getOrThrow()
                val createdCollections = if (!alreadyInitialized) {
                    // Initialize default collections
                    val initResult = initializeDefaultCollectionsUseCase()
                    if (initResult.isFailure) {
                        return@withLock Result.failure(
                            initResult.exceptionOrNull() ?: CollectionError.UnknownError("Failed to initialize default collections")
                        )
                    }
                    initResult.getOrThrow()
                } else {
                    emptyList()
                }
                
                // Mark as initialized
                isInitializedFlag = true
                
                val endTime = System.currentTimeMillis()
                val result = InitializationResult(
                    defaultCollectionsCreated = createdCollections,
                    wasAlreadyInitialized = alreadyInitialized,
                    migrationPerformed = false, // Future: implement migration logic here
                    initializationTimeMs = endTime - startTime
                )
                
                Result.success(result)
                
            } catch (e: Exception) {
                Result.failure(
                    CollectionError.UnknownError(
                        "Unexpected error during collections initialization: ${e.message}",
                        e
                    )
                )
            }
        }
    }
    
    override suspend fun isInitialized(): Result<Boolean> {
        return try {
            // Check both the flag and the actual state in the database
            if (!isInitializedFlag) {
                Result.success(false)
            } else {
                // Double-check with the database
                initializeDefaultCollectionsUseCase.areDefaultCollectionsInitialized()
            }
        } catch (e: Exception) {
            Result.failure(
                CollectionError.UnknownError(
                    "Failed to check initialization status: ${e.message}",
                    e
                )
            )
        }
    }
    
    override suspend fun forceReinitialize(): Result<InitializationResult> {
        return initializationMutex.withLock {
            try {
                // Reset the flag
                isInitializedFlag = false
                
                // Perform initialization
                initializeOnAppStart()
                
            } catch (e: Exception) {
                Result.failure(
                    CollectionError.UnknownError(
                        "Failed to force re-initialization: ${e.message}",
                        e
                    )
                )
            }
        }
    }
    
    /**
     * Initializes collections asynchronously in the background
     * This is useful for non-blocking app startup
     */
    fun initializeAsync(onComplete: (Result<InitializationResult>) -> Unit = {}) {
        initializationScope.launch {
            val result = initializeOnAppStart()
            onComplete(result)
        }
    }
    
    /**
     * Performs any necessary migration logic for existing users
     * This method can be extended in the future to handle data migrations
     */
    private suspend fun performMigrationIfNeeded(): Result<Boolean> {
        return try {
            // Future: Add migration logic here
            // For now, just return success with no migration performed
            Result.success(false)
        } catch (e: Exception) {
            Result.failure(
                CollectionError.UnknownError(
                    "Migration failed: ${e.message}",
                    e
                )
            )
        }
    }
}