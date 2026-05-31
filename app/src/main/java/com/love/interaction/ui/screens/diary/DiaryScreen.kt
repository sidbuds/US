package com.love.interaction.ui.screens.diary


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import com.love.interaction.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.interaction.data.local.CachedDiary
import com.love.interaction.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(onDiaryClick: (String) -> Unit, onWriteClick: () -> Unit, viewModel: DiaryViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = viewModel.currentUserId
    Scaffold(topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.ic_diary), contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text("\u65E5\u8BB0") } }) }, floatingActionButton = { FloatingActionButton(onClick = onWriteClick) { Image(painterResource(R.drawable.ic_write_diary), contentDescription = "写日记", modifier = Modifier.size(24.dp)) } }) { padding ->
        if (uiState.diaries.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("\uD83D\uDCD6", style = MaterialTheme.typography.displayLarge); Spacer(modifier = Modifier.height(8.dp)); Text("\u8FD8\u6CA1\u6709\u65E5\u8BB0\n\u70B9\u51FB\u53F3\u4E0B\u89D2\u6309\u94AE\u5F00\u59CB\u5199\u7B2C\u4E00\u7BC7\u65E5\u8BB0\u5427", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.diaries) { diary -> DiaryCard(diary = diary, isMine = diary.userId == currentUserId, onClick = { onDiaryClick(diary.id) }) }
            }
        }
    }
}

@Composable
private fun DiaryCard(diary: CachedDiary, isMine: Boolean, onClick: () -> Unit) {
    val categoryEmoji = when (diary.category) { "daily" -> "\uD83D\uDCCC"; "anniversary" -> "\uD83C\uDF82"; "mood" -> "\uD83E\uDD70"; else -> "\uD83D\uDCDD" }
    val categoryLabel = when (diary.category) { "daily" -> "\u65E5\u5E38"; "anniversary" -> "\u7EAA\u5FF5\u65E5"; "mood" -> "\u5FC3\u60C5"; else -> "\u65E5\u8BB0" }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) { Text("$categoryEmoji $categoryLabel", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                Surface(shape = RoundedCornerShape(8.dp), color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) { Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) }
            }
            Spacer(modifier = Modifier.height(8.dp)); Text(diary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp)); Text(diary.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp)); Text(com.love.interaction.util.TimeUtils.formatCreatedAt(diary.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
