package com.love.interaction.ui.screens.interaction

import com.love.interaction.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import coil.compose.rememberAsyncImagePainter
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.interaction.ui.components.LoveAnimation
import com.love.interaction.viewmodel.InteractionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionScreen(viewModel: InteractionViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showMissYouDialog by remember { mutableStateOf(false) }
    var missYouReason by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = "\uD83D\uDC96 \u4E92\u52A8",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Stats
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("\uD83E\uDD17", "\u62B1\u62B1", uiState.hugCount, Modifier.weight(1f))
                    StatCard("\uD83D\uDE18", "\u4EB2\u4EB2", uiState.kissCount, Modifier.weight(1f))
                    StatCard("\uD83D\uDE22", "\u60F3\u4F60", uiState.interactions.count { it.type == "miss" }, Modifier.weight(1f))
                }
            }

            // Action buttons
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.sendHug() },
                            modifier = Modifier.weight(1f).height(72.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !uiState.isLoading
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\uD83E\uDD17", style = MaterialTheme.typography.headlineSmall)
                                Text("\u62B1\u62B1", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        Button(
                            onClick = { viewModel.sendKiss() },
                            modifier = Modifier.weight(1f).height(72.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            enabled = !uiState.isLoading
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\uD83D\uDE18", style = MaterialTheme.typography.headlineSmall)
                                Text("\u4EB2\u4EB2", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                    // "Miss you" button — opens reason dialog
                    Button(
                        onClick = { showMissYouDialog = true },
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        enabled = !uiState.isLoading
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("\uD83D\uDE22", style = MaterialTheme.typography.headlineSmall)
                            Text("\u60F3\u4F60\u4E86", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Text("\u4E92\u52A8\u8BB0\u5F55", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            if (uiState.interactions.isEmpty()) {
                item {
                    Text("\u8FD8\u6CA1\u6709\u4E92\u52A8\u8BB0\u5F55\n\u70B9\u51FB\u4E0A\u65B9\u6309\u94AE\u5F00\u59CB\u5427~", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                }
            } else {
                items(uiState.interactions) { interaction ->
                    val isMine = interaction.fromUserId == viewModel.currentUserId
                    val emoji = when (interaction.type) {
                        "hug" -> "\uD83E\uDD17"
                        "kiss" -> "\uD83D\uDE18"
                        "miss" -> "\uD83D\uDE22"
                        else -> "\u2764\uFE0F"
                    }
                    val label = when (interaction.type) {
                        "hug" -> "\u62B1\u62B1"
                        "kiss" -> "\u4EB2\u4EB2"
                        "miss" -> "\u60F3\u4F60"
                        else -> "\u4E92\u52A8"
                    }
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isMine) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall,
                                        color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.background(if (isMine) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (interaction.type == "miss" && interaction.reason.isNotEmpty()) {
                                    Text("\uD83D\uDCCD " + interaction.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(com.love.interaction.util.TimeUtils.formatCreatedAt(interaction.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Miss you dialog with reason
        if (showMissYouDialog) {
            AlertDialog(
                onDismissRequest = { showMissYouDialog = false; missYouReason = "" },
                title = { Text("\u60F3\u4F60\u4E86") },
                text = {
                    OutlinedTextField(
                        value = missYouReason,
                        onValueChange = { missYouReason = it },
                        label = { Text("\u4E3A\u4EC0\u4E48\u60F3\u4F60\uFF1F\uFF08\u53EF\u9009\uFF09") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.sendMissYou(missYouReason)
                        missYouReason = ""
                        showMissYouDialog = false
                    }) { Text("\u53D1\u9001") }
                },
                dismissButton = {
                    TextButton(onClick = { showMissYouDialog = false; missYouReason = "" }) { Text("\u53D6\u6D88") }
                }
            )
        }

        // Animation overlay
        AnimatedVisibility(visible = uiState.showAnimation, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            LoveAnimation(type = uiState.animationType, modifier = Modifier.fillMaxSize())
            LaunchedEffect(uiState.showAnimation) {
                if (uiState.showAnimation) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.hideAnimation()
                }
            }
        }

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

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun StatCard(emoji: String, label: String, count: Int, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.headlineLarge)
            Text("$count \u6B21", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}



