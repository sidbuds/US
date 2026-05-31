package com.love.interaction.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.local.CachedCheckin
import com.love.interaction.data.model.CheckinType
import com.love.interaction.data.remote.RealtimeManager
import com.love.interaction.data.repository.CheckinRepository
import com.love.interaction.data.repository.CoinRepository
import com.love.interaction.data.repository.SessionManager
import com.love.interaction.util.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckinUiState(
    val isLoading: Boolean = false,
    val checkins: List<CachedCheckin> = emptyList(),
    val todayCount: Int = 0,
    val error: String? = null,
    val successMessage: String? = null
)

class CheckinViewModel(application: Application) : AndroidViewModel(application) {
    val currentUserId: String = run {
        val db = com.love.interaction.data.local.AppDatabase.getInstance(application)
        val sm = com.love.interaction.data.repository.SessionManager(db.sessionDao())
        kotlinx.coroutines.runBlocking { sm.getSession()?.userId ?: "" }
    }

    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    private val checkinRepository = CheckinRepository(db.checkinDao())
    private val coinRepository = CoinRepository()

    private val _uiState = MutableStateFlow(CheckinUiState())
    val uiState: StateFlow<CheckinUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            checkinRepository.getCheckins(session.spaceId).collect { cached ->
                _uiState.value = _uiState.value.copy(checkins = cached)
            }
        }
        refreshCheckins()
        viewModelScope.launch {
            RealtimeManager.refreshEvents.collect { refreshCheckins() }
        }
    }

    fun refreshCheckins() {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = checkinRepository.refreshCheckins(session.spaceId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun sendCheckin(type: CheckinType, customContent: String = "") {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            if (session.spaceId.isEmpty() || session.spaceId == "couple_main") {
                _uiState.value = _uiState.value.copy(error = "\u8BF7\u9000\u51FA\u91CD\u65B0\u9009\u62E9\u8EAB\u4EFD")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = checkinRepository.createCheckin(
                spaceId = session.spaceId, userId = session.userId,
                type = type, customContent = customContent
            )
            result.onSuccess {
                coinRepository.earn(session.spaceId, session.userId, "\u62A5\u5907", AppConfig.COIN_CHECKIN_REWARD.toLong())
                RealtimeManager.notifyDataChanged()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "\u62A5\u5907\u6210\u529F +${AppConfig.COIN_CHECKIN_REWARD}\u91D1\u5E01"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteCheckin(id: String) {
        viewModelScope.launch {
            checkinRepository.deleteCheckin(id).onSuccess {
                RealtimeManager.notifyDataChanged()
                _uiState.value = _uiState.value.copy(successMessage = "\u5DF2\u5220\u9664")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
}