package com.devpush.features.statistics.ui.statistics

import com.devpush.features.statistics.domain.model.UserRatingStats
import com.devpush.features.statistics.domain.repository.RecentActivity

/**
 * UI state for the statistics screen
 * Contains all data needed to display user rating and review statistics
 */
data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val stats: UserRatingStats? = null,
    val recentActivity: List<RecentActivity> = emptyList(),
    val isRecentActivityLoading: Boolean = false,
    val recentActivityError: String = ""
)