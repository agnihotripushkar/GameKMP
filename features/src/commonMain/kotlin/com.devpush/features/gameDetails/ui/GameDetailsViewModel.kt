package com.devpush.features.gameDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.gameDetails.domain.repository.GameDetailsRepository
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCase
import com.devpush.features.bookmarklist.domain.collections.GameCollection
import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.userRatingsReviews.domain.usecase.GetGameWithUserDataUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.SetUserRatingUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.SetUserReviewUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.DeleteUserRatingUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.DeleteUserReviewUseCase
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
    private val addGameToCollectionUseCase: AddGameToCollectionUseCase,
    private val getGameWithUserDataUseCase: GetGameWithUserDataUseCase,
    private val setUserRatingUseCase: SetUserRatingUseCase,
    private val setUserReviewUseCase: SetUserReviewUseCase,
    private val deleteUserRatingUseCase: DeleteUserRatingUseCase,
    private val deleteUserReviewUseCase: DeleteUserReviewUseCase
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
                _uiState.update { it.copy(isLoading = true) }
            }.onEach { result ->
                result.onSuccess { data ->
                    _uiState.update { it.copy(data = data, isLoading = false) }
                    // Load user data after game details are loaded
                    loadUserData(id)
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message.toString(), isLoading = false) }
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
    
    // User Rating and Review methods
    
    private fun loadUserData(gameId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUserDataLoading = true) }
            
            getGameWithUserDataUseCase(gameId).fold(
                onSuccess = { gameWithUserData ->
                    _uiState.update { 
                        it.copy(
                            userRating = gameWithUserData?.userRating,
                            userReview = gameWithUserData?.userReview,
                            isUserDataLoading = false,
                            userDataError = ""
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isUserDataLoading = false,
                            userDataError = error.message ?: "Failed to load user data"
                        )
                    }
                }
            )
        }
    }
    
    fun setUserRating(rating: Int) {
        val gameId = _uiState.value.data?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isRatingLoading = true) }
            
            setUserRatingUseCase(gameId, rating).fold(
                onSuccess = {
                    // Reload user data to get updated rating
                    loadUserData(gameId)
                    _uiState.update { it.copy(isRatingLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isRatingLoading = false,
                            userDataError = error.message ?: "Failed to save rating"
                        )
                    }
                }
            )
        }
    }
    
    fun showReviewDialog() {
        _uiState.update { it.copy(showReviewDialog = true) }
    }
    
    fun hideReviewDialog() {
        _uiState.update { it.copy(showReviewDialog = false) }
    }
    
    fun saveUserReview(reviewText: String) {
        val gameId = _uiState.value.data?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewLoading = true) }
            
            setUserReviewUseCase(gameId, reviewText).fold(
                onSuccess = {
                    // Reload user data to get updated review
                    loadUserData(gameId)
                    _uiState.update { 
                        it.copy(
                            isReviewLoading = false,
                            showReviewDialog = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isReviewLoading = false,
                            userDataError = error.message ?: "Failed to save review"
                        )
                    }
                }
            )
        }
    }
    
    fun deleteUserReview() {
        val gameId = _uiState.value.data?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewLoading = true) }
            
            deleteUserReviewUseCase(gameId).fold(
                onSuccess = {
                    // Reload user data to reflect deletion
                    loadUserData(gameId)
                    _uiState.update { it.copy(isReviewLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isReviewLoading = false,
                            userDataError = error.message ?: "Failed to delete review"
                        )
                    }
                }
            )
        }
    }

}