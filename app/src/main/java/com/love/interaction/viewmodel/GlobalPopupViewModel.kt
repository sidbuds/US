package com.love.interaction.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.RawRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.R
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.repository.CheckinRepository
import com.love.interaction.data.repository.InteractionRepository
import com.love.interaction.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GlobalPopupState(
    val showGifPopup: Boolean = false,
    @RawRes val gifResId: Int = 0
)

class GlobalPopupViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "GlobalPopupVM"
    }

    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    private val interactionRepo = InteractionRepository(db.interactionDao())
    private val checkinRepo = CheckinRepository(db.checkinDao())

    private val currentUserId: String = runBlocking { sessionManager.getSession()?.userId ?: "" }
    private val currentEmail: String = runBlocking { sessionManager.getSession()?.email ?: "" }
    private val isMale: Boolean get() = currentEmail.contains("longteng")

    private val _state = MutableStateFlow(GlobalPopupState())
    val state: StateFlow<GlobalPopupState> = _state.asStateFlow()

    // Track seen IDs to detect NEW items only
    private val seenInteractionIds = mutableSetOf<String>()
    private val seenCheckinIds = mutableSetOf<String>()

    // Flag: skip first emission to avoid popups on app restart with existing data
    private var interactionInitialized = false
    private var checkinInitialized = false

    init {
        Log.d(TAG, "init: userId=$currentUserId, email=$currentEmail, isMale=$isMale")
        val session = runBlocking { sessionManager.getSession() }
        if (session != null && session.spaceId.isNotEmpty()) {
            Log.d(TAG, "Starting monitors for spaceId=${session.spaceId}")

            // Monitor interactions via Room Flow
            viewModelScope.launch {
                interactionRepo.getInteractions(session.spaceId).collect { cached ->
                    val currentIds = cached.map { it.id }.toSet()
                    Log.d(TAG, "InteractionFlow: ${cached.size} items, initialized=$interactionInitialized, seenCount=${seenInteractionIds.size}")

                    if (!interactionInitialized) {
                        // First emission: mark all existing as seen, no popup
                        seenInteractionIds.addAll(currentIds)
                        interactionInitialized = true
                        Log.d(TAG, "InteractionFlow: initialized with ${currentIds.size} existing items")
                        return@collect
                    }

                    // Find truly new IDs
                    val newIds = currentIds - seenInteractionIds
                    if (newIds.isNotEmpty()) {
                        Log.d(TAG, "InteractionFlow: ${newIds.size} new interaction(s) detected")
                        val newItems = cached.filter { it.id in newIds }
                        // Find the newest incoming (from partner, not self)
                        val incoming = newItems
                            .filter { it.fromUserId != currentUserId }
                            .maxByOrNull { it.createdAt }

                        if (incoming != null) {
                            Log.d(TAG, "Incoming interaction: type=${incoming.type}, from=${incoming.fromUserId}")
                            val res = mapInteractionGif(incoming.type)
                            if (res != 0) {
                                Log.d(TAG, "Showing GIF popup: resId=$res")
                                _state.value = GlobalPopupState(showGifPopup = true, gifResId = res)
                            } else {
                                Log.w(TAG, "No GIF mapping for interaction type: ${incoming.type}")
                            }
                        } else {
                            Log.d(TAG, "New interactions were all self-sent, no popup")
                        }
                    }
                    // Update seen set
                    seenInteractionIds.addAll(currentIds)
                }
            }

            // Monitor checkins via Room Flow
            viewModelScope.launch {
                checkinRepo.getCheckins(session.spaceId).collect { cached ->
                    val currentIds = cached.map { it.id }.toSet()
                    Log.d(TAG, "CheckinFlow: ${cached.size} items, initialized=$checkinInitialized, seenCount=${seenCheckinIds.size}")

                    if (!checkinInitialized) {
                        seenCheckinIds.addAll(currentIds)
                        checkinInitialized = true
                        Log.d(TAG, "CheckinFlow: initialized with ${currentIds.size} existing items")
                        return@collect
                    }

                    val newIds = currentIds - seenCheckinIds
                    if (newIds.isNotEmpty()) {
                        Log.d(TAG, "CheckinFlow: ${newIds.size} new checkin(s) detected")
                        val newItems = cached.filter { it.id in newIds }
                        val incoming = newItems
                            .filter { it.userId != currentUserId }
                            .maxByOrNull { it.createdAt }

                        if (incoming != null) {
                            Log.d(TAG, "Incoming checkin: type=${incoming.type}, from=${incoming.userId}")
                            val res = mapCheckinGif(incoming.type)
                            if (res != 0) {
                                Log.d(TAG, "Showing checkin GIF popup: resId=$res")
                                _state.value = GlobalPopupState(showGifPopup = true, gifResId = res)
                            } else {
                                Log.w(TAG, "No GIF mapping for checkin type: ${incoming.type}")
                            }
                        } else {
                            Log.d(TAG, "New checkins were all self-sent, no popup")
                        }
                    }
                    seenCheckinIds.addAll(currentIds)
                }
            }

            // Trigger initial refresh from PocketBase
            viewModelScope.launch {
                Log.d(TAG, "Refreshing interactions from PocketBase...")
                val result = interactionRepo.refreshInteractions(session.spaceId)
                Log.d(TAG, "Refresh interactions result: ${result.isSuccess}")
            }
            viewModelScope.launch {
                Log.d(TAG, "Refreshing checkins from PocketBase...")
                val result = checkinRepo.refreshCheckins(session.spaceId)
                Log.d(TAG, "Refresh checkins result: ${result.isSuccess}")
            }
        } else {
            Log.w(TAG, "No session or empty spaceId, popup monitoring disabled")
        }
    }

    private fun mapInteractionGif(type: String): Int {
        val partnerIsMale = !isMale
        Log.d(TAG, "mapInteractionGif: type=$type, isMale=$isMale, partnerIsMale=$partnerIsMale")
        return when (type) {
            "hug" -> if (partnerIsMale) R.raw.anim_hug_male_to_female else R.raw.anim_hug_female_to_male
            "kiss" -> if (partnerIsMale) R.raw.anim_kiss_male_to_female else R.raw.anim_kiss_female_to_male
            "miss" -> if (partnerIsMale) R.raw.anim_miss_male_to_female else R.raw.anim_miss_female_to_male
            else -> 0
        }
    }

    private fun mapCheckinGif(type: String): Int {
        val partnerIsMale = !isMale
        Log.d(TAG, "mapCheckinGif: type=$type, isMale=$isMale, partnerIsMale=$partnerIsMale")
        return when (type) {
            "wake_up" -> if (partnerIsMale) R.raw.anim_wakeup_male else R.raw.anim_wakeup_female
            "lunch" -> if (partnerIsMale) R.raw.anim_eat_male else R.raw.anim_eat_female
            "sleep" -> R.raw.anim_sleep
            else -> 0
        }
    }

    fun dismissGifPopup() {
        Log.d(TAG, "GIF popup dismissed")
        _state.value = GlobalPopupState()
    }
}

private fun <T> runBlocking(block: suspend () -> T): T = kotlinx.coroutines.runBlocking { block() }