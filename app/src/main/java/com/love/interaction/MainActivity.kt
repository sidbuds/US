package com.love.interaction

import com.love.interaction.R

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.love.interaction.ui.navigation.AppNavGraph
import com.love.interaction.ui.navigation.Screen
import com.love.interaction.ui.theme.LoveInteractionTheme
import com.love.interaction.viewmodel.AuthViewModel
import com.love.interaction.viewmodel.GlobalPopupViewModel

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate called, savedInstanceState=${savedInstanceState != null}")
        setContent {
            LoveInteractionTheme {
                val authViewModel: AuthViewModel = viewModel()
                val globalPopupViewModel: GlobalPopupViewModel = viewModel()
                val uiState by authViewModel.uiState.collectAsState()

                Log.d(TAG, "RECOMPOSE: isLoading=${uiState.isLoading}, isLoggedIn=${uiState.isLoggedIn}, error=${uiState.error}")

                // Step 1: If loading, show heart
                if (uiState.isLoading) {
                    Log.d(TAG, "SHOWING: Loading heart")
                    Scaffold { pv ->
                        Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                            Text("\u2764\uFE0F", fontSize = 64.sp)
                        }
                    }
                    return@LoveInteractionTheme
                }

                // Step 2: Not logged in -> identity select
                if (!uiState.isLoggedIn) {
                    Log.d(TAG, "SHOWING: IdentitySelectScreen")
                    com.love.interaction.ui.screens.login.IdentitySelectScreen(
                        onIdentitySelected = { lover ->
                            Log.d(TAG, "IdentitySelected callback: ${lover.displayName}")
                            authViewModel.selectIdentity(lover)
                        }
                    )
                    return@LoveInteractionTheme
                }

                // Auto-refresh when app comes to foreground
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            Log.d(TAG, "ON_RESUME -> triggering refresh")
                            com.love.interaction.data.remote.RealtimeManager.triggerRefresh()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Step 3: Logged in -> main app with nav
                Log.d(TAG, "SHOWING: Main app (NavHost)")
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.destination?.route

                data class NI(val label: String, val iconRes: Int?, val emoji: String, val route: String)
                val tabs = listOf(
                    NI("\u4E3B\u9875", null, "\uD83C\uDFE0", Screen.Home.route),
                    NI("\u62A5\u5907", R.drawable.ic_baobei, "\u2705", Screen.Checkin.route),
                    NI("\u4E92\u52A8", R.drawable.ic_interaction, "\u2764\uFE0F", Screen.Interaction.route),
                    NI("\u65E5\u8BB0", R.drawable.ic_diary, "\uD83D\uDCD6", Screen.Diary.route),
                    NI("\u66F4\u591A", null, "\u2699\uFE0F", Screen.More.route)
                )
                val showBar = currentRoute in tabs.map { it.route }

                Scaffold(
                    bottomBar = {
                        if (showBar) {
                            NavigationBar {
                                tabs.forEach { t ->
                                    NavigationBarItem(
                                        icon = {
    if (t.iconRes != null) {
        Image(painterResource(t.iconRes), contentDescription = t.label, modifier = Modifier.size(28.dp))
    } else {
        Text(t.emoji, fontSize = 20.sp)
    }
},
                                        label = { Text(t.label) },
                                        selected = currentRoute == t.route,
                                        onClick = {
                                            if (t.route == Screen.Home.route) {
                                                navController.popBackStack(Screen.Home.route, inclusive = false)
                                            } else if (currentRoute != t.route) {
                                                navController.navigate(t.route) {
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { pv ->
                    Box(Modifier.fillMaxSize().padding(pv)) {
                    val heroTitle = remember { mutableStateOf("") }
                    val heroDays = remember { mutableStateOf(0) }
                    val heroEmoji = remember { mutableStateOf("\uD83C\uDF89") }

                    AppNavGraph(
                            navController = navController,
                            isLoggedIn = uiState.isLoggedIn,
                            authViewModel = authViewModel,
                            globalPopupViewModel = globalPopupViewModel,
                            heroTitle = heroTitle,
                            heroDays = heroDays,
                            heroEmoji = heroEmoji
                        )

                        // Global GIF popup overlay
                        val globalPopupState by globalPopupViewModel.state.collectAsState()
                        Log.d(TAG, "GlobalPopup: show=${globalPopupState.showGifPopup}, gifResId=${globalPopupState.gifResId}")
                        if (globalPopupState.showGifPopup && globalPopupState.gifResId != 0) {
                            Log.d(TAG, "Rendering GIF popup Dialog with resId=${globalPopupState.gifResId}")
                            Dialog(
                                onDismissRequest = { globalPopupViewModel.dismissGifPopup() },
                                properties = DialogProperties(usePlatformDefaultWidth = false)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().clickable { globalPopupViewModel.dismissGifPopup() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = coil.compose.rememberAsyncImagePainter(
                                            model = globalPopupState.gifResId,
                                            imageLoader = coil.ImageLoader.Builder(LocalContext.current)
                                                .components { add(coil.decode.GifDecoder.Factory()) }
                                                .build()
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth(0.85f),
                                        contentScale = ContentScale.Fit
                                    )
                                    LaunchedEffect(globalPopupState.showGifPopup) {
                                        kotlinx.coroutines.delay(4000)
                                        globalPopupViewModel.dismissGifPopup()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
