package com.love.interaction.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.local.UserSession
import com.love.interaction.data.remote.PocketBaseClient
import com.love.interaction.data.remote.RealtimeManager
import com.love.interaction.data.repository.AuthRepository
import com.love.interaction.data.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Lover(val id: String, val displayName: String, val emoji: String, val email: String) {
    LONGTENG("user_longteng", "\u9686\u817E", "\uD83D\uDC37", "longteng@love.local"),
    YANHUIXIN("user_yanhuixin", "\u95EB\u6167\u946B", "\uD83D\uDC8D", "yanhuixin@love.local")
}

data class AuthUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val hasSpace: Boolean = false,
    val currentLover: Lover? = null,
    val partnerLover: Lover? = null,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    private val authRepository = AuthRepository(sessionManager)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AuthVM"
    }

    init {
        Log.d(TAG, "init block STARTING - launching coroutine")
        viewModelScope.launch {
            try {
                val session = sessionManager.getSession()
                Log.d(TAG, "init: session=${session != null}, email='${session?.email}', token=${session?.token?.take(20)}..., spaceId='${session?.spaceId}'")
                val me = session?.let { s -> Lover.entries.find { it.id == s.email } }
                Log.d(TAG, "init: Lover match=${me?.displayName}")
                if (me != null && !session.token.isNullOrEmpty() && session.spaceId.isNotEmpty()) {
                    PocketBaseClient.setAuthToken(session.token)
                    val partner = Lover.entries.first { it != me }
                    Log.d(TAG, "init: RESTORING session for ${me.displayName}")
                    _uiState.value = AuthUiState(
                        isLoading = false, isLoggedIn = true, hasSpace = true,
                        currentLover = me, partnerLover = partner
                    )
                } else {
                    Log.d(TAG, "init: CLEARING session (no valid match)")
                    sessionManager.logout()
                    _uiState.value = AuthUiState(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "init: EXCEPTION", e)
                try { sessionManager.logout() } catch (_: Exception) {}
                _uiState.value = AuthUiState(isLoading = false)
            }
        }
    }

    fun selectIdentity(me: Lover) {
        Log.d(TAG, "selectIdentity START for ${me.displayName}, currentState=${_uiState.value}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val partner = Lover.entries.first { it != me }

            // 1. Real PocketBase login
            Log.d(TAG, "selectIdentity: calling authRepository.login(${me.email})")
            val loginResult = authRepository.login(me.email, "love123456")
            loginResult.onFailure { e ->
                Log.e(TAG, "selectIdentity: LOGIN FAILED", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = "\u767B\u5F55\u5931\u8D25: ${e.message}")
                return@launch
            }
            val user = loginResult.getOrNull()!!
            Log.d(TAG, "selectIdentity: login OK, userId=${user.id}")

            // 2. Find couple space
            Log.d(TAG, "selectIdentity: calling findMyCoupleSpace(${user.id})")
            val spaceResult = authRepository.findMyCoupleSpace(user.id)
            spaceResult.onFailure { e ->
                Log.e(TAG, "selectIdentity: SPACE LOOKUP FAILED", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = "\u67E5\u627E\u7A7A\u95F4\u5931\u8D25: ${e.message}")
                return@launch
            }
            val space = spaceResult.getOrNull()
            Log.d(TAG, "selectIdentity: space=${space?.id}")
            if (space == null) {
                Log.e(TAG, "selectIdentity: NO SPACE FOUND")
                _uiState.value = _uiState.value.copy(isLoading = false, error = "\u672A\u627E\u5230\u60C5\u4FA3\u7A7A\u95F4")
                return@launch
            }

            // 3. Save complete session
            val saved = sessionManager.getSession()!!
            Log.d(TAG, "selectIdentity: saving session, token=${saved.token.take(20)}..., spaceId=${saved.spaceId}, partnerId=${saved.partnerId}")
            db.sessionDao().saveSession(
                UserSession(
                    key = "current",
                    userId = user.id,
                    token = saved.token,
                    username = me.displayName,
                    email = me.id,
                    avatar = "",
                    spaceId = saved.spaceId,
                    partnerId = saved.partnerId
                )
            )
            PocketBaseClient.setAuthToken(saved.token)
            RealtimeManager.connect(saved.token, saved.spaceId)
            Log.d(TAG, "selectIdentity: RealtimeManager connected")

            // 4. Update UI state
            val newState = AuthUiState(
                isLoading = false, isLoggedIn = true, hasSpace = true,
                currentLover = me, partnerLover = partner
            )
            Log.d(TAG, "selectIdentity: SETTING isLoggedIn=true, newState=$newState")
            _uiState.value = newState
            Log.d(TAG, "selectIdentity: DONE, currentFlowValue=${_uiState.value}")
        }
    }

    fun logout() {
        Log.d(TAG, "logout() called")
        viewModelScope.launch {
            sessionManager.logout()
            _uiState.value = AuthUiState(isLoading = false)
        }
    }
}
