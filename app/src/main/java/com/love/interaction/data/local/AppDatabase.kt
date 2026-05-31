package com.love.interaction.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.love.interaction.data.local.dao.*

@Database(
    entities = [
        CachedCheckin::class,
        CachedInteraction::class,
        CachedDiary::class,
        CachedDiaryComment::class,
        CachedExpense::class,
        CachedWishlist::class,
        UserSession::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun checkinDao(): CheckinDao
    abstract fun interactionDao(): InteractionDao
    abstract fun diaryDao(): DiaryDao
    abstract fun diaryCommentDao(): DiaryCommentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "love_interaction.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

