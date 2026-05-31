package com.love.interaction.ui.screens.home

import com.love.interaction.R

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.love.interaction.ui.navigation.Screen
import com.love.interaction.ui.theme.*
import com.love.interaction.viewmodel.AuthViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit, authViewModel: AuthViewModel, heroTitle: MutableState<String>, heroDays: MutableState<Int>, heroEmoji: MutableState<String>) {
    val uiState by authViewModel.uiState.collectAsState()
    val meName = uiState.currentLover?.displayName ?: "\u6211"
    val meEmoji = uiState.currentLover?.emoji ?: "\uD83D\uDC64"
    val partnerName = uiState.partnerLover?.displayName ?: "\u53E6\u4E00\u534A"
    val partnerEmoji = uiState.partnerLover?.emoji ?: "\uD83D\uDC64"
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("countdown_prefs", Context.MODE_PRIVATE) }
    val anniversaryDate = remember { val saved = prefs.getString("anniversary_date", null); if (saved != null) { try { LocalDate.parse(saved) } catch (_: Exception) { null } } else null } ?: LocalDate.of(2024, 1, 1)
    val daysTogether = ChronoUnit.DAYS.between(anniversaryDate, LocalDate.now()).toInt()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Pink40, Pink60, Lavender40))).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.ic_dianzanman), contentDescription = null, modifier = Modifier.size(36.dp)); Spacer(Modifier.width(8.dp)); Text("\u79D8\u5BC6\u57FA\u5730", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Pink80), contentAlignment = Alignment.Center) { val myAvatar = if (uiState.currentLover?.email == "yanhuixin@love.local") R.drawable.avatar_female else R.drawable.avatar_male; Image(painterResource(myAvatar), contentDescription = meName, modifier = Modifier.size(48.dp).clip(CircleShape)) }; Spacer(Modifier.height(4.dp)); Text(meName, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium) }
                        Text("\u2764\uFE0F", fontSize = 28.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Lavender80), contentAlignment = Alignment.Center) { val partnerAvatar = if (uiState.currentLover?.email == "yanhuixin@love.local") R.drawable.avatar_male else R.drawable.avatar_female; Image(painterResource(partnerAvatar), contentDescription = partnerName, modifier = Modifier.size(48.dp).clip(CircleShape)) }; Spacer(Modifier.height(4.dp)); Text(partnerName, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Pink80.copy(alpha = 0.12f))) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("\u23F0", fontSize = 20.sp); Spacer(Modifier.width(8.dp)); Text("\u5728\u4E00\u8D77\u5DF2\u7ECF ", style = MaterialTheme.typography.titleMedium)
                Text("$daysTogether", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text(" \u5929", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(10.dp))
        if (heroTitle.value.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Lavender80.copy(alpha = 0.12f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(heroEmoji.value, fontSize = 28.sp); Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) { Text(heroTitle.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("\u8DDD\u79BB\u8FD8\u6709", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${heroDays.value}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text("\u5929", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onNavigate(Screen.Countdown) }, modifier = Modifier.size(32.dp)) { Image(painterResource(R.drawable.ic_expand), null, modifier = Modifier.size(24.dp)) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("\u5FEB\u6377\u64CD\u4F5C", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QAC(R.drawable.ic_baobei, "\u62A5\u5907", Pink40, Modifier.weight(1f)) { onNavigate(Screen.Checkin) }
            QAC(R.drawable.ic_interaction, "\u4E92\u52A8", Coral40, Modifier.weight(1f)) { onNavigate(Screen.Interaction) }
            QAC(R.drawable.ic_diary, "\u65E5\u8BB0", Lavender40, Modifier.weight(1f)) { onNavigate(Screen.Diary) }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QAC(R.drawable.ic_expense, "\u8BB0\u8D26", Success, Modifier.weight(1f)) { onNavigate(Screen.ExpenseList) }
            QAC(R.drawable.ic_wishlist, "\u613F\u671B", Gold, Modifier.weight(1f)) { onNavigate(Screen.WishlistPage) }
            QAC(R.drawable.ic_countdown, "\u5012\u6570\u65E5", Lavender40, Modifier.weight(1f)) { onNavigate(Screen.Countdown) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun QAC(iconRes: Int, label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(iconRes), contentDescription = label, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(4.dp)); Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
