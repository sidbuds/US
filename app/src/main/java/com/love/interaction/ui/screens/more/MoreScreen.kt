package com.love.interaction.ui.screens.more

import com.love.interaction.R

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.love.interaction.ui.navigation.Screen
import com.love.interaction.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(onNavigate: (Screen) -> Unit, authViewModel: AuthViewModel) {
    val uiState by authViewModel.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("\u66F4\u591A") })
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val avatarRes = when (uiState.currentLover) {
    com.love.interaction.viewmodel.Lover.LONGTENG -> R.drawable.avatar_male
    com.love.interaction.viewmodel.Lover.YANHUIXIN -> R.drawable.avatar_female
    else -> R.drawable.avatar_male
}
Image(painterResource(avatarRes), contentDescription = null, modifier = Modifier.size(56.dp).clip(CircleShape)); Spacer(modifier = Modifier.width(16.dp))
                Column { Text(uiState.currentLover?.displayName ?: "\u7528\u6237", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("\u5DF2\u767B\u5F55", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        MenuItemCard(R.drawable.ic_expense, "\u5171\u540C\u8BB0\u8D26", "\u8BB0\u5F55\u5171\u540C\u5F00\u9500") { onNavigate(Screen.ExpenseList) }
        MenuItemCard(R.drawable.ic_wishlist, "\u613F\u671B\u6E05\u5355", "\u4E00\u8D77\u8BB8\u4E0B\u5FC3\u613F") { onNavigate(Screen.WishlistPage) }
        MenuItemCard(R.drawable.ic_countdown, "\u5012\u6570\u65E5", "\u751F\u65E5\u3001\u7EAA\u5FF5\u65E5") { onNavigate(Screen.Countdown) }
        Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)); Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)), onClick = { authViewModel.logout() }) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_logout), contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(16.dp))
                Text("\u5207\u6362\u8EAB\u4EFD", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemCard(iconRes: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Image(painterResource(R.drawable.ic_expand), contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}
