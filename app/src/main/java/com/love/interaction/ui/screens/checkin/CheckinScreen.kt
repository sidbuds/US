package com.love.interaction.ui.screens.checkin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import com.love.interaction.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.interaction.data.local.CachedCheckin
import com.love.interaction.data.model.CheckinType
import com.love.interaction.viewmodel.CheckinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScreen(viewModel: CheckinViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var customContent by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<CachedCheckin?>(null) }

    LaunchedEffect(Unit) { android.util.Log.d("CheckinScreen", "LaunchedEffect: calling startAutoRefresh"); viewModel.startAutoRefresh() }
    DisposableEffect(Unit) { onDispose { viewModel.stopAutoRefresh() } }

    Scaffold(topBar = {
        TopAppBar(title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_baobei), contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("\u62A5\u5907")
            }
        })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val types = listOf(CheckinType.WAKE_UP, CheckinType.LEAVE_HOME, CheckinType.ARRIVE_OFFICE, CheckinType.LUNCH, CheckinType.OFF_WORK, CheckinType.ARRIVE_HOME, CheckinType.SLEEP)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(types.chunked(3)) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { type ->
                            OutlinedButton(onClick = { viewModel.sendCheckin(type) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                Text(type.label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = customContent, onValueChange = { customContent = it }, label = { Text("\u81EA\u5B9A\u4E49\u62A5\u5907") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), trailingIcon = {
                        IconButton(onClick = { if (customContent.isNotBlank()) { viewModel.sendCheckin(CheckinType.CUSTOM, customContent); customContent = "" } }) {
                            Image(painterResource(R.drawable.ic_send), contentDescription = "\u53D1\u9001", modifier = Modifier.size(24.dp))
                        }
                    })
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Text("\u62A5\u5907\u8BB0\u5F55", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(uiState.checkins) { checkin ->
                    val isMine = checkin.userId == viewModel.currentUserId
                    val label = when (checkin.type) {
                        "wake_up" -> "\u8D77\u5E8A"; "leave_home" -> "\u51FA\u95E8"; "arrive_office" -> "\u5230\u516C\u53F8"; "lunch" -> "\u5403\u5348\u996D"; "off_work" -> "\u4E0B\u73ED"; "arrive_home" -> "\u5230\u5BB6"; "sleep" -> "\u7761\u89C9"; "mood" -> "\u5FC3\u60C5"; else -> checkin.customContent.ifEmpty { checkin.type }
                    }
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(8.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) {
                                        Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(com.love.interaction.util.TimeUtils.formatCreatedAt(checkin.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (isMine) {
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = { deleteTarget = checkin }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "\u5220\u9664", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("\u786E\u8BA4\u5220\u9664") },
            text = { Text("\u786E\u5B9A\u8981\u5220\u9664\u8FD9\u6761\u62A5\u5907\u8BB0\u5F55\u5417\uFF1F") },
            confirmButton = { TextButton(onClick = { viewModel.deleteCheckin(target.id); deleteTarget = null }) { Text("\u5220\u9664", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("\u53D6\u6D88") } }
        )
    }

    // Success/Error snackbars
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
}