package com.love.interaction.ui.screens.diary

import com.love.interaction.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.love.interaction.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(diaryId: String, onBack: () -> Unit, viewModel: DiaryViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = viewModel.currentUserId
    var commentText by remember { mutableStateOf("") }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(diaryId) { viewModel.selectDiary(diaryId) }
    val diary = uiState.currentDiary
    val imageUrls = uiState.currentDiaryImageUrls

    if (zoomedImageUrl != null) {
        Dialog(onDismissRequest = { zoomedImageUrl = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().clickable { zoomedImageUrl = null }, contentAlignment = Alignment.Center) {
                var scale by remember { mutableFloatStateOf(1f) }; var ox by remember { mutableFloatStateOf(0f) }; var oy by remember { mutableFloatStateOf(0f) }
                Image(painter = rememberAsyncImagePainter(zoomedImageUrl), contentDescription = null, modifier = Modifier.fillMaxWidth().pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(0.5f, 5f); ox += pan.x; oy += pan.y } }.graphicsLayer(scaleX = scale, scaleY = scale, translationX = ox, translationY = oy), contentScale = ContentScale.Fit)
                IconButton(onClick = { zoomedImageUrl = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) { Image(painterResource(R.drawable.ic_close), contentDescription = null, modifier = Modifier.size(32.dp)) }
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(diary?.title ?: "\u65E5\u8BB0\u8BE6\u60C5") }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }) }) { padding ->
        if (diary == null) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 72.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { Text(when (diary.category) { "daily" -> "\uD83D\uDCCC \u65E5\u5E38"; "anniversary" -> "\uD83C\uDF82 \u7EAA\u5FF5\u65E5"; "mood" -> "\uD83E\uDD70 \u5FC3\u60C5"; else -> "\uD83D\uDCDD \u65E5\u8BB0" }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodyMedium) }
                    Text(diary.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(com.love.interaction.util.TimeUtils.formatCreatedAt(diary.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp)); val isMine = diary.userId == currentUserId; Surface(shape = RoundedCornerShape(8.dp), color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) { Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) }
                    }
                    Spacer(Modifier.height(16.dp)); Text(diary.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 16.dp))
                    if (imageUrls.isNotEmpty()) { Spacer(Modifier.height(16.dp)); Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { imageUrls.chunked(3).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { row.forEach { url -> Image(painter = rememberAsyncImagePainter(url), contentDescription = null, modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable { zoomedImageUrl = url }, contentScale = ContentScale.Crop) }; repeat(3 - row.size) { Spacer(Modifier.weight(1f)) } } } } }
                    Spacer(Modifier.height(16.dp)); HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(8.dp))
                    Text("\u8BC4\u8BBA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    if (uiState.comments.isEmpty()) Text("\u6682\u65E0\u8BC4\u8BBA", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    uiState.comments.forEach { comment -> val isMine = comment.userId == currentUserId; Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isMine) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) { Column(Modifier.padding(12.dp)) { Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary); Spacer(Modifier.height(4.dp)); Text(comment.content, style = MaterialTheme.typography.bodyMedium); Text(com.love.interaction.util.TimeUtils.formatCreatedAt(comment.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
                    Spacer(Modifier.height(16.dp))
                }
                Surface(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter), shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = commentText, onValueChange = { commentText = it }, placeholder = { Text("\u5199\u8BC4\u8BBA...") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(24.dp))
                        Spacer(Modifier.width(8.dp)); IconButton(onClick = { if (commentText.isNotBlank()) { viewModel.addComment(diaryId, commentText); commentText = "" } }) { Image(painterResource(R.drawable.ic_send), contentDescription = "发送", modifier = Modifier.size(24.dp)) }
                    }
                }
            }
        }
    }
}
