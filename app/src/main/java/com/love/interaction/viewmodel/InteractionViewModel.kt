package com.love.interaction.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.local.CachedInteraction
import com.love.interaction.data.remote.RealtimeManager
import com.love.interaction.data.repository.InteractionRepository
import com.love.interaction.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InteractionUiState(
    val isLoading: Boolean = false,
    val interactions: List<CachedInteraction> = emptyList(),
    val hugCount: Int = 0,
    val kissCount: Int = 0,
    val showAnimation: Boolean = false,
    val animationType: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

class InteractionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    val currentUserId: String = kotlinx.coroutines.runBlocking { sessionManager.getSession()?.userId ?: "" }
    private val currentLoverId: String = kotlinx.coroutines.runBlocking { sessionManager.getSession()?.email ?: "" }
    private val isMale: Boolean get() = currentLoverId.contains("longteng")
    private val interactionRepository = InteractionRepository(db.interactionDao())

    private val _uiState = MutableStateFlow(InteractionUiState())
    val uiState: StateFlow<InteractionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            interactionRepository.getInteractions(session.spaceId).collect { cached ->
                val hugs = cached.count { it.type == "hug" }
                val kisses = cached.count { it.type == "kiss" }
                _uiState.value = _uiState.value.copy(interactions = cached, hugCount = hugs, kissCount = kisses)
            }
        }
        refresh()
        viewModelScope.launch {
            RealtimeManager.refreshEvents.collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            if (session.spaceId.isEmpty() || session.spaceId == "couple_main") return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            interactionRepository.refreshInteractions(session.spaceId)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun sendHug() = sendInteraction("hug")
    fun sendKiss() = sendInteraction("kiss")
    fun sendMissYou(reason: String = "") = sendInteraction("miss", reason)

    private fun sendInteraction(type: String, reason: String = "") {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            if (session.spaceId.isEmpty() || session.spaceId == "couple_main" || session.partnerId.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "\u8BF7\u5148\u9009\u62E9\u8EAB\u4EFD")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = interactionRepository.sendInteraction(
                spaceId = session.spaceId, fromUserId = session.userId,
                toUserId = session.partnerId, type = type
            )
            result.onSuccess {
                val label = when (type) { "hug" -> "\u62B1\u62B1"; "kiss" -> "\u4EB2\u4EB2"; "miss" -> "\u60F3\u4F60"; else -> type }
                RealtimeManager.notifyDataChanged()
                _uiState.value = _uiState.value.copy(
                    isLoading = false, showAnimation = true, animationType = type,
                    successMessage = "\u5DF2\u53D1\u9001$label\uFF01"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteInteraction(id: String) {
        viewModelScope.launch {
            interactionRepository.deleteInteraction(id).onSuccess {
                RealtimeManager.notifyDataChanged()
                _uiState.value = _uiState.value.copy(successMessage = "\u5DF2\u5220\u9664")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun hideAnimation() { _uiState.value = _uiState.value.copy(showAnimation = false, animationType = "") }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
}