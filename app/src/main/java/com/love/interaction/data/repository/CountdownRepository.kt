package com.love.interaction.data.repository

import android.util.Log
import com.love.interaction.data.model.Countdown
import com.love.interaction.data.model.CountdownCreateRequest
import com.love.interaction.data.remote.PocketBaseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class CountdownRepository(private val sessionManager: SessionManager) {

    private val api = PocketBaseClient.api
    companion object {
        private const val TAG = "CountdownRepo"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun getCountdowns(): Result<List<Countdown>> {
        return try {
            val session = sessionManager.getSession() ?: return Result.failure(Exception("未登录"))
            val filter = "space_id=\"${session.spaceId}\""
            val response = api.getCountdowns(filter)
            if (response.isSuccessful) {
                Result.success(response.body()?.items ?: emptyList())
            } else {
                Log.e(TAG, "getCountdowns failed: ${response.code()} ${response.errorBody()?.string()}")
                Result.failure(Exception("加载倒数日失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCountdowns exception", e)
            Result.failure(e)
        }
    }

    suspend fun createCountdown(emoji: String, title: String, date: String, showOnHome: Boolean): Result<Countdown> {
        return try {
            val session = sessionManager.getSession() ?: return Result.failure(Exception("未登录"))
            val body = CountdownCreateRequest(
                spaceId = session.spaceId,
                emoji = emoji,
                title = title,
                date = date,
                showOnHome = showOnHome
            )
            val response = api.createCountdown(body)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Log.e(TAG, "createCountdown failed: ${response.code()} ${response.errorBody()?.string()}")
                Result.failure(Exception("创建倒数日失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "createCountdown exception", e)
            Result.failure(e)
        }
    }

    suspend fun updateCountdown(id: String, title: String, date: String, emoji: String, showOnHome: Boolean): Result<Countdown> {
        return try {
            val json = """{"title":"$title","date":"$date","emoji":"$emoji","show_on_home":$showOnHome}"""
            val body = json.toRequestBody(JSON_MEDIA)
            val response = api.updateCountdown(id, body)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Log.e(TAG, "updateCountdown failed: ${response.code()} ${response.errorBody()?.string()}")
                Result.failure(Exception("更新倒数日失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateCountdown exception", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCountdown(id: String): Result<Unit> {
        return try {
            val response = api.deleteCountdown(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Log.e(TAG, "deleteCountdown failed: ${response.code()}")
                Result.failure(Exception("删除倒数日失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteCountdown exception", e)
            Result.failure(e)
        }
    }
}
