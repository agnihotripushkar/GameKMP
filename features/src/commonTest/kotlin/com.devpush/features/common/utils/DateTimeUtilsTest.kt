package com.devpush.features.common.utils

import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class DateTimeUtilsTest {
    
    @Test
    fun testFormatTimestamp() {
        // Test with a known timestamp (January 1, 2024)
        val timestamp = 1704067200000L // 2024-01-01 00:00:00 UTC
        val formatted = DateTimeUtils.formatTimestamp(timestamp)
        
        // Should contain the year 2024
        assertTrue(formatted.contains("2024"))
    }
    
    @Test
    fun testGetCurrentTimestamp() {
        val timestamp = DateTimeUtils.getCurrentTimestamp()
        val now = Clock.System.now().toEpochMilliseconds()
        
        // Should be within a reasonable range (1 second)
        assertTrue(kotlin.math.abs(timestamp - now) < 1000)
    }
    
    @Test
    fun testFormatRelativeDate() {
        val now = Clock.System.now().toEpochMilliseconds()
        
        // Test "Just now"
        val justNow = DateTimeUtils.formatRelativeDate(now)
        assertEquals("Just now", justNow)
        
        // Test "1 hour ago"
        val oneHourAgo = now - (60 * 60 * 1000) // 1 hour in milliseconds
        val oneHourResult = DateTimeUtils.formatRelativeDate(oneHourAgo)
        assertTrue(oneHourResult.contains("hour"))
        
        // Test "1 day ago"
        val oneDayAgo = now - (24 * 60 * 60 * 1000) // 1 day in milliseconds
        val oneDayResult = DateTimeUtils.formatRelativeDate(oneDayAgo)
        assertTrue(oneDayResult.contains("day"))
    }
    
    @Test
    fun testTimestampConversion() {
        val timestamp = 1704067200000L // 2024-01-01 00:00:00 UTC
        val localDateTime = DateTimeUtils.timestampToLocalDateTime(timestamp)
        val backToTimestamp = DateTimeUtils.localDateTimeToTimestamp(localDateTime)
        
        // Should be able to convert back and forth
        assertEquals(timestamp, backToTimestamp)
    }
}