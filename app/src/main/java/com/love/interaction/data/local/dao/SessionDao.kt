package com.love.interaction.data.local.dao

import androidx.room.*
import com.love.interaction.data.local.UserSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM user_session WHERE key = 'current' LIMIT 1")
    suspend fun getSession(): UserSession?

    @Query("SELECT * FROM user_session WHERE key = 'current' LIMIT 1")
    fun observeSession(): Flow<UserSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSession)

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}
