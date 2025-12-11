package com.devpush.features.statistics.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.statistics.domain.usecase.GetUserRatingStatsUseCase
import com.devpush.features.statistics.domain.usecase.GetRecentUserActivityUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the statistics screen
 * Manages state for user rating statistics and recent activity
 */
class StatisticsViewModel(
    private val getUserRatingStatsUseCase: GetUserRatingStatsUseCase,
    private val getRecentUserActivityUseCase: GetRecentUserActivityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStatistics()
        loadRecentActivity()
    }

    /**
     * Loads user rating statistics
     */
    fun loadStatistics() {
        flow {
            emit(getUserRatingStatsUseCase())
        }.catch { error ->
            emit(Result.failure(error))
        }.onStart {
            _uiState.update { it.copy(isLoading = true, error = "") }
        }.onEach { result ->
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { stats ->
                        currentState.copy(
                            isLoading = false,
                            error = "",
                            stats = stats
                        )
                    },
                    onFailure = { error ->
                        currentState.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load statistics"
                        )
                    }
                )
            }
        }.flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    /**
     * Loads recent user activity
     */
    fun loadRecentActivity(limit: Int = 10) {
        flow {
            emit(getRecentUserActivityUseCase(limit))
        }.catch { error ->
            emit(Result.failure(error))
        }.onStart {
            _uiState.update { it.copy(isRecentActivityLoading = true, recentActivityError = "") }
        }.onEach { result ->
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { activities ->
                        currentState.copy(
                            isRecentActivityLoading = false,
                            recentActivityError = "",
                            recentActivity = activities
                        )
                    },
                    onFailure = { error ->
                        currentState.copy(
                            isRecentActivityLoading = false,
                            recentActivityError = error.message ?: "Failed to load recent activity"
                        )
                    }
                )
            }
        }.flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    /**
     * Refreshes all data
     */
    fun refresh() {
        loadStatistics()
        loadRecentActivity()
    }

    /**
     * Clears error states
     */
    fun clearErrors() {
        _uiState.update { 
            it.copy(
                error = "",
                recentActivityError = ""
            )
        }
    }
}