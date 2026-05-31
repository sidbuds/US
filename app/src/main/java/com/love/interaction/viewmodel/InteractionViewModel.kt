package com.love.interaction.viewmodel

import android.app.Application
import androidx.annotation.RawRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.R
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.local.CachedInteraction
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
    @RawRes val gifResId: Int = 0,
    val showGifPopup: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class InteractionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    val currentUserId: String = kotlinx.coroutines.runBlocking { sessionManager.getSession()?.userId ?: "" }
    private val currentLoverId: String = kotlinx.coroutines.runBlocking { sessionManager.getSession()?.email ?: "" }
    private val isMale: Boolean get() = currentLoverId == "user_longteng"
    private val interactionRepository = InteractionRepository(db.interactionDao())

    private val _uiState = MutableStateFlow(InteractionUiState())
    val uiState: StateFlow<InteractionUiState> = _uiState.asStateFlow()

    private var lastInteractionCount = 0

    init {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            interactionRepository.getInteractions(session.spaceId).collect { cached ->
                val hugs = cached.count { it.type == "hug" }
                val kisses = cached.count { it.type == "kiss" }

                // Detect new incoming interactions (from partner, not me)
                if (lastInteractionCount > 0 && cached.size > lastInteractionCount) {
                    val newOnes = cached.sortedByDescending { it.createdAt }.take(cached.size - lastInteractionCount)
                    val newIncoming = newOnes.firstOrNull { it.fromUserId != currentUserId }
                    if (newIncoming != null) {
                        val gifRes = getGifForInteraction(newIncoming.type, newIncoming.fromUserId)
                        if (gifRes != 0) {
                            _uiState.value = _uiState.value.copy(
                                interactions = cached, hugCount = hugs, kissCount = kisses,
                                showGifPopup = true, gifResId = gifRes
                            )
                            lastInteractionCount = cached.size
                            return@collect
                        }
                    }
                }
                lastInteractionCount = cached.size
                _uiState.value = _uiState.value.copy(interactions = cached, hugCount = hugs, kissCount = kisses)
            }
        }
        refresh()
    }

    private fun getGifForInteraction(type: String, fromUserId: String): Int {
        // Determine sender gender: if I'm male, sender is female, and vice versa
        val senderIsMale = fromUserId != currentUserId && !isMale || fromUserId == currentUserId && isMale
        // Actually: if fromUserId != currentUserId, sender is the partner
        // If I'm male (isMale=true), partner is female, so sender is female
        // If I'm female (isMale=false), partner is male, so sender is male
        val partnerIsMale = !isMale

        return when (type) {
            "hug" -> if (partnerIsMale) R.raw.anim_hug_male_to_female else R.raw.anim_hug_female_to_male
            "kiss" -> if (partnerIsMale) R.raw.anim_kiss_male_to_female else R.raw.anim_kiss_female_to_male
            "miss" -> if (partnerIsMale) R.raw.anim_miss_male_to_female else R.raw.anim_miss_female_to_male
            else -> 0
        }
    }

    fun dismissGifPopup() {
        _uiState.value = _uiState.value.copy(showGifPopup = false, gifResId = 0)
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
                spaceId = session.spaceId,
                fromUserId = session.userId,
                toUserId = session.partnerId,
                type = type
            )
            result.onSuccess {
                val label = when (type) { "hug" -> "\u62B1\u62B1"; "kiss" -> "\u4EB2\u4EB2"; "miss" -> "\u60F3\u4F60"; else -> type }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showAnimation = true,
                    animationType = type,
                    successMessage = "\u5DF2\u53D1\u9001$label\uFF01"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun hideAnimation() { _uiState.value = _uiState.value.copy(showAnimation = false, animationType = "") }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
}
