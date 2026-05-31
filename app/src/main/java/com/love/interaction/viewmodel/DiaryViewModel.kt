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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            diaryRepository.getDiaries(session.spaceId).collect { cached ->
                _uiState.value = _uiState.value.copy(diaries = cached)
            }
        }
        refreshDiaries()
    }

    fun refreshDiaries() {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            diaryRepository.refreshDiaries(session.spaceId)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun createDiary(
        title: String,
        content: String,
        category: DiaryCategory = DiaryCategory.DAILY,
        imageUris: List<Uri> = emptyList()
    ) {
        viewModelScope.launch {
            val session = sessionManager.getSession()
            if (session == null) {
                _uiState.value = _uiState.value.copy(error = "未登录，请重新选择身份")
                return@launch
            }
            if (session.spaceId.isEmpty() || session.spaceId == "couple_main") {
                _uiState.value = _uiState.value.copy(error = "会话无效，请退出重新选择身份")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = diaryRepository.createDiary(
                spaceId = session.spaceId,
                userId = session.userId,
                category = category,
                title = title,
                content = content,
                imageUris = imageUris
            )
            result.onSuccess {
                coinRepository.earn(
                    spaceId = session.spaceId,
                    userId = session.userId,
                    reason = "写日记",
                    amount = AppConfig.COIN_DIARY_REWARD.toLong(),
                    relatedId = it.id
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "日记发布成功 +${AppConfig.COIN_DIARY_REWARD}金币"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = "发布失败: ${e.message}")
            }
        }
    }

    fun selectDiary(diaryId: String) {
        viewModelScope.launch {
            // Load full diary from API to get image_files
            val session = sessionManager.getSession() ?: return@launch
            val cached = db.diaryDao().getDiaryById(diaryId)
            _uiState.value = _uiState.value.copy(currentDiary = cached, currentDiaryImageUrls = emptyList())

            // Fetch from API to get image_files field
            try {
                val response = com.love.interaction.data.remote.PocketBaseClient.api.getDiaryById(diaryId)
                if (response.isSuccessful) {
                    val diary = response.body()!!
                    val urls = diary.getImageUrls()
                    _uiState.value = _uiState.value.copy(currentDiaryImageUrls = urls)
                }
            } catch (_: Exception) {}

            // Refresh comments from API first
            diaryRepository.refreshComments(diaryId)

            diaryRepository.getComments(diaryId).collect { comments ->
                _uiState.value = _uiState.value.copy(comments = comments)
            }
        }
    }

    fun likeDiary(diaryId: String) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            val result = diaryRepository.likeDiary(diaryId, session.userId)
            result.onSuccess {
                coinRepository.earn(session.spaceId, session.userId, "点赞日记", AppConfig.COIN_LIKE_REWARD.toLong())
                refreshDiaries()
            }
        }
    }

    fun addComment(diaryId: String, content: String) {
        viewModelScope.launch {
            val session = sessionManager.getSession() ?: return@launch
            val result = diaryRepository.addComment(diaryId, session.userId, content)
            result.onSuccess {
                coinRepository.earn(session.spaceId, session.userId, "\u8BC4\u8BBA\u65E5\u8BB0", AppConfig.COIN_COMMENT_REWARD.toLong())
                // Refresh comments from server to ensure sync
                diaryRepository.refreshComments(diaryId)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "\u8BC4\u8BBA\u5931\u8D25: ${e.message}")
            }
        }
    }

    fun deleteDiary(id: String) {
        viewModelScope.launch {
            val result = diaryRepository.deleteDiary(id)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(successMessage = "日记已删除")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun clearSuccess() { _uiState.value = _uiState.value.copy(successMessage = null) }
}


