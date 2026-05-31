package com.love.interaction.data.model

import com.google.gson.annotations.SerializedName

// ========== Auth Models ==========

data class AuthResponse(
    val token: String,
    val record: User
)

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val avatar: String = "",
    @SerializedName("created") val created: String = ""
)

// ========== Couple Space ==========

data class CoupleSpace(
    val id: String = "",
    val code: String = "",
    @SerializedName("user_a_id") val userAId: String = "",
    @SerializedName("user_b_id") val userBId: String = "",
    @SerializedName("created") val createdAt: String = "",
    val status: String = "active"
)

data class CoupleSpaceCreateRequest(
    val code: String,
    @SerializedName("user_a_id") val userAId: String,
    val status: String = "active"
)

data class CoupleSpaceJoinRequest(
    @SerializedName("user_b_id") val userBId: String,
    val status: String = "active"
)

// ========== Checkin ==========

data class Checkin(
    val id: String = "",
    @SerializedName("space_id") val spaceId: String = "",
    @SerializedName("user_id") val userId: String = "",
    val type: String = "",
    @SerializedName("custom_content") val customContent: String = "",
    @SerializedName("location_lat") val locationLat: Double? = null,
    @SerializedName("location_lng") val locationLng: Double? = null,
    @SerializedName("location_name") val locationName: String = "",
    @SerializedName("created") val createdAt: String = ""
)

data class CheckinCreateRequest(
    @SerializedName("space_id") val spaceId: String,
    @SerializedName("user_id") val userId: String,
    val type: String,
    @SerializedName("custom_content") val customContent: String = "",
    @SerializedName("location_lat") val locationLat: Double? = null,
    @SerializedName("location_lng") val locationLng: Double? = null,
    @SerializedName("location_name") val locationName: String = "",
    @SerializedName("created") val createdAt: String = ""
)

// ========== Interaction (Hug/Kiss) ==========

data class Interaction(
    val id: String = "",
    @SerializedName("space_id") val spaceId: String = "",
    @SerializedName("from_user_id") val fromUserId: String = "",
    @SerializedName("to_user_id") val toUserId: String = "",
    val type: String = "",
    val reason: String = "",
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created") val createdAt: String = ""
)

data class InteractionCreateRequest(
    @SerializedName("space_id") val spaceId: String,
    @SerializedName("from_user_id") val fromUserId: String,
    @SerializedName("to_user_id") val toUserId: String,
    val type: String,
    val reason: String = "",
    @SerializedName("created") val createdAt: String = ""
)

// ========== Diary ==========

data class Diary(
    val id: String = "",
    @SerializedName("collectionId") val collectionId: String = "",
    @SerializedName("space_id") val spaceId: String = "",
    @SerializedName("user_id") val userId: String = "",
    val category: String = "daily",
    val title: String = "",
    val content: String = "",
    @SerializedName("images") private val _images: Any = "[]",
    @SerializedName("image_files") private val _imageFiles: Any = "[]",
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("created") val createdAt: String = "",
    @SerializedName("updated") val updatedAt: String = "",
    val deleted: Boolean = false
) {
    val images: String
        get() = when (_images) {
            is String -> _images
            is List<*> -> com.google.gson.Gson().toJson(_images)
            else -> "[]"
        }

    fun getImageUrls(): List<String> {
        val cid = collectionId.ifEmpty { "pbc_2950295505" }
        val base = com.love.interaction.util.AppConfig.POCKETBASE_URL.trimEnd('/')
        return try {
            val list: List<String> = when (_imageFiles) {
                is String -> if (_imageFiles.isBlank() || _imageFiles == "[]") emptyList() else {
                    try { com.google.gson.Gson().fromJson(_imageFiles, Array<String>::class.java)?.toList() ?: emptyList() }
                    catch (_: Exception) { listOf(_imageFiles) }
                }
                is List<*> -> _imageFiles.filterIsInstance<String>()
                else -> emptyList()
            }
            list.map { filename -> "$base/api/files/$cid/$id/$filename" }
        } catch (_: Exception) { emptyList() }
    }
}
data class DiaryCreateRequest(
    @SerializedName("space_id") val spaceId: String,
    @SerializedName("user_id") val userId: String,
    val category: String = "daily",
    val title: String,
    val content: String,
    val images: String = "[]"
)

data class DiaryLike(
    val id: String = "",
    @SerializedName("diary_id") val diaryId: String = "",
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("created") val createdAt: String = ""
)

data class DiaryComment(
    val id: String = "",
    @SerializedName("diary_id") val diaryId: String = "",
    @SerializedName("user_id") val userId: String = "",
    val content: String = "",
    @SerializedName("created") val createdAt: String = ""
)

data class DiaryCommentCreateRequest(
    @SerializedName("diary_id") val diaryId: String,
    @SerializedName("user_id") val userId: String,
    val content: String
)

// ========== Coins ==========

data class CoinBalance(
    @SerializedName("space_id") val spaceId: String = "",
    val balance: Long = 0,
    @SerializedName("total_earned") val totalEarned: Long = 0,
    @SerializedName("total_spent") val totalSpent: Long = 0,
    @SerializedName("updated") val updatedAt: String = ""
)

data class CoinTransaction(
    val id: String = "",
    @SerializedName("space_id") val spaceId: String = "",
    @SerializedName("user_id") val userId: String = "",
    val type: String = "",  // earn or spend
    val reason: String = "",
    val amount: Long = 0,
    @SerializedName("related_id") val relatedId: String = "",
    @SerializedName("created") val createdAt: String = ""
)

data class CoinTransactionCreateRequest(
    @SerializedName("space_id") val spaceId: String,
    @SerializedName("user_id") val userId: String,
    val type: String,
    val reason: String,
    val amount: Long,
    @SerializedName("related_id") val relatedId: String = ""
)

// ========== Expense ==========

data class Expense(
    val id: String = "",
    @SerializedName("space_id") val spaceId: String = "",
    val amount: Double = 0.0,
    val category: String = "",  // food, shopping, travel, housing, entertainment, other
    val note: String = "",
    @SerializedName("paid_by") val paidBy: String = "",
    val date: String = "",
    @SerializedName("created") val createdAt: String = "",
    @SerializedName("updated") val updatedAt: String = ""
)

data class ExpenseCreateRequest(
    @SerializedName("space_id") val spaceId: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    @SerializedName("paid_by") val paidBy: String,
    val date: String
)

// ========== Wishlist ==========

data class Wishlist(
    val id: String = "",
    @SerializedName("collectionId") val collectionId: String = "",
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("space_id") val spaceId: String = "",
    val name: String = "",
    @SerializedName("target_coins") val targetCoins: Long = 0,
    @SerializedName("current_balance") val currentBalance: Long = 0,
    @SerializedName("image_url") val imageUrl: String = "",
    val status: String = "active",
    @SerializedName("fulfill_description") val fulfillDescription: String = "",
    @SerializedName("fulfill_images") private val _fulfillImages: Any = "[]",
    @SerializedName("completed_at") val completedAt: String = "",
    @SerializedName("created") val createdAt: String = "",
    @SerializedName("updated") val updatedAt: String = ""
) {
    val fulfillImages: List<String>
        get() = try {
            val list: List<String> = when (_fulfillImages) {
                is String -> if (_fulfillImages.isBlank() || _fulfillImages == "[]") emptyList() else {
                    try { com.google.gson.Gson().fromJson(_fulfillImages, Array<String>::class.java)?.toList() ?: emptyList() }
                    catch (_: Exception) { listOf(_fulfillImages) }
                }
                is List<*> -> _fulfillImages.filterIsInstance<String>()
                else -> emptyList()
            }
            list
        } catch (_: Exception) { emptyList() }

    fun getFulfillImageUrls(): List<String> {
        val cid = collectionId.ifEmpty { "pbc_3702485544" }
        val base = com.love.interaction.util.AppConfig.POCKETBASE_URL.trimEnd('/')
        return fulfillImages.map { filename -> "$base/api/files/$cid/$id/$filename" }
    }
}

data class WishlistCreateRequest(
    @SerializedName("space_id") val spaceId: String,
    @SerializedName("user_id") val userId: String = "",
    val name: String,
    @SerializedName("target_coins") val targetCoins: Long = 0,
    @SerializedName("image_url") val imageUrl: String = ""
)

data class WishlistContribution(
    val id: String = "",
    @SerializedName("wish_id") val wishId: String = "",
    @SerializedName("user_id") val userId: String = "",
    val amount: Long = 0,
    @SerializedName("created") val createdAt: String = ""
)

data class WishlistContributionCreateRequest(
    @SerializedName("wish_id") val wishId: String,
    @SerializedName("user_id") val userId: String,
    val amount: Long
)

// ========== Generic PocketBase Response ==========

data class PbListResponse<T>(
    val page: Int = 1,
    @SerializedName("perPage") val perPage: Int = 30,
    @SerializedName("totalItems") val totalItems: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0,
    val items: List<T> = emptyList()
)

// ========== Checkin Type Enum ==========

enum class CheckinType(val key: String, val label: String) {
    WAKE_UP("wake_up", "起床"),
    LEAVE_HOME("leave_home", "出门"),
    ARRIVE_OFFICE("arrive_office", "到公司"),
    LUNCH("lunch", "吃午饭"),
    OFF_WORK("off_work", "下班"),
    ARRIVE_HOME("arrive_home", "到家"),
    SLEEP("sleep", "睡觉"),
    MOOD("mood", "心情"),
    CUSTOM("custom", "自定义")
}

// ========== Diary Category Enum ==========

enum class DiaryCategory(val key: String, val label: String) {
    DAILY("daily", "日常"),
    ANNIVERSARY("anniversary", "纪念日"),
    MOOD("mood", "心情")
}

// ========== Expense Category Enum ==========

enum class ExpenseCategory(val key: String, val label: String) {
    FOOD("food", "餐饮"),
    SHOPPING("shopping", "购物"),
    TRAVEL("travel", "旅行"),
    HOUSING("housing", "房租水电"),
    ENTERTAINMENT("entertainment", "娱乐"),
    OTHER("other", "其他"),
    INCOME("income", "收入")
}

// ========== Countdown ==========

data class Countdown(
    val id: String = "",
    @SerializedName("space_id") val spaceId: String = "",
    val emoji: String = "",
    val title: String = "",
    val date: String = "",
    @SerializedName("show_on_home") val showOnHome: Boolean = false,
    @SerializedName("created") val createdAt: String = "",
    @SerializedName("updated") val updatedAt: String = ""
)

data class CountdownCreateRequest(
    @SerializedName("space_id") val spaceId: String,
    val emoji: String = "",
    val title: String,
    val date: String,
    @SerializedName("show_on_home") val showOnHome: Boolean = false
)