package com.devpush.features.gameDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.gameDetails.domain.repository.GameDetailsRepository
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

class GameDetailsViewModel(
    private val gameDetailsRepository: GameDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun getGameDetails(id: Int) {
        flow {
            emit(gameDetailsRepository.getDetails(id))
        }.catch { error ->
            emit(Result.failure(error))
        }.flowOn(Dispatchers.IO)
            .onStart {
                _uiState.update { GameDetailsUiState(isLoading = true) }
            }.onEach { result ->
                result.onSuccess { data ->
                    _uiState.update { GameDetailsUiState(data = data) }
                }.onFailure { error ->
                    _uiState.update { GameDetailsUiState(error = error.message.toString()) }
                }
            }.launchIn(viewModelScope)
    }

    fun save(id: Int, image: String, name: String) {
        flow {
            emit(gameDetailsRepository.save(id, image, name))
        }.onStart {
            _uiState.update { GameDetailsUiState(isLoading = true) }
        }.launchIn(viewModelScope)
    }

    fun delete(id: Int) {
        flow {
            emit(gameDetailsRepository.delete(id))
        }.onStart {
            _uiState.update { GameDetailsUiState(isLoading = true) }
        }.launchIn(viewModelScope)
    }

}