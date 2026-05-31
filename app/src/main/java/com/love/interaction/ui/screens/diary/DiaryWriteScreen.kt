package com.love.interaction.ui.screens.diary

import com.love.interaction.R



import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.love.interaction.data.model.DiaryCategory
import com.love.interaction.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryWriteScreen(onBack: () -> Unit, viewModel: DiaryViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }; var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DiaryCategory.DAILY) }; var showCategoryMenu by remember { mutableStateOf(false) }
    val selectedImages = remember { mutableStateListOf<Uri>() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris -> selectedImages.clear(); selectedImages.addAll(uris.take(9)) }
    LaunchedEffect(uiState.successMessage) { if (uiState.successMessage != null) { kotlinx.coroutines.delay(1500); onBack() } }
    uiState.error?.let { error -> AlertDialog(onDismissRequest = { viewModel.clearError() }, title = { Text("\u53D1\u5E03\u5931\u8D25") }, text = { Text(error) }, confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("\u786E\u5B9A") } }) }
    Scaffold(topBar = { TopAppBar(title = { Text("\u5199\u65E5\u8BB0") }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }, actions = { TextButton(onClick = { if (title.isNotBlank() && content.isNotBlank()) viewModel.createDiary(title, content, selectedCategory, selectedImages.toList()) }, enabled = title.isNotBlank() && content.isNotBlank() && !uiState.isLoading) { Text("\u53D1\u5E03") } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = it }) { OutlinedTextField(value = selectedCategory.label, onValueChange = {}, readOnly = true, label = { Text("\u5206\u7C7B") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp)); ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) { DiaryCategory.entries.forEach { cat -> DropdownMenuItem(text = { Text(cat.label) }, onClick = { selectedCategory = cat; showCategoryMenu = false }) } } }
            Spacer(modifier = Modifier.height(12.dp)); OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("\u6807\u9898") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(12.dp)); OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("\u5185\u5BB9\uFF08\u6700\u591A5000\u5B57\uFF09") }, modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(12.dp), maxLines = Int.MAX_VALUE)
            Spacer(modifier = Modifier.height(8.dp)); Text("${content.length}/5000", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("\u56FE\u7247\uFF08\u6700\u591A9\u5F20\uFF09", style = MaterialTheme.typography.titleSmall); IconButton(onClick = { photoPicker.launch("image/*") }) { Image(painterResource(R.drawable.ic_add), contentDescription = "添加", modifier = Modifier.size(24.dp)) } }
            if (selectedImages.isNotEmpty()) { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(selectedImages) { uri -> Box { Image(painter = rememberAsyncImagePainter(uri), contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop); IconButton(onClick = { selectedImages.remove(uri) }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) { Image(painterResource(R.drawable.ic_close), contentDescription = "关闭", modifier = Modifier.size(16.dp)) } } } } }
        }
    }
}
