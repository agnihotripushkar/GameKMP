package com.devpush.kmp.userRatingsReviews

import com.devpush.features.statistics.domain.model.*
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository
import com.devpush.features.statistics.domain.repository.RecentActivity
import com.devpush.features.statistics.domain.usecase.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * End-to-end tests for the User Ratings and Reviews feature.
 * Tests complete user workflows from game discovery to rating and reviewing.
 */
class UserRatingsReviewsEndToEndTest {

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
    fun `complete rating workflow - from game discovery to rating`() = runTest {
        val gameId = 1

        // Step 1: User discovers a game and wants to rate it
        // Initially, no rating should exist
        val initialRating = getUserRatingUseCase(gameId).getOrNull()
        assertNull(initialRating)

        // Step 2: User sets initial rating
        val result = setUserRatingUseCase(gameId, 4)
        assertTrue(result.isSuccess)

        // Step 3: Verify rating is saved and retrievable
        val savedRating = getUserRatingUseCase(gameId).getOrThrow()
        assertNotNull(savedRating)
        assertEquals(gameId, savedRating.gameId)
        assertEquals(4, savedRating.rating)
        assertTrue(savedRating.createdAt > 0)
        assertTrue(savedRating.updatedAt > 0)

        // Step 4: User changes their mind and updates rating
        val updateResult = setUserRatingUseCase(gameId, 5)
        assertTrue(updateResult.isSuccess)

        // Step 5: Verify rating is updated
        val updatedRating = getUserRatingUseCase(gameId).getOrThrow()
        assertEquals(5, updatedRating.rating)
        assertTrue(updatedRating.updatedAt > updatedRating.createdAt)

        // Step 6: User decides to remove rating
        val deleteResult = deleteUserRatingUseCase(gameId)
        assertTrue(deleteResult.isSuccess)

        // Step 7: Verify rating is deleted
        val deletedRating = getUserRatingUseCase(gameId).getOrNull()
        assertNull(deletedRating)
    }

    @Test
    fun `complete review workflow - from writing to editing and deleting`() = runTest {
        val gameId = 2
        val initialReviewText = "This is an amazing game! I really enjoyed the storyline and graphics."

        // Step 1: User wants to write a review
        // Initially, no review should exist
        val initialReview = getUserReviewUseCase(gameId).getOrNull()
        assertNull(initialReview)

        // Step 2: User writes initial review
        val result = setUserReviewUseCase(gameId, initialReviewText)
        assertTrue(result.isSuccess)

        // Step 3: Verify review is saved and retrievable
        val savedReview = getUserReviewUseCase(gameId).getOrThrow()
        assertNotNull(savedReview)
        assertEquals(gameId, savedReview.gameId)
        assertEquals(initialReviewText, savedReview.reviewText)
        assertTrue(savedReview.createdAt > 0)
        assertTrue(savedReview.updatedAt > 0)

        // Step 4: User edits their review
        val updatedReviewText = "This is an amazing game! I really enjoyed the storyline, graphics, and the soundtrack was incredible."
        val updateResult = setUserReviewUseCase(gameId, updatedReviewText)
        assertTrue(updateResult.isSuccess)

        // Step 5: Verify review is updated
        val updatedReview = getUserReviewUseCase(gameId).getOrThrow()
        assertEquals(updatedReviewText, updatedReview.reviewText)
        assertTrue(updatedReview.updatedAt > updatedReview.createdAt)

        // Step 6: User decides to delete review
        val deleteResult = deleteUserReviewUseCase(gameId)
        assertTrue(deleteResult.isSuccess)

        // Step 7: Verify review is deleted
        val deletedReview = getUserReviewUseCase(gameId).getOrNull()
        assertNull(deletedReview)
    }

    @Test
    fun `integration between ratings, reviews, and collections workflow`() = runTest {
        val gameIds = listOf(1, 2, 3, 4, 5)

        // Step 1: User rates multiple games
        setUserRatingUseCase(gameIds[0], 5)
        setUserRatingUseCase(gameIds[1], 4)
        setUserRatingUseCase(gameIds[2], 3)
        setUserRatingUseCase(gameIds[3], 4)
        setUserRatingUseCase(gameIds[4], 5)

        // Step 2: User writes reviews for some games
        setUserReviewUseCase(gameIds[0], "Excellent game, highly recommended!")
        setUserReviewUseCase(gameIds[2], "Good game but has some issues.")
        setUserReviewUseCase(gameIds[4], "Perfect game, no complaints!")

        // Step 3: Get combined data for collection view
        val gamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        assertEquals(5, gamesWithUserData.size)

        // Step 4: Verify games with both ratings and reviews
        val gameWithBoth = gamesWithUserData.find { it.gameId == gameIds[0] }
        assertNotNull(gameWithBoth)
        assertNotNull(gameWithBoth.userRating)
        assertNotNull(gameWithBoth.userReview)
        assertEquals(5, gameWithBoth.userRating!!.rating)
        assertEquals("Excellent game, highly recommended!", gameWithBoth.userReview!!.reviewText)

        // Step 5: Verify games with only ratings
        val gameWithOnlyRating = gamesWithUserData.find { it.gameId == gameIds[1] }
        assertNotNull(gameWithOnlyRating)
        assertNotNull(gameWithOnlyRating.userRating)
        assertNull(gameWithOnlyRating.userReview)
        assertEquals(4, gameWithOnlyRating.userRating!!.rating)

        // Step 6: Get user statistics
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(5, stats.totalRatedGames)
        assertEquals(3, stats.totalReviews)
        assertEquals(4.2, stats.averageRating, 0.1)
        assertEquals(2, stats.ratingDistribution[5]) // Two 5-star ratings
        assertEquals(2, stats.ratingDistribution[4]) // Two 4-star ratings
        assertEquals(1, stats.ratingDistribution[3]) // One 3-star rating

        // Step 7: Get recent activity
        val recentActivity = getRecentUserActivityUseCase(10).getOrThrow()
        assertTrue(recentActivity.isNotEmpty())
        assertTrue(recentActivity.size <= 10)
    }

    @Test
    fun `error handling and recovery scenarios`() = runTest {
        val gameId = 10

        // Test invalid rating values
        val invalidRatingResult = setUserRatingUseCase(gameId, 6) // Invalid rating > 5
        assertTrue(invalidRatingResult.isFailure)
        assertTrue(invalidRatingResult.exceptionOrNull() is UserRatingReviewError.InvalidRating)

        val negativeRatingResult = setUserRatingUseCase(gameId, 0) // Invalid rating < 1
        assertTrue(negativeRatingResult.isFailure)
        assertTrue(negativeRatingResult.exceptionOrNull() is UserRatingReviewError.InvalidRating)

        // Test review text too long
        val longReviewText = "a".repeat(1001) // Exceeds 1000 character limit
        val longReviewResult = setUserReviewUseCase(gameId, longReviewText)
        assertTrue(longReviewResult.isFailure)
        assertTrue(longReviewResult.exceptionOrNull() is UserRatingReviewError.ReviewTooLong)

        // Test empty review text
        val emptyReviewResult = setUserReviewUseCase(gameId, "")
        assertTrue(emptyReviewResult.isFailure)
        assertTrue(emptyReviewResult.exceptionOrNull() is UserRatingReviewError.EmptyReview)

        // Test deleting non-existent rating
        val deleteNonExistentRating = deleteUserRatingUseCase(999)
        assertTrue(deleteNonExistentRating.isSuccess) // Should not fail, just no-op

        // Test deleting non-existent review
        val deleteNonExistentReview = deleteUserReviewUseCase(999)
        assertTrue(deleteNonExistentReview.isSuccess) // Should not fail, just no-op

        // Test getting non-existent data
        val nonExistentRating = getUserRatingUseCase(999).getOrNull()
        assertNull(nonExistentRating)

        val nonExistentReview = getUserReviewUseCase(999).getOrNull()
        assertNull(nonExistentReview)

        // Test recovery after error - valid operations should still work
        val validRatingResult = setUserRatingUseCase(gameId, 4)
        assertTrue(validRatingResult.isSuccess)

        val validReviewResult = setUserReviewUseCase(gameId, "This is a valid review.")
        assertTrue(validReviewResult.isSuccess)
    }

    @Test
    fun `concurrent operations and data consistency`() = runTest {
        val gameId = 20

        // Simulate rapid rating changes
        setUserRatingUseCase(gameId, 1)
        setUserRatingUseCase(gameId, 2)
        setUserRatingUseCase(gameId, 3)
        setUserRatingUseCase(gameId, 4)
        setUserRatingUseCase(gameId, 5)

        // Final rating should be 5
        val finalRating = getUserRatingUseCase(gameId).getOrThrow()
        assertEquals(5, finalRating.rating)

        // Simulate rapid review changes
        setUserReviewUseCase(gameId, "First review")
        setUserReviewUseCase(gameId, "Second review")
        setUserReviewUseCase(gameId, "Final review")

        // Final review should be the last one
        val finalReview = getUserReviewUseCase(gameId).getOrThrow()
        assertEquals("Final review", finalReview.reviewText)

        // Test concurrent rating and review operations
        setUserRatingUseCase(gameId, 3)
        setUserReviewUseCase(gameId, "Updated review")

        val gameWithUserData = getGameWithUserDataUseCase(gameId).getOrThrow()
        assertNotNull(gameWithUserData.userRating)
        assertNotNull(gameWithUserData.userReview)
        assertEquals(3, gameWithUserData.userRating!!.rating)
        assertEquals("Updated review", gameWithUserData.userReview!!.reviewText)
    }

    @Test
    fun `bulk operations and performance validation`() = runTest {
        val gameIds = (1..100).toList()

        // Bulk rate games
        gameIds.forEach { gameId ->
            val rating = (gameId % 5) + 1 // Ratings from 1-5
            setUserRatingUseCase(gameId, rating)
        }

        // Bulk add reviews for some games
        gameIds.take(50).forEach { gameId ->
            setUserReviewUseCase(gameId, "Review for game $gameId")
        }

        // Test bulk retrieval
        val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        assertEquals(100, allGamesWithUserData.size)

        // Verify all games have ratings
        allGamesWithUserData.forEach { gameData ->
            assertNotNull(gameData.userRating)
            assertTrue(gameData.userRating!!.rating in 1..5)
        }

        // Verify first 50 games have reviews
        allGamesWithUserData.take(50).forEach { gameData ->
            assertNotNull(gameData.userReview)
            assertTrue(gameData.userReview!!.reviewText.isNotEmpty())
        }

        // Verify last 50 games don't have reviews
        allGamesWithUserData.drop(50).forEach { gameData ->
            assertNull(gameData.userReview)
        }

        // Test statistics with large dataset
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(100, stats.totalRatedGames)
        assertEquals(50, stats.totalReviews)
        assertTrue(stats.averageRating > 0)
        assertEquals(100, stats.ratingDistribution.values.sum())

        // Test recent activity with large dataset
        val recentActivity = getRecentUserActivityUseCase(20).getOrThrow()
        assertTrue(recentActivity.size <= 20)
    }

    @Test
    fun `data persistence and state management`() = runTest {
        val gameId = 30

        // Set initial data
        setUserRatingUseCase(gameId, 4)
        setUserReviewUseCase(gameId, "Initial review")

        // Verify data exists
        val initialRating = getUserRatingUseCase(gameId).getOrThrow()
        val initialReview = getUserReviewUseCase(gameId).getOrThrow()
        
        assertEquals(4, initialRating.rating)
        assertEquals("Initial review", initialReview.reviewText)

        // Simulate app restart by creating new repository instance
        val newRepository = FakeUserRatingReviewRepository()
        // Copy data to simulate persistence
        newRepository.copyDataFrom(repository)

        val newGetUserRatingUseCase = GetUserRatingUseCase(newRepository)
        val newGetUserReviewUseCase = GetUserReviewUseCase(newRepository)

        // Verify data persists after "restart"
        val persistedRating = newGetUserRatingUseCase(gameId).getOrThrow()
        val persistedReview = newGetUserReviewUseCase(gameId).getOrThrow()

        assertEquals(4, persistedRating.rating)
        assertEquals("Initial review", persistedReview.reviewText)
        assertEquals(initialRating.createdAt, persistedRating.createdAt)
        assertEquals(initialReview.createdAt, persistedReview.createdAt)
    }

    @Test
    fun `user workflow integration with filtering and sorting`() = runTest {
        // Create diverse rating data for filtering/sorting tests
        val testData = listOf(
            Triple(1, 5, "Excellent game!"),
            Triple(2, 4, "Very good game"),
            Triple(3, 3, null), // Rating only
            Triple(4, 5, "Another excellent game!"),
            Triple(5, 2, "Not great"),
            Triple(6, 4, null), // Rating only
            Triple(7, 1, "Terrible game"),
            Triple(8, 5, "Perfect!"),
            Triple(9, 3, "Average game"),
            Triple(10, 4, "Good game")
        )

        // Set up test data
        testData.forEach { (gameId, rating, review) ->
            setUserRatingUseCase(gameId, rating)
            review?.let { setUserReviewUseCase(gameId, it) }
        }

        // Test filtering by rating range (4+ stars)
        val allGames = getGamesWithUserDataUseCase(testData.map { it.first }).getOrThrow()
        val highRatedGames = allGames.filter { it.userRating?.rating ?: 0 >= 4 }
        assertEquals(5, highRatedGames.size)

        // Test filtering games with reviews
        val gamesWithReviews = allGames.filter { it.userReview != null }
        assertEquals(6, gamesWithReviews.size)

        // Test sorting by rating (high to low)
        val sortedByRatingDesc = allGames.sortedByDescending { it.userRating?.rating ?: 0 }
        assertEquals(5, sortedByRatingDesc.first().userRating?.rating)
        assertEquals(1, sortedByRatingDesc.last().userRating?.rating)

        // Test sorting by rating (low to high)
        val sortedByRatingAsc = allGames.sortedBy { it.userRating?.rating ?: 0 }
        assertEquals(1, sortedByRatingAsc.first().userRating?.rating)
        assertEquals(5, sortedByRatingAsc.last().userRating?.rating)

        // Verify statistics reflect the test data
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(10, stats.totalRatedGames)
        assertEquals(6, stats.totalReviews)
        
        // Verify rating distribution
        assertEquals(1, stats.ratingDistribution[1]) // One 1-star
        assertEquals(1, stats.ratingDistribution[2]) // One 2-star
        assertEquals(2, stats.ratingDistribution[3]) // Two 3-star
        assertEquals(3, stats.ratingDistribution[4]) // Three 4-star
        assertEquals(3, stats.ratingDistribution[5]) // Three 5-star
    }
}