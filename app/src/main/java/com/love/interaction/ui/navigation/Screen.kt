package com.love.interaction.ui.navigation

sealed class Screen(val route: String) {
    // Identity selection (replaces login)
    data object IdentitySelect : Screen("identity_select")

    // Main tabs
    data object Home : Screen("home")
    data object Checkin : Screen("checkin")
    data object Interaction : Screen("interaction")
    data object Diary : Screen("diary")
    data object More : Screen("more")

    // Detail screens
    data object DiaryDetail : Screen("diary_detail/{diaryId}") {
        fun createRoute(diaryId: String) = "diary_detail/$diaryId"
    }
    data object DiaryWrite : Screen("diary_write")
    data object ExpenseList : Screen("expense_list")
    data object ExpenseAdd : Screen("expense_add")
    data object WishlistPage : Screen("wishlist")
    data object CoinHistory : Screen("coin_history")
    data object Countdown : Screen("countdown")
}
