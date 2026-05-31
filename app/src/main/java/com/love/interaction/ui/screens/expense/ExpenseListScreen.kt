package com.love.interaction.ui.screens.expense

import com.love.interaction.R


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.interaction.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(onAddClick: () -> Unit, onBack: () -> Unit, viewModel: ExpenseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.ic_expense), contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text("\u5171\u540C\u8BB0\u8D26") } }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }, actions = { IconButton(onClick = onAddClick) { Image(painterResource(R.drawable.ic_add), contentDescription = "添加", modifier = Modifier.size(24.dp)) } }) }, floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Image(painterResource(R.drawable.ic_add), contentDescription = "添加", modifier = Modifier.size(24.dp)) } }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (uiState.expenses.isEmpty() && !uiState.isLoading) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("\u6682\u65E0\u8BB0\u5F55", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            items(uiState.expenses) { expense -> val isMine = expense.paidBy == viewModel.currentUserId; Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(expense.note.ifEmpty { expense.category }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); Spacer(Modifier.width(8.dp)); Surface(shape = RoundedCornerShape(4.dp), color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) { Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } }; Text(expense.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("\u00A5${expense.amount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) } } }
        }
    }
}
