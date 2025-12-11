package com.devpush.features.gameDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.gameDetails.domain.repository.GameDetailsRepository
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCase
import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.statistics.domain.usecase.GetGameWithUserDataUseCase
import com.devpush.features.statistics.domain.usecase.SetUserRatingUseCase
import com.devpush.features.statistics.domain.usecase.SetUserReviewUseCase
import com.devpush.features.statistics.domain.usecase.DeleteUserRatingUseCase
import com.devpush.features.statistics.domain.usecase.DeleteUserReviewUseCase
import com.devpush.features.gameDetails.domain.sharing.ShareManager
import com.devpush.features.gameDetails.domain.sharing.ShareableGameContent
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
    private val deleteUserReviewUseCase: DeleteUserReviewUseCase,
    private val shareManager: ShareManager
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
                onFailure = { _ ->
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
                    
                    // Provide feedback if action was triggered from FAB
                    if (_uiState.value.fabMenuState.lastActionPerformed == FABAction.ADD_TO_COLLECTION) {
                        _uiState.update { 
                            it.copy(fabActionFeedback = "Added to ${collection.name}!")
                        }
                    }
                },
                onFailure = { _ ->
                    // Handle error - could show a snackbar or error dialog
                    hideAddToCollectionDialog()
                    
                    // Provide error feedback if action was triggered from FAB
                    if (_uiState.value.fabMenuState.lastActionPerformed == FABAction.ADD_TO_COLLECTION) {
                        _uiState.update { 
                            it.copy(fabActionFeedback = "Failed to add to collection")
                        }
                    }
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
                onFailure = { _ ->
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
                    
                    // Provide feedback if action was triggered from FAB
                    if (_uiState.value.fabMenuState.lastActionPerformed == FABAction.RATE_GAME) {
                        _uiState.update { 
                            it.copy(fabActionFeedback = "Rating saved: $rating stars!")
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isRatingLoading = false,
                            userDataError = error.message ?: "Failed to save rating"
                        )
                    }
                    
                    // Provide error feedback if action was triggered from FAB
                    if (_uiState.value.fabMenuState.lastActionPerformed == FABAction.RATE_GAME) {
                        _uiState.update { 
                            it.copy(fabActionFeedback = "Failed to save rating")
                        }
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
                    
                    // Provide feedback if action was triggered from FAB
                    if (_uiState.value.fabMenuState.lastActionPerformed == FABAction.WRITE_REVIEW) {
                        _uiState.update { 
                            it.copy(fabActionFeedback = "Review saved successfully!")
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isReviewLoading = false,
                            userDataError = error.message ?: "Failed to save review"
                        )
                    }
                    
                    // Provide error feedback if action was triggered from FAB
                    if (_uiState.value.fabMenuState.lastActionPerformed == FABAction.WRITE_REVIEW) {
                        _uiState.update { 
                            it.copy(fabActionFeedback = "Failed to save review")
                        }
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
    
    // FAB Menu state management
    
    fun setFABMenuExpanded(expanded: Boolean) {
        _uiState.update { 
            it.copy(
                isFABMenuExpanded = expanded,
                fabMenuState = it.fabMenuState.copy(
                    isExpanded = expanded,
                    isAnimating = true
                )
            )
        }
    }
    
    fun setFABMenuAnimating(animating: Boolean) {
        _uiState.update { 
            it.copy(
                fabMenuState = it.fabMenuState.copy(isAnimating = animating)
            )
        }
    }
    
    fun updateFABMenuExpandProgress(progress: Float) {
        _uiState.update { 
            it.copy(
                fabMenuState = it.fabMenuState.copy(expandProgress = progress)
            )
        }
    }
    
    fun clearFABActionFeedback() {
        _uiState.update { it.copy(fabActionFeedback = "") }
    }
    
    // FAB Action handlers with feedback
    
    fun handleFABAddToCollection() {
        _uiState.update { 
            it.copy(
                fabMenuState = it.fabMenuState.copy(lastActionPerformed = FABAction.ADD_TO_COLLECTION),
                fabActionFeedback = "Opening collection dialog..."
            )
        }
        showAddToCollectionDialog()
        setFABMenuExpanded(false)
    }
    
    fun handleFABRateGame() {
        _uiState.update { 
            it.copy(
                fabMenuState = it.fabMenuState.copy(lastActionPerformed = FABAction.RATE_GAME),
                fabActionFeedback = "Focusing on rating..."
            )
        }
        focusOnRating()
        setFABMenuExpanded(false)
    }
    
    fun handleFABWriteReview() {
        _uiState.update { 
            it.copy(
                fabMenuState = it.fabMenuState.copy(lastActionPerformed = FABAction.WRITE_REVIEW),
                fabActionFeedback = "Opening review dialog..."
            )
        }
        showReviewDialog()
        setFABMenuExpanded(false)
    }
    
    fun handleFABShareGame() {
        _uiState.update { 
            it.copy(
                fabMenuState = it.fabMenuState.copy(lastActionPerformed = FABAction.SHARE_GAME),
                fabActionFeedback = "Preparing to share..."
            )
        }
        shareGame()
    }
    
    fun focusOnRating() {
        _uiState.update { 
            it.copy(
                shouldFocusRating = true,
                isFABMenuExpanded = false
            )
        }
    }
    
    fun clearRatingFocus() {
        _uiState.update { it.copy(shouldFocusRating = false) }
    }
    
     fun shareGame() {
        val gameData = _uiState.value.data
        if (gameData != null) {
            viewModelScope.launch {
                val shareableContent = ShareableGameContent(
                    title = gameData.name,
                    description = gameData.description,
                    imageUrl = gameData.backgroundImage,
                    platforms = gameData.platforms.map { it.name },
                    additionalInfo = null
                )
                
                shareManager.shareGame(
                    title = shareableContent.title,
                    description = shareableContent.description,
                    imageUrl = shareableContent.imageUrl,
                    additionalInfo = shareableContent.platforms.takeIf { it.isNotEmpty() }
                        ?.let { "Available on: ${it.joinToString(", ")}" }
                ).fold(
                    onSuccess = {
                        // Successfully shared
                        _uiState.update { 
                            it.copy(
                                isFABMenuExpanded = false,
                                fabActionFeedback = "Game shared successfully!"
                            )
                        }
                    },
                    onFailure = { error ->
                        // Handle sharing error - could show a snackbar or error dialog
                        _uiState.update { 
                            it.copy(
                                isFABMenuExpanded = false,
                                fabActionFeedback = "Failed to share game",
                                error = "Failed to share game: ${error.message}"
                            )
                        }
                    }
                )
            }
        } else {
            _uiState.update { 
                it.copy(
                    isFABMenuExpanded = false,
                    fabActionFeedback = "No game data available to share"
                )
            }
        }
    }

}