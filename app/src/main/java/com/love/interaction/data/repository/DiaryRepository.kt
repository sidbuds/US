package com.love.interaction.data.repository

import android.content.Context
import android.net.Uri
import com.love.interaction.data.local.CachedDiary
import com.love.interaction.data.local.CachedDiaryComment
import com.love.interaction.data.local.dao.DiaryCommentDao
import com.love.interaction.data.local.dao.DiaryDao
import com.love.interaction.data.model.*
import com.love.interaction.data.remote.PocketBaseClient
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DiaryRepository(
    private val diaryDao: DiaryDao,
    private val commentDao: DiaryCommentDao,
    private val context: Context
) {
    private val api = PocketBaseClient.api

    fun getDiaries(spaceId: String): Flow<List<CachedDiary>> {
        return diaryDao.getDiariesBySpace(spaceId)
    }

    suspend fun refreshDiaries(spaceId: String): Result<List<Diary>> {
        return try {
            val filter = "space_id=\"$spaceId\" && deleted=false"
            val response = api.getDiaries(filter)
            if (response.isSuccessful) {
                val diaries = response.body()?.items ?: emptyList()
                diaryDao.clearForSpace(spaceId)
                diaryDao.insertAll(diaries.map { it.toCached() })
                Result.success(diaries)
            } else {
                Result.failure(Exception("加载日记失败 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDiary(
        spaceId: String,
        userId: String,
        category: DiaryCategory,
        title: String,
        content: String,
        imageUris: List<Uri> = emptyList()
    ): Result<Diary> {
        return try {
            val json = "application/json; charset=utf-8".toMediaType()
            val spaceIdBody = spaceId.toRequestBody(json)
            val userIdBody = userId.toRequestBody(json)
            val categoryBody = category.key.toRequestBody(json)
            val titleBody = title.toRequestBody(json)
            val contentBody = content.toRequestBody(json)

            val parts = imageUris.mapIndexed { index, uri ->
                val file = uriToFile(uri, "diary_img_$index")
                val reqBody = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("image_files", file.name, reqBody)
            }

            val response = if (parts.isNotEmpty()) {
                api.createDiaryWithFiles(spaceIdBody, userIdBody, categoryBody, titleBody, contentBody, parts)
            } else {
                api.createDiary(DiaryCreateRequest(spaceId, userId, category.key, title, content))
            }

            if (response.isSuccessful) {
                val diary = response.body()!!
                diaryDao.insert(diary.toCached())
                Result.success(diary)
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                Result.failure(Exception("写日记失败 (${response.code()}): $errBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDiary(id: String, updates: Map<String, Any>): Result<Diary> {
        return try {
            val response = api.updateDiary(id, updates)
            if (response.isSuccessful) {
                val diary = response.body()!!
                diaryDao.insert(diary.toCached())
                Result.success(diary)
            } else {
                Result.failure(Exception("更新日记失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDiary(id: String): Result<Unit> {
        return try {
            val response = api.deleteDiary(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("删除日记失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeDiary(diaryId: String, userId: String): Result<DiaryLike> {
        return try {
            val body = mapOf("diary_id" to diaryId, "user_id" to userId)
            val response = api.createDiaryLike(body)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("点赞失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun checkLiked(diaryId: String, userId: String): Result<DiaryLike?> {
        return try {
            val filter = "diary_id=\"$diaryId\" && user_id=\"$userId\""
            val response = api.getDiaryLikes(filter)
            if (response.isSuccessful) Result.success(response.body()?.items?.firstOrNull())
            else Result.failure(Exception("查询点赞状态失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun unlikeDiary(likeId: String): Result<Unit> {
        return try {
            val response = api.deleteDiaryLike(likeId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("取消点赞失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getComments(diaryId: String): Flow<List<CachedDiaryComment>> {
        return commentDao.getCommentsByDiary(diaryId)
    }

    suspend fun refreshComments(diaryId: String): Result<List<DiaryComment>> {
        return try {
            val filter = "diary_id=\"$diaryId\""
            val response = api.getDiaryComments(filter)
            if (response.isSuccessful) {
                val comments = response.body()?.items ?: emptyList()
                commentDao.clearForDiary(diaryId)
                commentDao.insertAll(comments.map { it.toCached() })
                Result.success(comments)
            } else Result.failure(Exception("加载评论失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addComment(diaryId: String, userId: String, content: String): Result<DiaryComment> {
        return try {
            val request = DiaryCommentCreateRequest(diaryId, userId, content)
            val response = api.createDiaryComment(request)
            if (response.isSuccessful) {
                val comment = response.body()!!
                commentDao.insert(comment.toCached())
                Result.success(comment)
            } else Result.failure(Exception("评论失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun uriToFile(uri: Uri, prefix: String): File {
        val input = context.contentResolver.openInputStream(uri)!!
        val bitmap = android.graphics.BitmapFactory.decodeStream(input)
        input.close()
        val tempFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        // Compress to max 1024px wide, 80% quality to keep upload fast
        val scaled = if (bitmap.width > 1024) {
            val ratio = 1024f / bitmap.width
            android.graphics.Bitmap.createScaledBitmap(bitmap, 1024, (bitmap.height * ratio).toInt(), true)
        } else bitmap
        tempFile.outputStream().use { output -> scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, output) }
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
        return tempFile
    }

    private fun Diary.toCached() = CachedDiary(
        id = id, collectionId = collectionId, spaceId = spaceId, userId = userId, category = category,
        title = title, content = content, images = images, likesCount = likesCount,
        createdAt = createdAt, updatedAt = updatedAt, deleted = deleted
    )

    private fun DiaryComment.toCached() = CachedDiaryComment(
        id = id, diaryId = diaryId, userId = userId, content = content, createdAt = createdAt
    )
}

