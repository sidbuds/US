package com.love.interaction.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.local.CachedDiary
import com.love.interaction.data.local.CachedDiaryComment
import com.love.interaction.data.model.DiaryCategory
import com.love.interaction.data.repository.CoinRepository
import com.love.interaction.data.repository.DiaryRepository
import com.love.interaction.data.repository.SessionManager
import com.love.interaction.util.AppConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DiaryUiState(
    val isLoading: Boolean = false,
    val diaries: List<CachedDiary> = emptyList(),
    val currentDiary: CachedDiary? = null,
    val currentDiaryImageUrls: List<String> = emptyList(),
    val comments: List<CachedDiaryComment> = emptyList(),
    val likedDiaryIds: Set<String> = emptySet(),
    val error: String? = null,
    val successMessage: String? = null
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val sessionManager = SessionManager(db.sessionDao())
    val currentUserId: String = kotlinx.coroutines.runBlocking { sessionManager.getSession()?.userId ?: "" }
    private val diaryRepository = DiaryRepository(db.diaryDao(), db.diaryCommentDao(), application)
    private val coinRepository = CoinRepository()

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            diaryRepository.getDiaries(session.spaceId).collect { cached ->
                _uiState.value = _uiState.value.copy(diaries = cached)
            }
        }
        refreshDiaries()
    }

    fun startAutoRefresh() {
        if (autoRefreshJob != null) return
        autoRefreshJob = viewModelScope.launch {
            while (isActive) { delay(5000); refreshDiaries() }
        }
    }

    fun stopAutoRefresh() { autoRefreshJob?.cancel(); autoRefreshJob = null }

    fun refreshDiaries() {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            diaryRepository.refreshDiaries(session.spaceId)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun createDiary(title: String, content: String, category: DiaryCategory = DiaryCategory.DAILY, imageUris: List<Uri> = emptyList()) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: run {
                _uiState.value = _uiState.value.copy(error = "\u672A\u767B\u5F55\uFF0C\u8BF7\u91CD\u65B0\u9009\u62E9\u8EAB\u4EFD")
                return@launch
            }
            if (session.spaceId.isEmpty() || session.spaceId == "couple_main") {
                _uiState.value = _uiState.value.copy(error = "\u4F1A\u8BDD\u65E0\u6548\uFF0C\u8BF7\u9000\u51FA\u91CD\u65B0\u9009\u62E9\u8EAB\u4EFD")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = diaryRepository.createDiary(
                spaceId = session.spaceId, userId = session.userId,
                category = category, title = title, content = content, imageUris = imageUris
            )
            result.onSuccess {
                coinRepository.earn(session.spaceId, session.userId, "\u5199\u65E5\u8BB0", AppConfig.COIN_DIARY_REWARD.toLong(), relatedId = it.id)
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "\u65E5\u8BB0\u53D1\u5E03\u6210\u529F +${AppConfig.COIN_DIARY_REWARD}\u91D1\u5E01")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = "\u53D1\u5E03\u5931\u8D25: ${e.message}")
            }
        }
    }

    fun selectDiary(diaryId: String) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            val cached = db.diaryDao().getDiaryById(diaryId)
            _uiState.value = _uiState.value.copy(currentDiary = cached, currentDiaryImageUrls = emptyList())
            try {
                val response = com.love.interaction.data.remote.PocketBaseClient.api.getDiaryById(diaryId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(currentDiaryImageUrls = response.body()!!.getImageUrls())
                }
            } catch (_: Exception) {}
            diaryRepository.refreshComments(diaryId)
            diaryRepository.getComments(diaryId).collect { comments ->
                _uiState.value = _uiState.value.copy(comments = comments)
            }
        }
    }

    fun likeDiary(diaryId: String) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            diaryRepository.likeDiary(diaryId, session.userId).onSuccess {
                coinRepository.earn(session.spaceId, session.userId, "\u70B9\u8D5E\u65E5\u8BB0", AppConfig.COIN_LIKE_REWARD.toLong())
                refreshDiaries()
            }
        }
    }

    fun addComment(diaryId: String, content: String) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            diaryRepository.addComment(diaryId, session.userId, content).onSuccess {
                coinRepository.earn(session.spaceId, session.userId, "\u8BC4\u8BBA\u65E5\u8BB0", AppConfig.COIN_COMMENT_REWARD.toLong())
                diaryRepository.refreshComments(diaryId)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "\u8BC4\u8BBA\u5931\u8D25: ${e.message}")
            }
        }
    }

    fun deleteDiary(id: String) {
        viewModelScope.launch {
            diaryRepository.deleteDiary(id).onSuccess {
                _uiState.value = _uiState.value.copy(successMessage = "\u65E5\u8BB0\u5DF2\u5220\u9664")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
}