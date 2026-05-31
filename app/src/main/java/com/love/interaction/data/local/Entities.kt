package com.love.interaction.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_checkins")
data class CachedCheckin(
    @PrimaryKey val id: String,
    val spaceId: String,
    val userId: String,
    val type: String,
    val customContent: String = "",
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationName: String = "",
    val createdAt: String = ""
)

@Entity(tableName = "cached_interactions")
data class CachedInteraction(
    @PrimaryKey val id: String,
    val spaceId: String,
    val fromUserId: String,
    val toUserId: String,
    val type: String,
    val reason: String = "",
    val isRead: Boolean = false,
    val createdAt: String = ""
)

@Entity(tableName = "cached_diaries")
data class CachedDiary(
    @PrimaryKey val id: String,
    val collectionId: String = "",
    val spaceId: String,
    val userId: String,
    val category: String = "daily",
    val title: String = "",
    val content: String = "",
    val images: String = "[]",
    val likesCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val deleted: Boolean = false
)

@Entity(tableName = "cached_diary_comments")
data class CachedDiaryComment(
    @PrimaryKey val id: String,
    val diaryId: String,
    val userId: String,
    val content: String = "",
    val createdAt: String = ""
)

@Entity(tableName = "cached_expenses")
data class CachedExpense(
    @PrimaryKey val id: String,
    val spaceId: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    val paidBy: String,
    val date: String,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Entity(tableName = "cached_wishlists")
data class CachedWishlist(
    @PrimaryKey val id: String,
    val collectionId: String = "",
    val userId: String = "",
    val spaceId: String,
    val name: String,
    val targetCoins: Long,
    val currentBalance: Long = 0,
    val imageUrl: String = "",
    val status: String = "active",
    val fulfillDescription: String = "",
    val fulfillImages: String = "[]",
    val completedAt: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun getFulfillImageUrls(): List<String> {
        val cid = collectionId.ifEmpty { "pbc_3702485544" }
        val base = com.love.interaction.util.AppConfig.POCKETBASE_URL.trimEnd('/')
        return try {
            val list = if (fulfillImages.isBlank() || fulfillImages == "[]") emptyList()
            else try { com.google.gson.Gson().fromJson(fulfillImages, Array<String>::class.java)?.toList() ?: emptyList() }
                 catch (_: Exception) { listOf(fulfillImages) }
            list.map { filename -> "$base/api/files/$cid/$id/$filename" }
        } catch (_: Exception) { emptyList() }
    }
}

@Entity(tableName = "user_session")
data class UserSession(
    @PrimaryKey val key: String = "current",
    val userId: String = "",
    val token: String = "",
    val username: String = "",
    val email: String = "",
    val avatar: String = "",
    val spaceId: String = "",
    val partnerId: String = ""
)