package com.love.interaction.data.repository

import com.love.interaction.data.model.CoinBalance
import com.love.interaction.data.model.CoinTransaction
import com.love.interaction.data.model.CoinTransactionCreateRequest
import com.love.interaction.data.remote.PocketBaseClient

class CoinRepository {
    private val api = PocketBaseClient.api

    suspend fun getBalance(spaceId: String): Result<CoinBalance> {
        return try {
            val filter = "space_id=\"$spaceId\""
            val response = api.getCoinBalance(filter)
            if (response.isSuccessful) {
                val balance = response.body()?.items?.firstOrNull()
                if (balance != null) {
                    Result.success(balance)
                } else {
                    // Create initial balance record
                    val createBody = mapOf(
                        "space_id" to spaceId,
                        "balance" to 100L,
                        "total_earned" to 100L,
                        "total_spent" to 0L
                    )
                    val createResponse = api.createCoinBalance(createBody)
                    if (createResponse.isSuccessful) {
                        Result.success(createResponse.body()!!)
                    } else {
                        Result.failure(Exception("创建金币账户失败"))
                    }
                }
            } else {
                Result.failure(Exception("获取金币余额失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBalance(spaceId: String, amount: Long, isEarn: Boolean): Result<CoinBalance> {
        return try {
            val current = getBalance(spaceId).getOrThrow()
            val newBalance = if (isEarn) current.balance + amount else current.balance - amount
            val newEarned = if (isEarn) current.totalEarned + amount else current.totalEarned
            val newSpent = if (!isEarn) current.totalSpent + amount else current.totalSpent

            val body = mapOf(
                "balance" to newBalance,
                "total_earned" to newEarned,
                "total_spent" to newSpent
            )
            val response = api.updateCoinBalance(current.spaceId, body)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("更新金币余额失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTransaction(
        spaceId: String,
        userId: String,
        type: String,
        reason: String,
        amount: Long,
        relatedId: String = ""
    ): Result<CoinTransaction> {
        return try {
            val request = CoinTransactionCreateRequest(
                spaceId = spaceId,
                userId = userId,
                type = type,
                reason = reason,
                amount = amount,
                relatedId = relatedId
            )
            val response = api.createCoinTransaction(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("记录交易失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun earn(
        spaceId: String,
        userId: String,
        reason: String,
        amount: Long,
        relatedId: String = ""
    ): Result<CoinTransaction> {
        updateBalance(spaceId, amount, isEarn = true)
        return addTransaction(spaceId, userId, "earn", reason, amount, relatedId)
    }

    suspend fun spend(
        spaceId: String,
        userId: String,
        reason: String,
        amount: Long,
        relatedId: String = ""
    ): Result<CoinTransaction> {
        val balance = getBalance(spaceId).getOrThrow()
        if (balance.balance < amount) {
            return Result.failure(Exception("金币不足"))
        }
        updateBalance(spaceId, amount, isEarn = false)
        return addTransaction(spaceId, userId, "spend", reason, amount, relatedId)
    }

    suspend fun getTransactions(
        spaceId: String,
        page: Int = 1,
        perPage: Int = 50
    ): Result<List<CoinTransaction>> {
        return try {
            val filter = "space_id=\"$spaceId\""
            val response = api.getCoinTransactions(filter, page = page, perPage = perPage)
            if (response.isSuccessful) {
                Result.success(response.body()?.items ?: emptyList())
            } else {
                Result.failure(Exception("获取交易记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
