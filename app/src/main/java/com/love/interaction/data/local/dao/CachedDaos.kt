package com.love.interaction.data.local.dao

import androidx.room.*
import com.love.interaction.data.local.CachedCheckin
import com.love.interaction.data.local.CachedDiary
import com.love.interaction.data.local.CachedDiaryComment
import com.love.interaction.data.local.CachedExpense
import com.love.interaction.data.local.CachedInteraction
import com.love.interaction.data.local.CachedWishlist
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckinDao {
    @Query("SELECT * FROM cached_checkins WHERE spaceId = :spaceId ORDER BY createdAt DESC")
    fun getCheckinsBySpace(spaceId: String): Flow<List<CachedCheckin>>

    @Query("SELECT * FROM cached_checkins WHERE spaceId = :spaceId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentCheckins(spaceId: String, limit: Int = 20): List<CachedCheckin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkins: List<CachedCheckin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkin: CachedCheckin)

    @Query("DELETE FROM cached_checkins WHERE spaceId = :spaceId")
    suspend fun clearForSpace(spaceId: String)

    @Query("DELETE FROM cached_checkins WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface InteractionDao {
    @Query("SELECT * FROM cached_interactions WHERE spaceId = :spaceId ORDER BY createdAt DESC")
    fun getInteractionsBySpace(spaceId: String): Flow<List<CachedInteraction>>

    @Query("""
        SELECT COUNT(*) FROM cached_interactions 
        WHERE spaceId = :spaceId AND type = :type AND fromUserId = :userId 
        AND createdAt >= :todayStart
    """)
    suspend fun getTodayCount(spaceId: String, userId: String, type: String, todayStart: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(interactions: List<CachedInteraction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interaction: CachedInteraction)

    @Query("UPDATE cached_interactions SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("DELETE FROM cached_interactions WHERE spaceId = :spaceId")
    suspend fun clearForSpace(spaceId: String)

    @Query("DELETE FROM cached_interactions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM cached_diaries WHERE spaceId = :spaceId AND deleted = 0 ORDER BY createdAt DESC")
    fun getDiariesBySpace(spaceId: String): Flow<List<CachedDiary>>

    @Query("SELECT * FROM cached_diaries WHERE id = :id")
    suspend fun getDiaryById(id: String): CachedDiary?

    @Query("SELECT * FROM cached_diaries WHERE userId = :userId AND deleted = 0 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentDiariesByUser(userId: String, limit: Int = 10): List<CachedDiary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(diaries: List<CachedDiary>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: CachedDiary)

    @Query("DELETE FROM cached_diaries WHERE spaceId = :spaceId")
    suspend fun clearForSpace(spaceId: String)

    @Query("DELETE FROM cached_diaries WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface DiaryCommentDao {
    @Query("SELECT * FROM cached_diary_comments WHERE diaryId = :diaryId ORDER BY createdAt ASC")
    fun getCommentsByDiary(diaryId: String): Flow<List<CachedDiaryComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CachedDiaryComment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CachedDiaryComment)

    @Query("DELETE FROM cached_diary_comments WHERE diaryId = :diaryId")
    suspend fun clearForDiary(diaryId: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM cached_expenses WHERE spaceId = :spaceId ORDER BY date DESC")
    fun getExpensesBySpace(spaceId: String): Flow<List<CachedExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<CachedExpense>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: CachedExpense)

    @Query("DELETE FROM cached_expenses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cached_expenses WHERE spaceId = :spaceId")
    suspend fun clearForSpace(spaceId: String)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM cached_wishlists WHERE spaceId = :spaceId ORDER BY createdAt DESC")
    fun getWishlistsBySpace(spaceId: String): Flow<List<CachedWishlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wishlists: List<CachedWishlist>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wishlist: CachedWishlist)

    @Query("DELETE FROM cached_wishlists WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cached_wishlists WHERE spaceId = :spaceId")
    suspend fun clearForSpace(spaceId: String)
}
