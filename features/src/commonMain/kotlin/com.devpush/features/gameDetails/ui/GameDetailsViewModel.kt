package com.devpush.features.gameDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.gameDetails.domain.repository.GameDetailsRepository
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCase
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.model.collections.CollectionType
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

class GameDetailsViewModel(
    private val gameDetailsRepository: GameDetailsRepository,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val addGameToCollectionUseCase: AddGameToCollectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailsUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        loadCollections()
    }

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
    
    // Collection-related methods
    
    private fun loadCollections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCollectionsLoading = true) }
            
            getCollectionsUseCase().fold(
                onSuccess = { collectionsWithCount ->
                    val collections = collectionsWithCount.map { it.collection }
                    val gameCollectionTypes = getCurrentGameCollectionTypes(collections)
                    
                    _uiState.update { 
                        it.copy(
                            collections = collections,
                            gameCollectionTypes = gameCollectionTypes,
                            isCollectionsLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(isCollectionsLoading = false)
                    }
                }
            )
        }
    }
    
    fun showAddToCollectionDialog() {
        _uiState.update { 
            it.copy(showAddToCollectionDialog = true)
        }
    }
    
    fun hideAddToCollectionDialog() {
        _uiState.update { 
            it.copy(showAddToCollectionDialog = false)
        }
    }
    
    fun addToCollection(collection: GameCollection) {
        val gameId = _uiState.value.data?.id ?: return
        
        viewModelScope.launch {
            addGameToCollectionUseCase(
                collectionId = collection.id,
                gameId = gameId,
                confirmTransition = true
            ).fold(
                onSuccess = {
                    // Refresh collections to update the UI
                    loadCollections()
                    hideAddToCollectionDialog()
                },
                onFailure = { error ->
                    // Handle error - could show a snackbar or error dialog
                    hideAddToCollectionDialog()
                }
            )
        }
    }
    
    fun quickAddToDefaultCollection(collectionType: CollectionType) {
        val gameId = _uiState.value.data?.id ?: return
        val targetCollection = _uiState.value.collections.find { it.type == collectionType } ?: return
        
        viewModelScope.launch {
            addGameToCollectionUseCase(
                collectionId = targetCollection.id,
                gameId = gameId,
                confirmTransition = true
            ).fold(
                onSuccess = {
                    // Refresh collections to update the UI
                    loadCollections()
                },
                onFailure = { error ->
                    // Handle error silently for quick actions
                }
            )
        }
    }
    
    private fun getCurrentGameCollectionTypes(collections: List<GameCollection>): List<CollectionType> {
        val gameId = _uiState.value.data?.id ?: return emptyList()
        
        return collections
            .filter { it.containsGame(gameId) }
            .map { it.type }
    }

}