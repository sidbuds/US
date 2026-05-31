package com.love.interaction.ui.screens.more

import com.love.interaction.R


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.love.interaction.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinHistoryScreen(onBack: () -> Unit, viewModel: ExpenseViewModel = viewModel()) {
    Scaffold(topBar = { TopAppBar(title = { Text("\u91D1\u5E01\u8BB0\u5F55") }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("\u91D1\u5E01\u8BB0\u5F55\u529F\u80FD\u5F00\u53D1\u4E2D", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
