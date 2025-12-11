package com.devpush.kmp.userRatingsReviews

import com.devpush.features.statistics.domain.model.*
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository
import com.devpush.features.statistics.domain.usecase.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Integration tests for the User Ratings and Reviews feature.
 * Tests complete integration between all components and real-world user scenarios.
 */
class UserRatingsReviewsIntegrationTest {

    private lateinit var repository: FakeUserRatingReviewRepository
    private lateinit var setUserRatingUseCase: SetUserRatingUseCase
    private lateinit var getUserRatingUseCase: GetUserRatingUseCase
    private lateinit var deleteUserRatingUseCase: DeleteUserRatingUseCase
    private lateinit var setUserReviewUseCase: SetUserReviewUseCase
    private lateinit var getUserReviewUseCase: GetUserReviewUseCase
    private lateinit var deleteUserReviewUseCase: DeleteUserReviewUseCase
    private lateinit var getUserRatingStatsUseCase: GetUserRatingStatsUseCase
    private lateinit var getGameWithUserDataUseCase: GetGameWithUserDataUseCase
    private lateinit var getGamesWithUserDataUseCase: GetGamesWithUserDataUseCase
    private lateinit var getRecentUserActivityUseCase: GetRecentUserActivityUseCase

    @BeforeTest
    fun setup() {
        repository = FakeUserRatingReviewRepository()
        setUserRatingUseCase = SetUserRatingUseCase(repository)
        getUserRatingUseCase = GetUserRatingUseCase(repository)
        deleteUserRatingUseCase = DeleteUserRatingUseCase(repository)
        setUserReviewUseCase = SetUserReviewUseCase(repository)
        getUserReviewUseCase = GetUserReviewUseCase(repository)
        deleteUserReviewUseCase = DeleteUserReviewUseCase(repository)
        getUserRatingStatsUseCase = GetUserRatingStatsUseCase(repository)
        getGameWithUserDataUseCase = GetGameWithUserDataUseCase(repository)
        getGamesWithUserDataUseCase = GetGamesWithUserDataUseCase(repository)
        getRecentUserActivityUseCase = GetRecentUserActivityUseCase(repository)
    }

    @Test
    fun `integration test - complete game discovery to rating workflow`() = runTest {
        // Scenario: User discovers a new game and wants to rate it
        val gameId = 1
        
        // Step 1: User discovers game (simulated - game exists in system)
        // Initially no user data exists
        val initialGameData = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertNotNull(initialGameData)
        assertNull(initialGameData.userRating)
        assertNull(initialGameData.userReview)
        
        // Step 2: User decides to rate the game after playing
        val initialRating = 4
        val ratingResult = setUserRatingUseCase(gameId, initialRating)
        assertTrue(ratingResult.isSuccess)
        
        // Step 3: Verify rating appears in game data
        val gameDataWithRating = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertNotNull(gameDataWithRating.userRating)
        assertEquals(initialRating, gameDataWithRating.userRating!!.rating)
        assertNull(gameDataWithRating.userReview) // Still no review
        
        // Step 4: User changes their mind and updates rating
        val updatedRating = 5
        val updateResult = setUserRatingUseCase(gameId, updatedRating)
        assertTrue(updateResult.isSuccess)
        
        // Step 5: Verify updated rating
        val gameDataWithUpdatedRating = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertEquals(updatedRating, gameDataWithUpdatedRating.userRating!!.rating)
        
        // Step 6: Verify rating appears in statistics
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(1, stats.totalRatedGames)
        assertEquals(0, stats.totalReviews)
        assertEquals(5.0, stats.averageRating)
        assertEquals(1, stats.ratingDistribution[5])
        
        // Step 7: Verify rating appears in recent activity
        val recentActivity = getRecentUserActivityUseCase(10).getOrThrow()
        assertTrue(recentActivity.isNotEmpty())
        val ratingActivity = recentActivity.find { it.gameId == gameId }
        assertNotNull(ratingActivity)
        assertEquals(updatedRating, ratingActivity.rating)
    }

    @Test
    fun `integration test - complete review writing workflow`() = runTest {
        // Scenario: User wants to write a detailed review for a game they've played
        val gameId = 2
        
        // Step 1: User starts with just a rating
        setUserRatingUseCase(gameId, 4)
        
        // Step 2: User decides to write a review
        val initialReview = "This game has excellent gameplay mechanics and a compelling story. The graphics are stunning and the sound design is top-notch."
        val reviewResult = setUserReviewUseCase(gameId, initialReview)
        assertTrue(reviewResult.isSuccess)
        
        // Step 3: Verify review is saved with rating
        val gameDataWithReview = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertNotNull(gameDataWithReview.userRating)
        assertNotNull(gameDataWithReview.userReview)
        assertEquals(4, gameDataWithReview.userRating!!.rating)
        assertEquals(initialReview, gameDataWithReview.userReview!!.reviewText)
        
        // Step 4: User edits their review to add more details
        val updatedReview = "$initialReview However, I did encounter some minor bugs that affected the experience. Overall, still a great game that I would recommend."
        val updateReviewResult = setUserReviewUseCase(gameId, updatedReview)
        assertTrue(updateReviewResult.isSuccess)
        
        // Step 5: Verify updated review
        val gameDataWithUpdatedReview = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertEquals(updatedReview, gameDataWithUpdatedReview.userReview!!.reviewText)
        assertTrue(gameDataWithUpdatedReview.userReview!!.updatedAt > gameDataWithUpdatedReview.userReview!!.createdAt)
        
        // Step 6: Verify review appears in statistics
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(1, stats.totalRatedGames)
        assertEquals(1, stats.totalReviews)
        
        // Step 7: Verify review appears in recent activity
        val recentActivity = getRecentUserActivityUseCase(10).getOrThrow()
        val reviewActivity = recentActivity.find { it.gameId == gameId && it.reviewText != null }
        assertNotNull(reviewActivity)
        assertTrue(reviewActivity.reviewText!!.contains("excellent gameplay"))
    }

    @Test
    fun `integration test - collection management with ratings and reviews`() = runTest {
        // Scenario: User manages their game collection with ratings and reviews
        val collectionGameIds = listOf(1, 2, 3, 4, 5)
        
        // Step 1: User rates all games in their collection
        collectionGameIds.forEachIndexed { index, gameId ->
            val rating = (index % 5) + 1 // Ratings 1-5
            setUserRatingUseCase(gameId, rating)
        }
        
        // Step 2: User writes reviews for their favorite games (4+ stars)
        val favoriteGames = listOf(1, 5) // Games with 5-star ratings
        favoriteGames.forEach { gameId ->
            setUserReviewUseCase(gameId, "This is one of my favorite games! Highly recommended.")
        }
        
        // Step 3: User wants to view their collection with ratings and reviews
        val collectionWithUserData = getGamesWithUserDataUseCase(collectionGameIds).getOrThrow()
        assertEquals(5, collectionWithUserData.size)
        
        // Step 4: Verify all games have ratings
        collectionWithUserData.forEach { gameData ->
            assertNotNull(gameData.userRating)
            assertTrue(gameData.userRating!!.rating in 1..5)
        }
        
        // Step 5: Verify favorite games have reviews
        val gamesWithReviews = collectionWithUserData.filter { it.userReview != null }
        assertEquals(2, gamesWithReviews.size)
        gamesWithReviews.forEach { gameData ->
            assertTrue(gameData.gameId in favoriteGames)
            assertEquals(5, gameData.userRating!!.rating)
            assertTrue(gameData.userReview!!.reviewText.contains("favorite"))
        }
        
        // Step 6: User filters collection by high ratings (4+ stars)
        val highRatedGames = collectionWithUserData.filter { it.userRating!!.rating >= 4 }
        assertEquals(2, highRatedGames.size) // Games with ratings 4 and 5
        
        // Step 7: User sorts collection by rating (highest first)
        val sortedByRating = collectionWithUserData.sortedByDescending { it.userRating!!.rating }
        assertEquals(5, sortedByRating.first().userRating!!.rating)
        assertEquals(1, sortedByRating.last().userRating!!.rating)
        
        // Step 8: Verify collection statistics
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(5, stats.totalRatedGames)
        assertEquals(2, stats.totalReviews)
        assertEquals(3.0, stats.averageRating) // (1+2+3+4+5)/5 = 3.0
    }

    @Test
    fun `integration test - user statistics and insights workflow`() = runTest {
        // Scenario: User wants to see insights about their gaming preferences
        val gameIds = (1..20).toList()
        
        // Step 1: User rates games over time with realistic distribution
        val ratingDistribution = mapOf(
            5 to 4, // 4 excellent games
            4 to 6, // 6 very good games  
            3 to 5, // 5 good games
            2 to 3, // 3 fair games
            1 to 2  // 2 poor games
        )
        
        var gameIndex = 0
        ratingDistribution.forEach { (rating, count) ->
            repeat(count) {
                setUserRatingUseCase(gameIds[gameIndex], rating)
                gameIndex++
            }
        }
        
        // Step 2: User writes reviews for highly rated games
        val highlyRatedGameIds = gameIds.take(10) // First 10 games (ratings 4-5)
        highlyRatedGameIds.forEach { gameId ->
            val rating = getUserRatingUseCase(gameId).getOrThrow()!!.rating
            val reviewText = when (rating) {
                5 -> "Absolutely amazing game! Perfect in every way."
                4 -> "Really great game with minor flaws."
                else -> "Good game overall."
            }
            setUserReviewUseCase(gameId, reviewText)
        }
        
        // Step 3: User views their statistics
        val stats = getUserRatingStatsUseCase().getOrThrow()
        
        // Verify total counts
        assertEquals(20, stats.totalRatedGames)
        assertEquals(10, stats.totalReviews)
        
        // Verify average rating
        val expectedAverage = (5*4 + 4*6 + 3*5 + 2*3 + 1*2) / 20.0 // 3.15
        assertEquals(expectedAverage, stats.averageRating, 0.01)
        
        // Verify rating distribution
        assertEquals(2, stats.ratingDistribution[1])
        assertEquals(3, stats.ratingDistribution[2])
        assertEquals(5, stats.ratingDistribution[3])
        assertEquals(6, stats.ratingDistribution[4])
        assertEquals(4, stats.ratingDistribution[5])
        
        // Step 4: User views recent activity
        val recentActivity = getRecentUserActivityUseCase(15).getOrThrow()
        assertTrue(recentActivity.size <= 15)
        
        // Verify activity contains both ratings and reviews
        val ratingActivities = recentActivity.filter { it.rating != null }
        val reviewActivities = recentActivity.filter { it.reviewText != null }
        assertTrue(ratingActivities.isNotEmpty())
        assertTrue(reviewActivities.isNotEmpty())
        
        // Step 5: User analyzes their preferences
        // Most common rating should be 4 (6 games)
        val mostCommonRating = stats.ratingDistribution.maxByOrNull { it.value }?.key
        assertEquals(4, mostCommonRating)
        
        // User has high standards (average > 3.0)
        assertTrue(stats.averageRating > 3.0, "User has high gaming standards")
        
        // User writes reviews for half their games
        assertEquals(0.5, stats.totalReviews.toDouble() / stats.totalRatedGames, 0.01)
    }

    @Test
    fun `integration test - data migration and persistence workflow`() = runTest {
        // Scenario: User data needs to persist through app updates and migrations
        val gameIds = listOf(1, 2, 3, 4, 5)
        
        // Step 1: User creates initial data (simulating older app version)
        gameIds.forEach { gameId ->
            setUserRatingUseCase(gameId, gameId) // Rating equals gameId for easy verification
            setUserReviewUseCase(gameId, "Original review for game $gameId")
        }
        
        // Step 2: Capture initial state
        val initialData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        val initialStats = getUserRatingStatsUseCase().getOrThrow()
        
        // Step 3: Simulate app update/migration by creating new repository instance
        val migratedRepository = FakeUserRatingReviewRepository()
        migratedRepository.copyDataFrom(repository)
        
        // Create new use cases with migrated repository
        val migratedGetGamesUseCase = GetGamesWithUserDataUseCase(migratedRepository)
        val migratedGetStatsUseCase = GetUserRatingStatsUseCase(migratedRepository)
        val migratedSetRatingUseCase = SetUserRatingUseCase(migratedRepository)
        val migratedSetReviewUseCase = SetUserReviewUseCase(migratedRepository)
        
        // Step 4: Verify data persisted through migration
        val migratedData = migratedGetGamesUseCase(gameIds).getOrThrow()
        val migratedStats = migratedGetStatsUseCase().getOrThrow()
        
        assertEquals(initialData.size, migratedData.size)
        assertEquals(initialStats.totalRatedGames, migratedStats.totalRatedGames)
        assertEquals(initialStats.totalReviews, migratedStats.totalReviews)
        assertEquals(initialStats.averageRating, migratedStats.averageRating)
        
        // Step 5: Verify individual game data integrity
        initialData.zip(migratedData).forEach { (initial, migrated) ->
            assertEquals(initial.gameId, migrated.gameId)
            assertEquals(initial.userRating?.rating, migrated.userRating?.rating)
            assertEquals(initial.userReview?.reviewText, migrated.userReview?.reviewText)
            assertEquals(initial.userRating?.createdAt, migrated.userRating?.createdAt)
            assertEquals(initial.userReview?.createdAt, migrated.userReview?.createdAt)
        }
        
        // Step 6: Verify new operations work after migration
        val newGameId = 10
        migratedSetRatingUseCase(newGameId, 5)
        migratedSetReviewUseCase(newGameId, "Post-migration review")
        
        val newGameData = migratedGetGamesUseCase(listOf(newGameId)).getOrThrow().first()
        assertEquals(5, newGameData.userRating!!.rating)
        assertEquals("Post-migration review", newGameData.userReview!!.reviewText)
        
        // Step 7: Verify updated statistics
        val finalStats = migratedGetStatsUseCase().getOrThrow()
        assertEquals(6, finalStats.totalRatedGames)
        assertEquals(6, finalStats.totalReviews)
    }

    @Test
    fun `integration test - error recovery and data consistency workflow`() = runTest {
        // Scenario: System encounters errors but maintains data consistency
        val gameId = 100
        
        // Step 1: User successfully rates a game
        setUserRatingUseCase(gameId, 4)
        val initialRating = getUserRatingUseCase(gameId).getOrThrow()
        assertNotNull(initialRating)
        
        // Step 2: User attempts invalid operations
        val invalidRatingResult = setUserRatingUseCase(gameId, 10) // Invalid rating
        assertTrue(invalidRatingResult.isFailure)
        
        // Step 3: Verify original data is unchanged after error
        val ratingAfterError = getUserRatingUseCase(gameId).getOrThrow()
        assertEquals(initialRating.rating, ratingAfterError.rating)
        assertEquals(initialRating.createdAt, ratingAfterError.createdAt)
        
        // Step 4: User attempts invalid review
        val invalidReview = "a".repeat(1001) // Too long
        val invalidReviewResult = setUserReviewUseCase(gameId, invalidReview)
        assertTrue(invalidReviewResult.isFailure)
        
        // Step 5: Verify no review was created due to error
        val reviewAfterError = getUserReviewUseCase(gameId).getOrNull()
        assertNull(reviewAfterError)
        
        // Step 6: User provides valid review after error
        val validReview = "This is a valid review after the error."
        val validReviewResult = setUserReviewUseCase(gameId, validReview)
        assertTrue(validReviewResult.isSuccess)
        
        // Step 7: Verify system recovered and data is consistent
        val finalGameData = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertEquals(4, finalGameData.userRating!!.rating)
        assertEquals(validReview, finalGameData.userReview!!.reviewText)
        
        // Step 8: Verify statistics are consistent
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(1, stats.totalRatedGames)
        assertEquals(1, stats.totalReviews)
    }

    @Test
    fun `integration test - concurrent user operations workflow`() = runTest {
        // Scenario: User performs multiple operations rapidly (like changing rating quickly)
        val gameId = 200
        val operations = 20
        
        // Step 1: User rapidly changes rating (simulating indecisive user)
        repeat(operations) { i ->
            val rating = (i % 5) + 1
            setUserRatingUseCase(gameId, rating)
        }
        
        // Step 2: Verify final state is consistent
        val finalRating = getUserRatingUseCase(gameId).getOrThrow()
        assertNotNull(finalRating)
        assertTrue(finalRating.rating in 1..5)
        
        // Step 3: User rapidly updates review
        repeat(operations) { i ->
            setUserReviewUseCase(gameId, "Review iteration $i with more content")
        }
        
        // Step 4: Verify final review state
        val finalReview = getUserReviewUseCase(gameId).getOrThrow()
        assertNotNull(finalReview)
        assertTrue(finalReview.reviewText.contains("iteration"))
        assertTrue(finalReview.updatedAt >= finalReview.createdAt)
        
        // Step 5: Verify statistics reflect final state (not intermediate states)
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(1, stats.totalRatedGames)
        assertEquals(1, stats.totalReviews)
        
        // Step 6: Verify game data consistency
        val gameData = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertEquals(finalRating.rating, gameData.userRating!!.rating)
        assertEquals(finalReview.reviewText, gameData.userReview!!.reviewText)
    }

    @Test
    fun `integration test - complete user journey from discovery to insights`() = runTest {
        // Scenario: Complete user journey over time
        val discoveredGames = (1..50).toList()
        
        // Phase 1: User discovers and rates games over time
        discoveredGames.take(20).forEachIndexed { index, gameId ->
            val rating = when {
                index < 5 -> 5 // First 5 games are excellent
                index < 10 -> 4 // Next 5 are very good
                index < 15 -> 3 // Next 5 are good
                index < 18 -> 2 // Next 3 are fair
                else -> 1 // Last 2 are poor
            }
            setUserRatingUseCase(gameId, rating)
        }
        
        // Phase 2: User writes reviews for memorable games (very good and excellent)
        discoveredGames.take(10).forEach { gameId ->
            val rating = getUserRatingUseCase(gameId).getOrThrow()!!.rating
            val reviewText = when (rating) {
                5 -> "Absolutely incredible! This game exceeded all my expectations."
                4 -> "Really solid game with great mechanics and story."
                else -> "Decent game but nothing special."
            }
            setUserReviewUseCase(gameId, reviewText)
        }
        
        // Phase 3: User continues discovering more games
        discoveredGames.drop(20).take(15).forEach { gameId ->
            val rating = (gameId % 5) + 1 // Random distribution
            setUserRatingUseCase(gameId, rating)
        }
        
        // Phase 4: User reviews their gaming journey
        val finalStats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(35, finalStats.totalRatedGames)
        assertEquals(10, finalStats.totalReviews)
        
        // Phase 5: User analyzes their collection
        val allGamesWithUserData = getGamesWithUserDataUseCase(discoveredGames.take(35)).getOrThrow()
        
        // Find highly rated games
        val excellentGames = allGamesWithUserData.filter { it.userRating!!.rating == 5 }
        assertTrue(excellentGames.size >= 5, "Should have excellent games")
        
        // Find games with reviews
        val reviewedGames = allGamesWithUserData.filter { it.userReview != null }
        assertEquals(10, reviewedGames.size)
        
        // Phase 6: User checks recent activity
        val recentActivity = getRecentUserActivityUseCase(20).getOrThrow()
        assertTrue(recentActivity.isNotEmpty())
        assertTrue(recentActivity.size <= 20)
        
        // Verify activity is sorted by recency
        if (recentActivity.size > 1) {
            for (i in 0 until recentActivity.size - 1) {
                assertTrue(recentActivity[i].activityDate >= recentActivity[i + 1].activityDate)
            }
        }
        
        // Phase 7: User reflects on their gaming preferences
        val averageRating = finalStats.averageRating
        val hasHighStandards = averageRating > 3.0
        val reviewsToRatingsRatio = finalStats.totalReviews.toDouble() / finalStats.totalRatedGames
        
        // User insights
        assertTrue(finalStats.totalRatedGames > 30, "User is an active gamer")
        assertTrue(reviewsToRatingsRatio > 0.2, "User writes reviews for memorable games")
        
        println("User gaming journey complete:")
        println("- Total games rated: ${finalStats.totalRatedGames}")
        println("- Total reviews written: ${finalStats.totalReviews}")
        println("- Average rating: ${finalStats.averageRating}")
        println("- Has high standards: $hasHighStandards")
        println("- Review ratio: ${(reviewsToRatingsRatio * 100).toInt()}%")
    }
}