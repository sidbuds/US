package com.love.interaction.data.repository

import com.love.interaction.data.local.CachedExpense
import com.love.interaction.data.local.dao.ExpenseDao
import com.love.interaction.data.model.Expense
import com.love.interaction.data.model.ExpenseCategory
import com.love.interaction.data.model.ExpenseCreateRequest
import com.love.interaction.data.remote.PocketBaseClient
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    private val api = PocketBaseClient.api

    fun getExpenses(spaceId: String): Flow<List<CachedExpense>> {
        return expenseDao.getExpensesBySpace(spaceId)
    }

    suspend fun refreshExpenses(spaceId: String): Result<List<Expense>> {
        return try {
            val filter = "space_id=\"$spaceId\""
            val response = api.getExpenses(filter)
            if (response.isSuccessful) {
                val expenses = response.body()?.items ?: emptyList()
                expenseDao.clearForSpace(spaceId)
                expenseDao.insertAll(expenses.map { it.toCached() })
                Result.success(expenses)
            } else {
                Result.failure(Exception("加载账单失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createExpense(
        spaceId: String,
        amount: Double,
        category: ExpenseCategory,
        note: String,
        paidBy: String,
        date: String
    ): Result<Expense> {
        return try {
            val request = ExpenseCreateRequest(spaceId, amount, category.key, note, paidBy, date)
            val response = api.createExpense(request)
            if (response.isSuccessful) {
                val expense = response.body()!!
                expenseDao.insert(expense.toCached())
                Result.success(expense)
            } else {
                Result.failure(Exception("记账失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(id: String): Result<Unit> {
        return try {
            val response = api.deleteExpense(id)
            if (response.isSuccessful) {
                expenseDao.deleteById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除账单失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Expense.toCached() = CachedExpense(
        id = id, spaceId = spaceId, amount = amount, category = category,
        note = note, paidBy = paidBy, date = date, createdAt = createdAt, updatedAt = updatedAt
    )
}
