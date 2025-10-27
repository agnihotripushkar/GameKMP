package com.devpush.features.gameDetails.data.sharing

import com.devpush.features.gameDetails.domain.sharing.ShareManager

/**
 * Default implementation of ShareManager that provides basic sharing functionality
 * This can be replaced with platform-specific implementations
 */
class DefaultShareManager : ShareManager {
    
    override suspend fun shareGame(
        title: String,
        description: String,
        imageUrl: String?,
        additionalInfo: String?
    ): Result<Unit> {
        return try {
            val shareText = buildString {
                append("Check out this game: $title")
                
                if (description.isNotBlank()) {
                    append("\n\n$description")
                }
                
                additionalInfo?.let { info ->
                    if (info.isNotBlank()) {
                        append("\n\n$info")
                    }
                }
            }
            
            // For now, just log the share content
            // In a real implementation, this would trigger platform-specific sharing
            println("Sharing game content: $shareText")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}