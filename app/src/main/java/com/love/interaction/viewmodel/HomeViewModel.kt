package com.love.interaction.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.model.CoinBalance
import com.love.interaction.data.model.User
import com.love.interaction.data.remote.PocketBaseClient
import com.love.interaction.data.repository.CoinRepository
import com.love.interaction.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val partnerName: String = "",
    val coinBalance: CoinBalance? = null,
    val daysTogether: Int = 0,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    private val coinRepository = CoinRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val session = sessionManager.getSession() ?: return@launch
                val balanceResult = coinRepository.getBalance(session.spaceId)
                balanceResult.onSuccess { balance ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        coinBalance = balance
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }

                // Try to get partner info
                if (session.partnerId.isNotEmpty()) {
                    try {
                        // partner info loaded from session
                    } catch (_: Exception) { }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refresh() = loadData()
}

