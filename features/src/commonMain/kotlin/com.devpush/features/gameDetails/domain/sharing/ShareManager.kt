package com.devpush.features.gameDetails.domain.sharing

/**
 * Platform-agnostic interface for sharing game content
 */
interface ShareManager {
    /**
     * Share game information using the platform's native sharing mechanism
     * @param title The title of the game
     * @param description The game description
     * @param imageUrl Optional image URL to share
     * @param additionalInfo Any additional information to include
     */
    suspend fun shareGame(
        title: String,
        description: String,
        imageUrl: String? = null,
        additionalInfo: String? = null
    ): Result<Unit>
}

/**
 * Data class representing shareable game content
 */
data class ShareableGameContent(
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val platforms: List<String> = emptyList(),
    val additionalInfo: String? = null
) {
    fun toShareText(): String = buildString {
        append("Check out this game: $title")
        
        if (description.isNotBlank()) {
            append("\n\n$description")
        }
        
        if (platforms.isNotEmpty()) {
            append("\n\nAvailable on: ${platforms.joinToString(", ")}")
        }
        
        additionalInfo?.let { info ->
            if (info.isNotBlank()) {
                append("\n\n$info")
            }
        }
    }
}