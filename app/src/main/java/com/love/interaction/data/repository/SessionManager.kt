package com.love.interaction.data.repository

import com.love.interaction.data.local.UserSession
import com.love.interaction.data.local.dao.SessionDao
import com.love.interaction.data.remote.PocketBaseClient
import kotlinx.coroutines.flow.Flow

/**
 * Manages user session state: login, logout, token persistence.
 */
class SessionManager(private val sessionDao: SessionDao) {

    val currentSession: Flow<UserSession?> = sessionDao.observeSession()

    suspend fun getSession(): UserSession? = sessionDao.getSession()

    suspend fun saveLogin(
        userId: String,
        token: String,
        username: String,
        email: String,
        avatar: String = ""
    ) {
        PocketBaseClient.setAuthToken(token)
        val existing = sessionDao.getSession()
        sessionDao.saveSession(
            UserSession(
                key = "current",
                userId = userId,
                token = token,
                username = username,
                email = email,
                avatar = avatar,
                spaceId = existing?.spaceId ?: "",
                partnerId = existing?.partnerId ?: ""
            )
        )
    }

    suspend fun saveSpaceInfo(spaceId: String, partnerId: String) {
        val existing = sessionDao.getSession() ?: return
        sessionDao.saveSession(
            existing.copy(spaceId = spaceId, partnerId = partnerId)
        )
    }

    suspend fun restoreToken() {
        val session = sessionDao.getSession()
        if (session?.token?.isNotEmpty() == true) {
            PocketBaseClient.setAuthToken(session.token)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val session = sessionDao.getSession()
        return session?.token?.isNotEmpty() == true
    }

    suspend fun logout() {
        PocketBaseClient.setAuthToken(null)
        sessionDao.clearSession()
    }
}
