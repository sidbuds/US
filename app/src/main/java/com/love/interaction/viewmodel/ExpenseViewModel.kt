package com.love.interaction.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.local.CachedExpense
import com.love.interaction.data.model.ExpenseCategory
import com.love.interaction.data.repository.CoinRepository
import com.love.interaction.data.repository.ExpenseRepository
import com.love.interaction.data.repository.SessionManager
import com.love.interaction.util.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExpenseUiState(
    val isLoading: Boolean = false,
    val expenses: List<CachedExpense> = emptyList(),
    val totalAmount: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap(),
    val myTotal: Double = 0.0,
    val partnerTotal: Double = 0.0,
    val error: String? = null,
    val successMessage: String? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    val currentUserId: String = kotlinx.coroutines.runBlocking { sessionManager.getSession()?.userId ?: "" }
    private val expenseRepository = ExpenseRepository(db.expenseDao())
    private val coinRepository = CoinRepository()

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            expenseRepository.getExpenses(session.spaceId).collect { cached ->
                val total = cached.sumOf { it.amount }
                val byCategory = cached.groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.amount } }
                val myT = cached.filter { it.paidBy == currentUserId }.sumOf { it.amount }
                val partnerT = total - myT
                _uiState.value = _uiState.value.copy(
                    expenses = cached,
                    totalAmount = total,
                    categoryTotals = byCategory,
                    myTotal = myT,
                    partnerTotal = partnerT
                )
            }
        }
        refreshExpenses()
    }

    fun refreshExpenses() {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            expenseRepository.refreshExpenses(session.spaceId)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun addExpense(
        amount: Double,
        category: ExpenseCategory,
        note: String,
        date: String,
        isIncome: Boolean = false
    ) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = expenseRepository.createExpense(
                spaceId = session.spaceId,
                amount = amount,
                category = category,
                note = note,
                paidBy = session.userId,
                date = date
            )
            result.onSuccess {
                if (!isIncome) coinRepository.earn(session.spaceId, session.userId, "记账", AppConfig.COIN_EXPENSE_REWARD.toLong())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "记账成功 +${AppConfig.COIN_EXPENSE_REWARD}金币"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(id).onSuccess {
                _uiState.value = _uiState.value.copy(successMessage = "已删除")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
}

