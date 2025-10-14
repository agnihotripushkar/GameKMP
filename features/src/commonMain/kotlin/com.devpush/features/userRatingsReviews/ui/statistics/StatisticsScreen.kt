package com.devpush.features.userRatingsReviews.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devpush.features.userRatingsReviews.ui.statistics.components.RatingDistributionChart
import com.devpush.features.userRatingsReviews.ui.statistics.components.RecentActivityList
import com.devpush.features.userRatingsReviews.ui.statistics.components.StatisticsCard
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * Statistics screen showing user rating and review insights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<StatisticsViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages
    LaunchedEffect(uiState.error) {
        if (uiState.error.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.error)
            viewModel.clearErrors()
        }
    }

    LaunchedEffect(uiState.recentActivityError) {
        if (uiState.recentActivityError.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.recentActivityError)
            viewModel.clearErrors()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Statistics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.stats != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        // Overview cards
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    val stats = uiState.stats
                                    StatisticsCard(
                                        title = "Games Rated",
                                        value = stats?.totalRatedGames?.toString() ?: "0",
                                        icon = Icons.Default.Star,
                                        subtitle = if ((stats?.totalRatedGames ?: 0) > 0) {
                                            "Keep rating games!"
                                        } else {
                                            "Start rating games"
                                        },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                                
                                item {
                                    val stats = uiState.stats
                                    StatisticsCard(
                                        title = "Reviews Written",
                                        value = stats?.totalReviews?.toString() ?: "0",
                                        icon = Icons.Default.RateReview,
                                        subtitle = if ((stats?.totalReviews ?: 0) > 0) {
                                            "Great insights!"
                                        } else {
                                            "Share your thoughts"
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                                
                                item {
                                    val stats = uiState.stats
                                    StatisticsCard(
                                        title = "Average Rating",
                                        value = if ((stats?.totalRatedGames ?: 0) > 0) {
                                            "${((stats?.averageRating ?: 0.0) * 10).roundToInt() / 10.0}"
                                        } else {
                                            "0.0"
                                        },
                                        icon = Icons.Default.TrendingUp,
                                        subtitle = if ((stats?.totalRatedGames ?: 0) > 0) {
                                            "out of 5.0 stars"
                                        } else {
                                            "Rate some games first"
                                        },
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        // Rating distribution chart
                        item {
                            val stats = uiState.stats
                            if (stats != null) {
                                RatingDistributionChart(
                                    stats = stats,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        // Recent activity
                        item {
                            if (uiState.isRecentActivityLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                RecentActivityList(
                                    activities = uiState.recentActivity,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                else -> {
                    // Empty state or error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "No statistics available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Start rating and reviewing games to see your statistics here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}