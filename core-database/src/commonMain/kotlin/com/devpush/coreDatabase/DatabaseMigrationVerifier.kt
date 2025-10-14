package com.devpush.coreDatabase

import app.cash.sqldelight.db.SqlDriver

/**
 * Utility class to verify database migration and schema integrity
 */
class DatabaseMigrationVerifier(private val driver: SqlDriver) {
    
    /**
     * Verifies that all required tables exist and have the correct structure
     */
    fun verifyDatabaseSchema(): Boolean {
        return try {
            val database = AppDatabase(driver)
            
            // Simple verification by trying to access the tables
            // If tables don't exist, this will throw an exception
            database.appDatabaseQueries.getUserRatingCount().executeAsOne()
            database.appDatabaseQueries.getUserReviewCount().executeAsOne()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Tests basic CRUD operations to ensure constraints work properly
     */
    fun testBasicOperations(): Boolean {
        return try {
            val database = AppDatabase(driver)
            val currentTime = 1000000L // Use a fixed timestamp for testing
            
            // Test inserting a valid rating (should succeed)
            database.appDatabaseQueries.insertUserRating(
                game_id = 999L, // Use a test game ID that likely doesn't exist
                rating = 5L,
                created_at = currentTime,
                updated_at = currentTime
            )
            
            // Test inserting a valid review (should succeed)
            database.appDatabaseQueries.insertUserReview(
                game_id = 999L,
                review_text = "Great game!",
                created_at = currentTime,
                updated_at = currentTime
            )
            
            // Verify data was inserted
            val rating = database.appDatabaseQueries.getUserRating(999L).executeAsOneOrNull()
            val review = database.appDatabaseQueries.getUserReview(999L).executeAsOneOrNull()
            
            // Clean up test data
            database.appDatabaseQueries.deleteUserRating(999L)
            database.appDatabaseQueries.deleteUserReview(999L)
            
            rating != null && review != null
        } catch (e: Exception) {
            false
        }
    }
}