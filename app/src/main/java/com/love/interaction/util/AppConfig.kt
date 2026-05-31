package com.love.interaction.util

import com.love.interaction.BuildConfig

object AppConfig {
    // PocketBase server URL
    const val POCKETBASE_URL = BuildConfig.POCKETBASE_URL

    // Image limits
    const val IMAGE_MAX_SIZE_MB = 5
    const val IMAGE_MAX_COUNT = 9

    // Content limits
    const val DIARY_MAX_LENGTH = 5000

    // Coin rewards
    const val COIN_CHECKIN_REWARD = 5
    const val COIN_DIARY_REWARD = 20
    const val COIN_LIKE_REWARD = 2
    const val COIN_COMMENT_REWARD = 5
    const val COIN_HUG_COST = 5
    const val COIN_HUG_RECEIVE = 15
    const val COIN_KISS_COST = 5
    const val COIN_KISS_RECEIVE = 15
    const val COIN_DINNER_COST = 100
    const val COIN_DINNER_REWARD = 20
    const val COIN_LOGIN_REWARD = 10
    const val COIN_WISH_REWARD = 30
    const val COIN_EXPENSE_REWARD = 5
    const val COIN_CONSECUTIVE_BONUS = 50
    const val COIN_DIARY_STREAK_BONUS = 100

    // Daily limits
    const val DAILY_HUG_LIMIT = 10
    const val DAILY_KISS_LIMIT = 10
    const val DAILY_CHECKIN_LIMIT = 7
    const val DAILY_DIARY_LIMIT = 2
    const val DAILY_LIKE_LIMIT = 10
    const val DAILY_COMMENT_LIMIT = 5
    const val DAILY_DINNER_LIMIT = 3
    const val DAILY_EXPENSE_LIMIT = 5

    // Couple code
    const val COUPLE_CODE_LENGTH = 6
    const val COUPLE_CODE_EXPIRY_HOURS = 24

    // Interaction
    const val CONSECUTIVE_DAYS_BONUS = 3
    const val DIARY_STREAK_DAYS = 7
}
