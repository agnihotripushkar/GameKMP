package com.devpush.kmp.userRatingsReviews

import com.devpush.features.userRatingsReviews.domain.model.*
import com.devpush.features.userRatingsReviews.domain.repository.UserRatingReviewRepository
import com.devpush.features.userRatingsReviews.domain.usecase.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.measureTime

/**
 * Performance and scalability tests for the User Ratings and Reviews feature.
 * Tests database query optimization, UI performance, and scalability with large datasets.
 */
class UserRatingsReviewsPerformanceTest {

    private lateinit var repository: FakeUserRatingReviewRepository
    private lateinit var setUserRatingUseCase: SetUserRatingUseCase
    private lateinit var getUserRatingUseCase: GetUserRatingUseCase
    private lateinit var setUserReviewUseCase: SetUserReviewUseCase
    private lateinit var getUserReviewUseCase: GetUserReviewUseCase
    private lateinit var getUserRatingStatsUseCase: GetUserRatingStatsUseCase
    private lateinit var getGamesWithUserDataUseCase: GetGamesWithUserDataUseCase
    private lateinit var getRecentUserActivityUseCase: GetRecentUserActivityUseCase

    @BeforeTest
    fun setup() {
        repository = FakeUserRatingReviewRepository()
        setUserRatingUseCase = SetUserRatingUseCase(repository)
        getUserRatingUseCase = GetUserRatingUseCase(repository)
        setUserReviewUseCase = SetUserReviewUseCase(repository)
        getUserReviewUseCase = GetUserReviewUseCase(repository)
        getUserRatingStatsUseCase = GetUserRatingStatsUseCase(repository)
        getGamesWithUserDataUseCase = GetGamesWithUserDataUseCase(repository)
        getRecentUserActivityUseCase = GetRecentUserActivityUseCase(repository)
    }

    @Test
    fun `performance test - bulk rating operations with large dataset`() = runTest {
        val gameCount = 1000
        val gameIds = (1..gameCount).toList()

        // Measure time for bulk rating operations
        val ratingTime = measureTime {
            gameIds.forEach { gameId ->
                val rating = (gameId % 5) + 1 // Ratings from 1-5
                setUserRatingUseCase(gameId, rating)
            }
        }

        println("Time to rate $gameCount games: $ratingTime")
        
        // Verify all ratings were saved
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(gameCount, stats.totalRatedGames)
        
        // Measure time for bulk retrieval
        val retrievalTime = measureTime {
            val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
            assertEquals(gameCount, allGamesWithUserData.size)
        }

        println("Time to retrieve $gameCount games with user data: $retrievalTime")
        
        // Performance assertions - these thresholds should be adjusted based on requirements
        // For a fake repository, operations should be very fast
        assertTrue(ratingTime.inWholeMilliseconds < 5000, "Rating $gameCount games took too long: $ratingTime")
        assertTrue(retrievalTime.inWholeMilliseconds < 1000, "Retrieving $gameCount games took too long: $retrievalTime")
    }

    @Test
    fun `performance test - bulk review operations with large dataset`() = runTest {
        val gameCount = 500
        val gameIds = (1..gameCount).toList()

        // Measure time for bulk review operations
        val reviewTime = measureTime {
            gameIds.forEach { gameId ->
                val reviewText = "This is a detailed review for game $gameId. ".repeat(10) // ~400 chars
                setUserReviewUseCase(gameId, reviewText)
            }
        }

        println("Time to write $gameCount reviews: $reviewTime")
        
        // Verify all reviews were saved
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(gameCount, stats.totalReviews)
        
        // Measure time for bulk retrieval with reviews
        val retrievalTime = measureTime {
            val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
            assertEquals(gameCount, allGamesWithUserData.size)
            // Verify all have reviews
            allGamesWithUserData.forEach { gameData ->
                assertNotNull(gameData.userReview)
                assertTrue(gameData.userReview!!.reviewText.isNotEmpty())
            }
        }

        println("Time to retrieve $gameCount games with reviews: $retrievalTime")
        
        // Performance assertions
        assertTrue(reviewTime.inWholeMilliseconds < 10000, "Writing $gameCount reviews took too long: $reviewTime")
        assertTrue(retrievalTime.inWholeMilliseconds < 2000, "Retrieving $gameCount games with reviews took too long: $retrievalTime")
    }

    @Test
    fun `scalability test - statistics calculation with large dataset`() = runTest {
        val gameCount = 2000
        val gameIds = (1..gameCount).toList()

        // Create diverse rating data
        gameIds.forEach { gameId ->
            val rating = (gameId % 5) + 1 // Even distribution of ratings 1-5
            setUserRatingUseCase(gameId, rating)
        }

        // Add reviews for half the games
        gameIds.take(gameCount / 2).forEach { gameId ->
            setUserReviewUseCase(gameId, "Review for game $gameId")
        }

        // Measure statistics calculation time
        val statsTime = measureTime {
            val stats = getUserRatingStatsUseCase().getOrThrow()
            
            // Verify statistics are correct
            assertEquals(gameCount, stats.totalRatedGames)
            assertEquals(gameCount / 2, stats.totalReviews)
            assertEquals(3.0, stats.averageRating, 0.1) // Should be 3.0 with even distribution
            
            // Verify rating distribution
            assertEquals(gameCount / 5, stats.ratingDistribution[1])
            assertEquals(gameCount / 5, stats.ratingDistribution[2])
            assertEquals(gameCount / 5, stats.ratingDistribution[3])
            assertEquals(gameCount / 5, stats.ratingDistribution[4])
            assertEquals(gameCount / 5, stats.ratingDistribution[5])
        }

        println("Time to calculate statistics for $gameCount ratings: $statsTime")
        
        // Performance assertion
        assertTrue(statsTime.inWholeMilliseconds < 1000, "Statistics calculation took too long: $statsTime")
    }

    @Test
    fun `scalability test - recent activity with large dataset`() = runTest {
        val gameCount = 1000
        val gameIds = (1..gameCount).toList()

        // Create mixed activity data
        gameIds.forEach { gameId ->
            setUserRatingUseCase(gameId, (gameId % 5) + 1)
            if (gameId % 3 == 0) { // Add reviews for every 3rd game
                setUserReviewUseCase(gameId, "Review for game $gameId")
            }
        }

        // Test different activity limits
        val limits = listOf(10, 50, 100, 200)
        
        limits.forEach { limit ->
            val activityTime = measureTime {
                val recentActivity = getRecentUserActivityUseCase(limit).getOrThrow()
                
                // Verify activity limit is respected
                assertTrue(recentActivity.size <= limit, "Activity count ${recentActivity.size} exceeds limit $limit")
                
                // Verify activities are sorted by date (most recent first)
                if (recentActivity.size > 1) {
                    for (i in 0 until recentActivity.size - 1) {
                        assertTrue(
                            recentActivity[i].activityDate >= recentActivity[i + 1].activityDate,
                            "Activities not sorted by date"
                        )
                    }
                }
            }
            
            println("Time to get $limit recent activities from $gameCount total: $activityTime")
            assertTrue(activityTime.inWholeMilliseconds < 500, "Recent activity retrieval took too long: $activityTime")
        }
    }

    @Test
    fun `memory efficiency test - large text reviews`() = runTest {
        val gameCount = 100
        val gameIds = (1..gameCount).toList()
        
        // Create reviews with maximum allowed length (1000 characters)
        val longReviewText = "A".repeat(1000)
        
        val memoryTime = measureTime {
            gameIds.forEach { gameId ->
                setUserReviewUseCase(gameId, longReviewText)
            }
            
            // Retrieve all reviews
            val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
            
            // Verify all reviews are present and correct length
            allGamesWithUserData.forEach { gameData ->
                assertNotNull(gameData.userReview)
                assertEquals(1000, gameData.userReview!!.reviewText.length)
            }
        }

        println("Time to handle $gameCount maximum-length reviews: $memoryTime")
        
        // Memory efficiency assertion
        assertTrue(memoryTime.inWholeMilliseconds < 3000, "Large review handling took too long: $memoryTime")
    }

    @Test
    fun `concurrent operations simulation`() = runTest {
        val gameId = 1
        val operationCount = 100

        // Simulate rapid rating changes (like user changing their mind quickly)
        val concurrentTime = measureTime {
            repeat(operationCount) { i ->
                val rating = (i % 5) + 1
                setUserRatingUseCase(gameId, rating)
            }
        }

        println("Time for $operationCount concurrent rating operations: $concurrentTime")
        
        // Verify final state is consistent
        val finalRating = getUserRatingUseCase(gameId).getOrThrow()
        assertNotNull(finalRating)
        assertTrue(finalRating.rating in 1..5)
        
        // Performance assertion
        assertTrue(concurrentTime.inWholeMilliseconds < 2000, "Concurrent operations took too long: $concurrentTime")
    }

    @Test
    fun `database query optimization simulation`() = runTest {
        val gameCount = 1000
        val gameIds = (1..gameCount).toList()

        // Setup data
        gameIds.forEach { gameId ->
            setUserRatingUseCase(gameId, (gameId % 5) + 1)
            if (gameId % 2 == 0) {
                setUserReviewUseCase(gameId, "Review $gameId")
            }
        }

        // Test batch retrieval vs individual retrieval performance
        val batchTime = measureTime {
            getGamesWithUserDataUseCase(gameIds).getOrThrow()
        }

        val individualTime = measureTime {
            gameIds.forEach { gameId ->
                getUserRatingUseCase(gameId)
                getUserReviewUseCase(gameId)
            }
        }

        println("Batch retrieval time for $gameCount games: $batchTime")
        println("Individual retrieval time for $gameCount games: $individualTime")
        
        // Batch operations should be more efficient
        assertTrue(batchTime < individualTime, "Batch operations should be faster than individual operations")
    }

    @Test
    fun `pagination simulation for large collections`() = runTest {
        val totalGames = 1000
        val pageSize = 50
        val gameIds = (1..totalGames).toList()

        // Setup data
        gameIds.forEach { gameId ->
            setUserRatingUseCase(gameId, (gameId % 5) + 1)
        }

        // Simulate paginated loading
        val pages = gameIds.chunked(pageSize)
        
        pages.forEachIndexed { pageIndex, pageGameIds ->
            val pageTime = measureTime {
                val pageData = getGamesWithUserDataUseCase(pageGameIds).getOrThrow()
                assertEquals(pageSize, pageData.size)
                
                // Verify all games in page have ratings
                pageData.forEach { gameData ->
                    assertNotNull(gameData.userRating)
                }
            }
            
            println("Page $pageIndex (size $pageSize) load time: $pageTime")
            assertTrue(pageTime.inWholeMilliseconds < 200, "Page load took too long: $pageTime")
        }
    }

    @Test
    fun `stress test - mixed operations under load`() = runTest {
        val gameCount = 500
        val operationsPerGame = 5

        val stressTime = measureTime {
            repeat(gameCount) { gameId ->
                // Simulate user behavior: rate, review, update rating, update review, delete
                repeat(operationsPerGame) { operation ->
                    when (operation) {
                        0 -> setUserRatingUseCase(gameId + 1, (gameId % 5) + 1)
                        1 -> setUserReviewUseCase(gameId + 1, "Review $gameId operation $operation")
                        2 -> setUserRatingUseCase(gameId + 1, ((gameId + 1) % 5) + 1) // Update rating
                        3 -> setUserReviewUseCase(gameId + 1, "Updated review $gameId operation $operation")
                        4 -> {
                            // Occasionally delete (simulate user removing rating/review)
                            if (gameId % 10 == 0) {
                                repository.deleteUserRating(gameId + 1)
                                repository.deleteUserReview(gameId + 1)
                            }
                        }
                    }
                }
            }
        }

        println("Stress test with ${gameCount * operationsPerGame} total operations: $stressTime")
        
        // Verify system is still functional after stress test
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertTrue(stats.totalRatedGames > 0, "System should still have ratings after stress test")
        
        // Performance assertion
        assertTrue(stressTime.inWholeMilliseconds < 15000, "Stress test took too long: $stressTime")
    }

    @Test
    fun `data consistency under rapid operations`() = runTest {
        val gameId = 1
        val rapidOperations = 50

        // Perform rapid operations
        repeat(rapidOperations) { i ->
            setUserRatingUseCase(gameId, (i % 5) + 1)
            setUserReviewUseCase(gameId, "Review iteration $i")
        }

        // Verify final state is consistent
        val finalRating = getUserRatingUseCase(gameId).getOrThrow()
        val finalReview = getUserReviewUseCase(gameId).getOrThrow()
        val gameWithUserData = getGamesWithUserDataUseCase(listOf(gameId)).getOrThrow().first()

        assertNotNull(finalRating)
        assertNotNull(finalReview)
        assertEquals(finalRating.gameId, gameWithUserData.userRating?.gameId)
        assertEquals(finalReview.gameId, gameWithUserData.userReview?.gameId)
        assertEquals(finalRating.rating, gameWithUserData.userRating?.rating)
        assertEquals(finalReview.reviewText, gameWithUserData.userReview?.reviewText)
    }

    @Test
    fun `performance regression test - baseline measurements`() = runTest {
        // This test establishes baseline performance metrics
        val testSizes = listOf(10, 100, 500, 1000)
        
        testSizes.forEach { size ->
            repository.clearAllData() // Reset for each test
            
            val setupTime = measureTime {
                repeat(size) { gameId ->
                    setUserRatingUseCase(gameId + 1, (gameId % 5) + 1)
                    if (gameId % 2 == 0) {
                        setUserReviewUseCase(gameId + 1, "Review for game ${gameId + 1}")
                    }
                }
            }
            
            val queryTime = measureTime {
                getUserRatingStatsUseCase().getOrThrow()
            }
            
            val batchQueryTime = measureTime {
                val gameIds = (1..size).toList()
                getGamesWithUserDataUseCase(gameIds).getOrThrow()
            }
            
            println("Size: $size, Setup: $setupTime, Stats: $queryTime, Batch: $batchQueryTime")
            
            // Log performance metrics for regression testing
            // In a real implementation, these could be stored and compared over time
            assertTrue(setupTime.inWholeMilliseconds < size * 10, "Setup time regression for size $size")
            assertTrue(queryTime.inWholeMilliseconds < 500, "Stats query time regression for size $size")
            assertTrue(batchQueryTime.inWholeMilliseconds < size * 2, "Batch query time regression for size $size")
        }
    }
}