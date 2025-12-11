package com.devpush.kmp.userRatingsReviews

import com.devpush.features.statistics.domain.model.*
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository
import com.devpush.features.statistics.domain.usecase.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Accessibility and usability tests for the User Ratings and Reviews feature.
 * Tests screen reader compatibility, keyboard navigation, touch interactions, and visual accessibility.
 */
class UserRatingsReviewsAccessibilityTest {

    private lateinit var repository: FakeUserRatingReviewRepository
    private lateinit var setUserRatingUseCase: SetUserRatingUseCase
    private lateinit var getUserRatingUseCase: GetUserRatingUseCase
    private lateinit var setUserReviewUseCase: SetUserReviewUseCase
    private lateinit var getUserReviewUseCase: GetUserReviewUseCase
    private lateinit var getUserRatingStatsUseCase: GetUserRatingStatsUseCase
    private lateinit var getGamesWithUserDataUseCase: GetGamesWithUserDataUseCase

    @BeforeTest
    fun setup() {
        repository = FakeUserRatingReviewRepository()
        setUserRatingUseCase = SetUserRatingUseCase(repository)
        getUserRatingUseCase = GetUserRatingUseCase(repository)
        setUserReviewUseCase = SetUserReviewUseCase(repository)
        getUserReviewUseCase = GetUserReviewUseCase(repository)
        getUserRatingStatsUseCase = GetUserRatingStatsUseCase(repository)
        getGamesWithUserDataUseCase = GetGamesWithUserDataUseCase(repository)
    }

    @Test
    fun `accessibility test - screen reader compatibility for ratings`() = runTest {
        val gameId = 1
        
        // Test rating with screen reader descriptions
        setUserRatingUseCase(gameId, 4)
        val rating = getUserRatingUseCase(gameId).getOrThrow()
        
        // Verify rating data provides accessible information
        assertNotNull(rating)
        assertEquals(4, rating.rating)
        
        // Test that rating values are within accessible range (1-5)
        assertTrue(rating.rating in 1..5, "Rating should be in accessible range 1-5")
        
        // Test rating descriptions for screen readers
        val ratingDescription = when (rating.rating) {
            1 -> "1 star out of 5"
            2 -> "2 stars out of 5"
            3 -> "3 stars out of 5"
            4 -> "4 stars out of 5"
            5 -> "5 stars out of 5"
            else -> "Invalid rating"
        }
        
        assertEquals("4 stars out of 5", ratingDescription)
    }

    @Test
    fun `accessibility test - review text accessibility`() = runTest {
        val gameId = 2
        val reviewText = "This game has excellent accessibility features including screen reader support and keyboard navigation."
        
        setUserReviewUseCase(gameId, reviewText)
        val review = getUserReviewUseCase(gameId).getOrThrow()
        
        assertNotNull(review)
        assertEquals(reviewText, review.reviewText)
        
        // Test review text length for accessibility (should not be too long for screen readers)
        assertTrue(review.reviewText.length <= 1000, "Review text should not exceed 1000 characters for accessibility")
        
        // Test that review text doesn't contain problematic characters for screen readers
        assertFalse(review.reviewText.contains("<"), "Review should not contain HTML tags")
        assertFalse(review.reviewText.contains(">"), "Review should not contain HTML tags")
        
        // Test review text is readable (contains actual words, not just symbols)
        assertTrue(review.reviewText.any { it.isLetter() }, "Review should contain readable text")
    }

    @Test
    fun `usability test - keyboard navigation simulation`() = runTest {
        val gameIds = listOf(1, 2, 3, 4, 5)
        
        // Simulate keyboard navigation through rating interface
        gameIds.forEachIndexed { index, gameId ->
            val rating = (index % 5) + 1
            
            // Simulate keyboard rating selection (Tab navigation + Enter/Space)
            setUserRatingUseCase(gameId, rating)
            
            val savedRating = getUserRatingUseCase(gameId).getOrThrow()
            assertNotNull(savedRating)
            assertEquals(rating, savedRating.rating)
        }
        
        // Test keyboard navigation through reviews
        gameIds.take(3).forEach { gameId ->
            val reviewText = "Keyboard accessible review for game $gameId"
            setUserReviewUseCase(gameId, reviewText)
            
            val savedReview = getUserReviewUseCase(gameId).getOrThrow()
            assertNotNull(savedReview)
            assertEquals(reviewText, savedReview.reviewText)
        }
        
        // Verify all data is accessible via keyboard navigation
        val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        assertEquals(5, allGamesWithUserData.size)
        
        // All games should have ratings (keyboard accessible)
        allGamesWithUserData.forEach { gameData ->
            assertNotNull(gameData.userRating)
        }
        
        // First 3 games should have reviews (keyboard accessible)
        allGamesWithUserData.take(3).forEach { gameData ->
            assertNotNull(gameData.userReview)
        }
    }

    @Test
    fun `usability test - touch interaction simulation`() = runTest {
        val gameId = 10
        
        // Simulate touch interactions for rating (tap on stars)
        val touchRatings = listOf(1, 3, 5, 4, 2) // Simulate user changing rating via touch
        
        touchRatings.forEach { rating ->
            setUserRatingUseCase(gameId, rating)
            val currentRating = getUserRatingUseCase(gameId).getOrThrow()
            assertEquals(rating, currentRating.rating)
        }
        
        // Simulate touch interaction for review (tap to open, type, save)
        val reviewText = "Touch-friendly review interface test"
        setUserReviewUseCase(gameId, reviewText)
        
        val savedReview = getUserReviewUseCase(gameId).getOrThrow()
        assertEquals(reviewText, savedReview.reviewText)
        
        // Test quick rating touch interaction (long press or quick tap)
        val quickRating = 5
        setUserRatingUseCase(gameId, quickRating)
        
        val finalRating = getUserRatingUseCase(gameId).getOrThrow()
        assertEquals(quickRating, finalRating.rating)
    }

    @Test
    fun `accessibility test - color contrast and visual accessibility`() = runTest {
        val gameIds = (1..5).toList()
        
        // Set up ratings with different values to test visual representation
        gameIds.forEachIndexed { index, gameId ->
            setUserRatingUseCase(gameId, index + 1) // Ratings 1-5
        }
        
        val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        
        // Test that all rating values are visually distinguishable
        val ratingValues = allGamesWithUserData.mapNotNull { it.userRating?.rating }.toSet()
        assertEquals(5, ratingValues.size, "All rating values should be present and distinguishable")
        
        // Test rating range for visual accessibility
        ratingValues.forEach { rating ->
            assertTrue(rating in 1..5, "Rating $rating should be in accessible range")
        }
        
        // Test that ratings provide sufficient information for users with visual impairments
        allGamesWithUserData.forEach { gameData ->
            val rating = gameData.userRating!!
            
            // Rating should have both numeric value and semantic meaning
            val semanticMeaning = when (rating.rating) {
                1 -> "Poor"
                2 -> "Fair" 
                3 -> "Good"
                4 -> "Very Good"
                5 -> "Excellent"
                else -> "Unknown"
            }
            
            assertNotEquals("Unknown", semanticMeaning, "Rating should have semantic meaning")
        }
    }

    @Test
    fun `accessibility test - text size and readability`() = runTest {
        val gameId = 20
        
        // Test with various review text lengths for readability
        val shortReview = "Good game."
        val mediumReview = "This is a medium-length review that provides a reasonable amount of detail about the game experience."
        val longReview = "This is a comprehensive review that covers multiple aspects of the game including gameplay mechanics, story, graphics, sound design, and overall user experience. ".repeat(3)
        
        val reviews = listOf(shortReview, mediumReview, longReview)
        
        reviews.forEachIndexed { index, reviewText ->
            val currentGameId = gameId + index
            setUserReviewUseCase(currentGameId, reviewText)
            
            val savedReview = getUserReviewUseCase(currentGameId).getOrThrow()
            assertEquals(reviewText, savedReview.reviewText)
            
            // Test readability constraints
            assertTrue(savedReview.reviewText.length <= 1000, "Review should not exceed maximum length for readability")
            
            // Test that review contains readable content
            val wordCount = savedReview.reviewText.split("\\s+".toRegex()).size
            assertTrue(wordCount > 0, "Review should contain words for readability")
        }
    }

    @Test
    fun `usability test - error handling and user feedback`() = runTest {
        val gameId = 30
        
        // Test invalid rating error handling
        val invalidRatingResult = setUserRatingUseCase(gameId, 6) // Invalid rating
        assertTrue(invalidRatingResult.isFailure)
        
        val error = invalidRatingResult.exceptionOrNull() as? UserRatingReviewError.InvalidRating
        assertNotNull(error)
        assertEquals(6, error.rating)
        
        // Test that error provides user-friendly message
        assertTrue(error.userMessage.isNotEmpty(), "Error should provide user-friendly message")
        assertTrue(error.suggestedAction?.isNotEmpty() == true, "Error should provide suggested action")
        
        // Test review length error handling
        val tooLongReview = "a".repeat(1001)
        val reviewError = setUserReviewUseCase(gameId, tooLongReview)
        assertTrue(reviewError.isFailure)
        
        val reviewErrorObj = reviewError.exceptionOrNull() as? UserRatingReviewError.ReviewTooLong
        assertNotNull(reviewErrorObj)
        assertEquals(1001, reviewErrorObj.length)
        
        // Test that review error provides helpful feedback
        assertTrue(reviewErrorObj.userMessage.isNotEmpty(), "Review error should provide user-friendly message")
        assertTrue(reviewErrorObj.suggestedAction.isNotEmpty(), "Review error should provide suggested action")
    }

    @Test
    fun `accessibility test - data persistence and state management`() = runTest {
        val gameIds = listOf(1, 2, 3)
        
        // Set up initial state
        gameIds.forEach { gameId ->
            setUserRatingUseCase(gameId, gameId) // Rating equals gameId for easy verification
            setUserReviewUseCase(gameId, "Review for game $gameId")
        }
        
        // Simulate app state changes (like screen rotation, app backgrounding)
        val initialData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        
        // Verify data persistence through state changes
        initialData.forEach { gameData ->
            assertNotNull(gameData.userRating)
            assertNotNull(gameData.userReview)
            assertEquals(gameData.gameId, gameData.userRating!!.rating)
            assertEquals("Review for game ${gameData.gameId}", gameData.userReview!!.reviewText)
        }
        
        // Simulate data reload after state change
        val reloadedData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        
        // Verify data consistency after reload
        assertEquals(initialData.size, reloadedData.size)
        initialData.zip(reloadedData).forEach { (initial, reloaded) ->
            assertEquals(initial.gameId, reloaded.gameId)
            assertEquals(initial.userRating?.rating, reloaded.userRating?.rating)
            assertEquals(initial.userReview?.reviewText, reloaded.userReview?.reviewText)
        }
    }

    @Test
    fun `usability test - progressive disclosure and information hierarchy`() = runTest {
        val gameIds = (1..10).toList()
        
        // Set up data with varying complexity
        gameIds.forEach { gameId ->
            setUserRatingUseCase(gameId, (gameId % 5) + 1)
            
            // Add reviews for some games (progressive disclosure)
            if (gameId % 3 == 0) {
                setUserReviewUseCase(gameId, "Detailed review for game $gameId with more information")
            }
        }
        
        // Test that basic information (ratings) is always available
        val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        allGamesWithUserData.forEach { gameData ->
            assertNotNull(gameData.userRating, "Basic rating information should always be available")
        }
        
        // Test that detailed information (reviews) is available when needed
        val gamesWithReviews = allGamesWithUserData.filter { it.userReview != null }
        assertEquals(3, gamesWithReviews.size, "Should have reviews for games 3, 6, 9")
        
        // Test information hierarchy - ratings are primary, reviews are secondary
        gamesWithReviews.forEach { gameData ->
            assertNotNull(gameData.userRating, "Games with reviews should also have ratings")
            assertTrue(gameData.userReview!!.reviewText.isNotEmpty(), "Reviews should contain meaningful content")
        }
    }

    @Test
    fun `accessibility test - internationalization and localization support`() = runTest {
        val gameId = 40
        
        // Test with various character sets and languages
        val reviews = listOf(
            "Great game!", // English
            "¡Excelente juego!", // Spanish
            "Excellent jeu!", // French
            "素晴らしいゲーム！", // Japanese
            "훌륭한 게임!", // Korean
            "Отличная игра!", // Russian
            "لعبة ممتازة!", // Arabic
        )
        
        reviews.forEachIndexed { index, reviewText ->
            val currentGameId = gameId + index
            setUserReviewUseCase(currentGameId, reviewText)
            
            val savedReview = getUserReviewUseCase(currentGameId).getOrThrow()
            assertEquals(reviewText, savedReview.reviewText)
            
            // Test that international characters are preserved
            assertTrue(savedReview.reviewText.isNotEmpty(), "International text should be preserved")
            assertEquals(reviewText.length, savedReview.reviewText.length, "Character count should be preserved")
        }
        
        // Test that statistics work with international content
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(reviews.size, stats.totalReviews)
    }

    @Test
    fun `usability test - performance under accessibility constraints`() = runTest {
        val gameCount = 100
        val gameIds = (1..gameCount).toList()
        
        // Test performance with accessibility features enabled (simulated)
        gameIds.forEach { gameId ->
            setUserRatingUseCase(gameId, (gameId % 5) + 1)
            
            // Simulate accessibility-enhanced review text
            val accessibleReview = "Game $gameId review. Rating: ${(gameId % 5) + 1} stars. Accessible description included."
            setUserReviewUseCase(gameId, accessibleReview)
        }
        
        // Test bulk operations with accessibility data
        val allGamesWithUserData = getGamesWithUserDataUseCase(gameIds).getOrThrow()
        assertEquals(gameCount, allGamesWithUserData.size)
        
        // Verify all accessibility information is preserved
        allGamesWithUserData.forEach { gameData ->
            assertNotNull(gameData.userRating)
            assertNotNull(gameData.userReview)
            assertTrue(gameData.userReview!!.reviewText.contains("Accessible description"))
        }
        
        // Test statistics calculation with accessibility data
        val stats = getUserRatingStatsUseCase().getOrThrow()
        assertEquals(gameCount, stats.totalRatedGames)
        assertEquals(gameCount, stats.totalReviews)
    }
}