package com.love.interaction.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.local.CachedWishlist
import com.love.interaction.data.repository.SessionManager
import com.love.interaction.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WishlistUiState(
    val isLoading: Boolean = false,
    val wishlists: List<CachedWishlist> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class WishlistViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    private val wishlistRepository = WishlistRepository(db.wishlistDao(), application)
    val currentUserId: String = kotlinx.coroutines.runBlocking { sessionManager.getSession()?.userId ?: "" }

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            wishlistRepository.getWishlists(session.spaceId).collect { cached ->
                _uiState.value = _uiState.value.copy(wishlists = cached)
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            wishlistRepository.refreshWishlists(session.spaceId)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun createWish(name: String) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = wishlistRepository.createWishlist(session.spaceId, session.userId, name)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "\u613F\u671B\u521B\u5EFA\u6210\u529F")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun fulfillWish(wishId: String, description: String, imageUris: List<Uri> = emptyList()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = wishlistRepository.fulfillWish(wishId, description, imageUris)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "\u8FD8\u613F\u6210\u529F\u2728")
                refresh()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = "\u8FD8\u613F\u5931\u8D25: ${e.message}")
            }
        }
    }

    fun deleteWish(id: String) {
        viewModelScope.launch {
            wishlistRepository.deleteWishlist(id).onSuccess {
                _uiState.value = _uiState.value.copy(successMessage = "\u613F\u671B\u5DF2\u5220\u9664")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
}
