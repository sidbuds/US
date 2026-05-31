package com.love.interaction.ui.screens.expense

import com.love.interaction.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.interaction.data.local.CachedExpense
import com.love.interaction.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(onAddClick: () -> Unit, onBack: () -> Unit, viewModel: ExpenseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<CachedExpense?>(null) }

    val categoryLabels = mapOf(
        "food" to "\u9910\u996E", "shopping" to "\u8D2D\u7269", "travel" to "\u65C5\u884C",
        "housing" to "\u623F\u79DF\u6C34\u7535", "entertainment" to "\u5A31\u4E50", "income" to "\u6536\u5165", "other" to "\u5176\u4ED6"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.ic_expense), contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text("\u5171\u540C\u8BB0\u8D26") } },
                navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "\u8FD4\u56DE", modifier = Modifier.size(24.dp)) } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Image(painterResource(R.drawable.ic_add), contentDescription = "\u6DFB\u52A0", modifier = Modifier.size(24.dp)) } }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Summary card
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("\u6C47\u603B", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("\u603B\u652F\u51FA", style = MaterialTheme.typography.bodyMedium)
                            Text("\u00A5${"%.2f".format(uiState.totalAmount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("\u6211", style = MaterialTheme.typography.labelMedium); Text("\u00A5${"%.2f".format(uiState.myTotal)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("TA", style = MaterialTheme.typography.labelMedium); Text("\u00A5${"%.2f".format(uiState.partnerTotal)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("\u5408\u8BA1", style = MaterialTheme.typography.labelMedium); Text("${uiState.expenses.size}\u7B14", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) }
                        }
                        if (uiState.categoryTotals.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp)); HorizontalDivider(); Spacer(Modifier.height(8.dp))
                            Text("\u5206\u7C7B\u660E\u7EC6", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            uiState.categoryTotals.entries.sortedByDescending { it.value }.forEach { (cat, amount) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(categoryLabels[cat] ?: cat, style = MaterialTheme.typography.bodySmall)
                                    Text("\u00A5${"%.2f".format(amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.expenses.isEmpty() && !uiState.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("\u6682\u65E0\u8BB0\u5F55", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            items(uiState.expenses) { expense ->
                val isMine = expense.paidBy == viewModel.currentUserId
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(expense.note.ifEmpty { categoryLabels[expense.category] ?: expense.category }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) { Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
                            }
                            Text(expense.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("\u00A5${expense.amount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (isMine) {
                            IconButton(onClick = { deleteTarget = expense }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "\u5220\u9664", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
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
            text = { Text("\u786E\u5B9A\u8981\u5220\u9664\u8FD9\u6761\u8BB0\u8D26\u8BB0\u5F55\u5417\uFF1F") },
            confirmButton = { TextButton(onClick = { viewModel.deleteExpense(target.id); deleteTarget = null }) { Text("\u5220\u9664", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("\u53D6\u6D88") } }
        )
    }

    uiState.successMessage?.let { msg ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { Snackbar(modifier = Modifier.padding(16.dp), action = { TextButton(onClick = { viewModel.clearSuccess() }) { Text("\u597D\u7684") } }) { Text(msg) } }
    }
    uiState.error?.let { error ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { Snackbar(modifier = Modifier.padding(16.dp), containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer, action = { TextButton(onClick = { viewModel.clearError() }) { Text("\u77E5\u9053\u4E86") } }) { Text(error) } }
    }
}