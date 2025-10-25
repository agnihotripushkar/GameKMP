package com.devpush.features.common.preview

import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.bookmarklist.domain.collections.GameCollection
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.usecase.CollectionWithCount
import com.devpush.features.userRatingsReviews.domain.model.GameWithUserData
import com.devpush.features.userRatingsReviews.domain.model.UserRating
import com.devpush.features.userRatingsReviews.domain.model.UserReview
import com.devpush.features.common.utils.DateTimeUtils

/**
 * Centralized preview data provider for consistent UI previews across the application.
 * This object provides sample data for all major domain models used in Compose previews.
 */
object PreviewData {
    
    // Sample timestamps
    private val currentTime = DateTimeUtils.getCurrentTimestamp()
    private val oneDayAgo = currentTime - 86400000L // 24 hours ago
    private val oneWeekAgo = currentTime - 604800000L // 7 days ago
    private val oneMonthAgo = currentTime - 2592000000L // 30 days ago
    
    // Sample Games
    val sampleGame1 = Game(
        id = 1,
        name = "The Legend of Zelda: Breath of the Wild",
        imageUrl = "https://example.com/zelda.jpg",
        platforms = listOf("Nintendo Switch", "Wii U"),
        genres = listOf("Action", "Adventure", "Open World"),
        rating = 4.8,
        releaseDate = "2017-03-03"
    )
    
    val sampleGame2 = Game(
        id = 2,
        name = "Super Mario Odyssey",
        imageUrl = "https://example.com/mario.jpg",
        platforms = listOf("Nintendo Switch"),
        genres = listOf("Platformer", "Adventure"),
        rating = 4.7,
        releaseDate = "2017-10-27"
    )
    
    val sampleGame3 = Game(
        id = 3,
        name = "Cyberpunk 2077",
        imageUrl = "https://example.com/cyberpunk.jpg",
        platforms = listOf("PC", "PlayStation 5", "Xbox Series X"),
        genres = listOf("RPG", "Action", "Sci-Fi"),
        rating = 3.8,
        releaseDate = "2020-12-10"
    )
    
    val sampleGame4 = Game(
        id = 4,
        name = "Hades",
        imageUrl = "https://example.com/hades.jpg",
        platforms = listOf("PC", "Nintendo Switch", "PlayStation 4"),
        genres = listOf("Roguelike", "Action", "Indie"),
        rating = 4.9,
        releaseDate = "2020-09-17"
    )
    
    val sampleGame5 = Game(
        id = 5,
        name = "Among Us",
        imageUrl = "https://example.com/amongus.jpg",
        platforms = listOf("PC", "Mobile", "Nintendo Switch"),
        genres = listOf("Social Deduction", "Multiplayer"),
        rating = 4.2,
        releaseDate = "2018-06-15"
    )
    
    val sampleGames = listOf(sampleGame1, sampleGame2, sampleGame3, sampleGame4, sampleGame5)
    
    // Sample User Ratings
    val sampleRating1 = UserRating(
        gameId = 1,
        rating = 5,
        createdAt = oneWeekAgo,
        updatedAt = oneWeekAgo
    )
    
    val sampleRating2 = UserRating(
        gameId = 2,
        rating = 4,
        createdAt = oneMonthAgo,
        updatedAt = oneDayAgo
    )
    
    val sampleRating3 = UserRating(
        gameId = 3,
        rating = 3,
        createdAt = oneMonthAgo,
        updatedAt = oneMonthAgo
    )
    
    // Sample User Reviews
    val sampleReview1 = UserReview(
        gameId = 1,
        reviewText = "This game completely redefined what an open-world adventure could be. The freedom to explore, the physics-based puzzles, and the sheer beauty of Hyrule make this an unforgettable experience. Every mountain peak calls to be climbed, every shrine offers a unique challenge. A masterpiece that will be remembered for years to come.",
        createdAt = oneWeekAgo,
        updatedAt = oneWeekAgo
    )
    
    val sampleReview2 = UserReview(
        gameId = 2,
        reviewText = "Mario's latest adventure is pure joy in video game form. The creative level design, charming characters, and innovative mechanics make every moment delightful. Cappy adds a fantastic new dimension to gameplay.",
        createdAt = oneMonthAgo,
        updatedAt = oneDayAgo
    )
    
    val sampleReview3 = UserReview(
        gameId = 3,
        reviewText = "Despite its rocky launch, this game has incredible potential. The world is beautifully crafted and the story is engaging, but technical issues hold it back from greatness.",
        createdAt = oneMonthAgo,
        updatedAt = oneMonthAgo
    )
    
    val sampleReviewShort = UserReview(
        gameId = 4,
        reviewText = "Amazing game! Highly recommended.",
        createdAt = oneDayAgo,
        updatedAt = oneDayAgo
    )
    
    // Sample GameWithUserData
    val sampleGameWithUserData1 = GameWithUserData(
        game = sampleGame1,
        userRating = sampleRating1,
        userReview = sampleReview1
    )
    
    val sampleGameWithUserData2 = GameWithUserData(
        game = sampleGame2,
        userRating = sampleRating2,
        userReview = sampleReview2
    )
    
    val sampleGameWithUserData3 = GameWithUserData(
        game = sampleGame3,
        userRating = sampleRating3,
        userReview = sampleReview3
    )
    
    val sampleGameWithUserDataNoReview = GameWithUserData(
        game = sampleGame4,
        userRating = UserRating(
            gameId = 4,
            rating = 5,
            createdAt = oneDayAgo,
            updatedAt = oneDayAgo
        ),
        userReview = null
    )
    
    // Sample Collections
    val sampleWishlistCollection = GameCollection(
        id = "wishlist-1",
        name = "My Wishlist",
        type = CollectionType.WISHLIST,
        gameIds = listOf(1, 2, 3, 4, 5),
        createdAt = oneMonthAgo,
        updatedAt = oneDayAgo
    )
    
    val sampleCurrentlyPlayingCollection = GameCollection(
        id = "playing-1",
        name = "Currently Playing",
        type = CollectionType.CURRENTLY_PLAYING,
        gameIds = listOf(1, 3),
        createdAt = oneMonthAgo,
        updatedAt = oneWeekAgo
    )
    
    val sampleCompletedCollection = GameCollection(
        id = "completed-1",
        name = "Completed Games",
        type = CollectionType.COMPLETED,
        gameIds = listOf(2, 4),
        createdAt = oneMonthAgo,
        updatedAt = oneDayAgo
    )
    
    val sampleCustomCollection = GameCollection(
        id = "custom-1",
        name = "Indie Favorites",
        type = CollectionType.CUSTOM,
        gameIds = listOf(4, 5),
        createdAt = oneWeekAgo,
        updatedAt = oneWeekAgo
    )
    
    val sampleEmptyCollection = GameCollection(
        id = "empty-1",
        name = "Future Purchases",
        type = CollectionType.CUSTOM,
        gameIds = emptyList(),
        createdAt = oneDayAgo,
        updatedAt = oneDayAgo
    )
    
    // Sample CollectionWithCount
    val sampleCollectionWithCount1 = CollectionWithCount(
        collection = sampleWishlistCollection,
        gameCount = 5
    )
    
    val sampleCollectionWithCount2 = CollectionWithCount(
        collection = sampleCurrentlyPlayingCollection,
        gameCount = 2
    )
    
    val sampleCollectionWithCount3 = CollectionWithCount(
        collection = sampleCompletedCollection,
        gameCount = 2
    )
    
    val sampleCollectionWithCount4 = CollectionWithCount(
        collection = sampleCustomCollection,
        gameCount = 2
    )
    
    val sampleCollectionWithCountEmpty = CollectionWithCount(
        collection = sampleEmptyCollection,
        gameCount = 0
    )
    
    val sampleCollections = listOf(
        sampleCollectionWithCount1,
        sampleCollectionWithCount2,
        sampleCollectionWithCount3,
        sampleCollectionWithCount4
    )
    
    // Sample platforms and genres for filters
    val samplePlatforms = listOf(
        "PC", "PlayStation 5", "PlayStation 4", "Xbox Series X", "Xbox One", 
        "Nintendo Switch", "Nintendo 3DS", "Mobile", "Wii U"
    )
    
    val sampleGenres = listOf(
        "Action", "Adventure", "RPG", "Strategy", "Simulation", "Sports",
        "Racing", "Puzzle", "Platformer", "Fighting", "Shooter", "Horror",
        "Indie", "Roguelike", "Open World", "Sci-Fi", "Fantasy", "Multiplayer"
    )
    
    // Collection type mappings for games
    val sampleGameCollectionMap = mapOf(
        1 to listOf(CollectionType.WISHLIST, CollectionType.CURRENTLY_PLAYING),
        2 to listOf(CollectionType.COMPLETED),
        3 to listOf(CollectionType.WISHLIST, CollectionType.CURRENTLY_PLAYING),
        4 to listOf(CollectionType.COMPLETED, CollectionType.CUSTOM),
        5 to listOf(CollectionType.WISHLIST, CollectionType.CUSTOM)
    )
}