package com.devpush.features.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.game.domain.repository.GameRepository
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

class GameViewModel(
    private val gameRepository: GameRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getGames()
    }

    fun getGames() {
        flow {
            emit(gameRepository.getGames())
        }.flowOn(Dispatchers.IO)
            .catch { error ->
                emit(Result.failure(error))
            }
            .onStart {
                _uiState.update { GameUiState(isLoading = true) }
            }
            .onEach { result ->
                result.onSuccess { data ->
                    _uiState.update { GameUiState(games = data) }
                }.onFailure { error ->
                    _uiState.update { GameUiState(error = error.message.toString()) }
                }
            }
            .launchIn(viewModelScope)
    }

}