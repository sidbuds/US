package com.love.interaction.data.repository

import com.love.interaction.data.model.*
import com.love.interaction.data.remote.PocketBaseClient

class AuthRepository(private val sessionManager: SessionManager) {

    private val api = PocketBaseClient.api

    /**
     * Login with email/password
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.authWithPassword(mapOf("identity" to email, "password" to password))
            if (response.isSuccessful) {
                val auth = response.body()!!
                sessionManager.saveLogin(
                    userId = auth.record.id,
                    token = auth.token,
                    username = auth.record.username,
                    email = auth.record.email,
                    avatar = auth.record.avatar
                )
                Result.success(auth.record)
            } else {
                Result.failure(Exception("登录失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new user
     */
    suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            val body = mapOf(
                "email" to email,
                "password" to password,
                "passwordConfirm" to password,
                "username" to username
            )
            val response = api.registerUser(body)
            if (response.isSuccessful) {
                val user = response.body()!!
                // Auto-login after registration
                login(email, password)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Result.failure(Exception("注册失败: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a couple space (user A creates, generates 6-digit code)
     */
    suspend fun createCoupleSpace(userId: String, code: String): Result<CoupleSpace> {
        return try {
            val request = CoupleSpaceCreateRequest(
                code = code,
                userAId = userId
            )
            val response = api.createCoupleSpace(request)
            if (response.isSuccessful) {
                val space = response.body()!!
                sessionManager.saveSpaceInfo(spaceId = space.id, partnerId = "")
                Result.success(space)
            } else {
                Result.failure(Exception("创建空间失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Join a couple space using the 6-digit code (user B joins)
     */
    suspend fun joinCoupleSpace(code: String, userId: String): Result<CoupleSpace> {
        return try {
            // Find the space by code
            val filter = "code=\"$code\" && status=\"active\" && user_b_id=\"\""
            val listResponse = api.getCoupleSpaces(filter)
            if (!listResponse.isSuccessful || listResponse.body()?.items.isNullOrEmpty()) {
                return Result.failure(Exception("情侣码无效或已被使用"))
            }
            val space = listResponse.body()!!.items.first()

            // Join the space
            val response = api.joinCoupleSpace(
                space.id,
                CoupleSpaceJoinRequest(userBId = userId)
            )
            if (response.isSuccessful) {
                val joinedSpace = response.body()!!
                sessionManager.saveSpaceInfo(
                    spaceId = joinedSpace.id,
                    partnerId = joinedSpace.userAId
                )
                Result.success(joinedSpace)
            } else {
                Result.failure(Exception("加入空间失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if user has an existing couple space
     */
    suspend fun findMyCoupleSpace(userId: String): Result<CoupleSpace?> {
        return try {
            val filter = "(user_a_id=\"$userId\" || user_b_id=\"$userId\") && status=\"active\""
            val response = api.getCoupleSpaces(filter)
            if (response.isSuccessful) {
                val space = response.body()?.items?.firstOrNull()
                if (space != null) {
                    val partnerId = if (space.userAId == userId) space.userBId else space.userAId
                    sessionManager.saveSpaceInfo(spaceId = space.id, partnerId = partnerId)
                }
                Result.success(space)
            } else {
                Result.failure(Exception("查询空间失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Request password reset
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val response = api.requestPasswordReset(mapOf("email" to email))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("重置密码请求失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a random 6-digit couple code
     */
    fun generateCoupleCode(): String {
        return (100000..999999).random().toString()
    }
}

