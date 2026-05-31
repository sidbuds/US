package com.love.interaction.ui.screens.expense

import com.love.interaction.R



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.interaction.data.model.ExpenseCategory
import com.love.interaction.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddScreen(onBack: () -> Unit, viewModel: ExpenseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }; var showCategoryMenu by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.successMessage) { if (uiState.successMessage != null) { kotlinx.coroutines.delay(1000); onBack() } }
    Scaffold(topBar = { TopAppBar(title = { Text("\u8BB0\u4E00\u7B14") }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }, actions = { TextButton(onClick = { val amt = amount.toDoubleOrNull(); if (amt != null && amt > 0) { val finalAmount = if (isIncome) -amt else amt; viewModel.addExpense(finalAmount, selectedCategory, note, java.time.LocalDate.now().toString()) } }, enabled = amount.isNotBlank()) { Text("\u4FDD\u5B58") } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row { FilterChip(selected = !isIncome, onClick = { isIncome = false }, label = { Text("\u652F\u51FA") }); Spacer(Modifier.width(8.dp)); FilterChip(selected = isIncome, onClick = { isIncome = true }, label = { Text("\u6536\u5165") }) }
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("\u91D1\u989D") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = it }) { OutlinedTextField(value = selectedCategory.label, onValueChange = {}, readOnly = true, label = { Text("\u5206\u7C7B") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp)); ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) { ExpenseCategory.entries.forEach { cat -> DropdownMenuItem(text = { Text(cat.label) }, onClick = { selectedCategory = cat; showCategoryMenu = false }) } } }
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("\u5907\u6CE8") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        }
    }
}
