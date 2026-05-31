package com.love.interaction.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.love.interaction.ui.screens.countdown.CountdownScreen
import com.love.interaction.ui.screens.diary.DiaryDetailScreen
import com.love.interaction.ui.screens.diary.DiaryWriteScreen
import com.love.interaction.ui.screens.expense.ExpenseAddScreen
import com.love.interaction.ui.screens.expense.ExpenseListScreen
import com.love.interaction.ui.screens.home.HomeScreen
import com.love.interaction.ui.screens.checkin.CheckinScreen
import com.love.interaction.ui.screens.interaction.InteractionScreen
import com.love.interaction.ui.screens.diary.DiaryScreen
import com.love.interaction.ui.screens.login.IdentitySelectScreen
import com.love.interaction.ui.screens.more.MoreScreen
import com.love.interaction.ui.screens.more.CoinHistoryScreen
import com.love.interaction.ui.screens.more.WishlistScreen
import com.love.interaction.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    authViewModel: AuthViewModel,
    heroTitle: MutableState<String>,
    heroDays: MutableState<Int>,
    heroEmoji: MutableState<String>
) {
    val start = if (isLoggedIn) Screen.Home.route else Screen.IdentitySelect.route

    NavHost(navController = navController, startDestination = start) {
        composable(Screen.IdentitySelect.route) {
            IdentitySelectScreen(onIdentitySelected = { lover ->
                authViewModel.selectIdentity(lover)
                navController.navigate(Screen.Home.route) { popUpTo(Screen.IdentitySelect.route) { inclusive = true } }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(onNavigate = { navController.navigate(it.route) }, authViewModel = authViewModel, heroTitle = heroTitle, heroDays = heroDays, heroEmoji = heroEmoji)
        }
        composable(Screen.Checkin.route) { CheckinScreen() }
        composable(Screen.Interaction.route) { InteractionScreen() }
        composable(Screen.Diary.route) {
            DiaryScreen(onDiaryClick = { navController.navigate(Screen.DiaryDetail.createRoute(it)) }, onWriteClick = { navController.navigate(Screen.DiaryWrite.route) })
        }
        composable(Screen.More.route) {
            MoreScreen(onNavigate = { navController.navigate(it.route) }, authViewModel = authViewModel)
        }
        composable(Screen.DiaryDetail.route, arguments = listOf(navArgument("diaryId") { type = NavType.StringType })) {
            DiaryDetailScreen(diaryId = it.arguments?.getString("diaryId") ?: "", onBack = { navController.popBackStack() })
        }
        composable(Screen.DiaryWrite.route) { DiaryWriteScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.ExpenseList.route) { ExpenseListScreen(onAddClick = { navController.navigate(Screen.ExpenseAdd.route) }, onBack = { navController.popBackStack() }) }
        composable(Screen.ExpenseAdd.route) { ExpenseAddScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.WishlistPage.route) { WishlistScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.CoinHistory.route) { CoinHistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Countdown.route) {
            CountdownScreen(onBack = { navController.popBackStack() }, heroTitle = heroTitle, heroDays = heroDays, heroEmoji = heroEmoji)
        }
    }
}
