package com.love.interaction.data.repository

import com.love.interaction.data.local.CachedInteraction
import com.love.interaction.data.local.dao.InteractionDao
import com.love.interaction.data.model.Interaction
import com.love.interaction.data.model.InteractionCreateRequest
import com.love.interaction.data.remote.PocketBaseClient
import kotlinx.coroutines.flow.Flow

class InteractionRepository(
    private val interactionDao: InteractionDao
) {
    private val api = PocketBaseClient.api

    fun getInteractions(spaceId: String): Flow<List<CachedInteraction>> {
        return interactionDao.getInteractionsBySpace(spaceId)
    }

    suspend fun refreshInteractions(spaceId: String): Result<List<Interaction>> {
        return try {
            val filter = "space_id=\"$spaceId\""
            val response = api.getInteractions(filter)
            if (response.isSuccessful) {
                val interactions = response.body()?.items ?: emptyList()
                interactionDao.clearForSpace(spaceId)
                interactionDao.insertAll(interactions.map { it.toCached() })
                Result.success(interactions)
            } else {
                Result.failure(Exception("加载互动失败 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendInteraction(
        spaceId: String,
        fromUserId: String,
        toUserId: String,
        type: String // "hug" or "kiss"
    ): Result<Interaction> {
        return try {
            val request = InteractionCreateRequest(
                spaceId = spaceId,
                fromUserId = fromUserId,
                toUserId = toUserId,
                type = type
            )
            val response = api.createInteraction(request)
            if (response.isSuccessful) {
                val interaction = response.body()!!
                interactionDao.insert(interaction.toCached())
                Result.success(interaction)
            } else {
                Result.failure(Exception("发送失败 (${response.code()}): ${response.errorBody()?.string() ?: ""}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: String): Result<Unit> {
        return try {
            val response = api.markInteractionRead(id, mapOf("is_read" to true))
            if (response.isSuccessful) {
                interactionDao.markRead(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("标记已读失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayCount(spaceId: String, userId: String, type: String): Int {
        val todayStart = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toString()
        return interactionDao.getTodayCount(spaceId, userId, type, todayStart)
    }

    private fun Interaction.toCached() = CachedInteraction(
        id = id,
        spaceId = spaceId,
        fromUserId = fromUserId,
        toUserId = toUserId,
        type = type,
        reason = reason,
        isRead = isRead,
        createdAt = createdAt
    )
}

