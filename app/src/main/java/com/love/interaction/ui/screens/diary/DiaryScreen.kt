package com.love.interaction.ui.screens.diary

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import com.love.interaction.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
    var deleteTarget by remember { mutableStateOf<CachedDiary?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.ic_diary), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("\u65E5\u8BB0")
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onWriteClick) {
                Image(painterResource(R.drawable.ic_write_diary), contentDescription = "\u5199\u65E5\u8BB0", modifier = Modifier.size(24.dp))
            }
        }
    ) { padding ->
        if (uiState.diaries.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("\uD83D\uDCD6", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("\u8FD8\u6CA1\u6709\u65E5\u8BB0\n\u70B9\u51FB\u53F3\u4E0B\u89D2\u6309\u94AE\u5F00\u59CB\u5199\u7B2C\u4E00\u7BC7\u65E5\u8BB0\u5427", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.diaries) { diary ->
                    DiaryCard(
                        diary = diary,
                        isMine = diary.userId == currentUserId,
                        onClick = { onDiaryClick(diary.id) },
                        onDelete = { deleteTarget = diary }
                    )
                }
            }
        }
    }

    // Success/Error
    uiState.successMessage?.let { msg ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(modifier = Modifier.padding(16.dp), action = { TextButton(onClick = { viewModel.clearSuccess() }) { Text("\u597D\u7684") } }) { Text(msg) }
        }
    }
    uiState.error?.let { error ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(modifier = Modifier.padding(16.dp), containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer, action = { TextButton(onClick = { viewModel.clearError() }) { Text("\u77E5\u9053\u4E86") } }) { Text(error) }
        }
    }

    // Delete confirmation dialog
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("\u786E\u8BA4\u5220\u9664") },
            text = { Text("\u786E\u5B9A\u8981\u5220\u9664\u65E5\u8BB0\u300C${target.title}\u300D\u5417\uFF1F") },
            confirmButton = { TextButton(onClick = { viewModel.deleteDiary(target.id); deleteTarget = null }) { Text("\u5220\u9664", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("\u53D6\u6D88") } }
        )
    }
}

@Composable
private fun DiaryCard(diary: CachedDiary, isMine: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    val categoryEmoji = when (diary.category) { "daily" -> "\uD83D\uDCCC"; "anniversary" -> "\uD83C\uDF82"; "mood" -> "\uD83E\uDD70"; else -> "\uD83D\uDCDD" }
    val categoryLabel = when (diary.category) { "daily" -> "\u65E5\u5E38"; "anniversary" -> "\u7EAA\u5FF5\u65E5"; "mood" -> "\u5FC3\u60C5"; else -> "\u65E5\u8BB0" }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) { Text("$categoryEmoji $categoryLabel", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) { Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) }
                    if (isMine) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "\u5220\u9664", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(diary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(diary.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(com.love.interaction.util.TimeUtils.formatCreatedAt(diary.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}