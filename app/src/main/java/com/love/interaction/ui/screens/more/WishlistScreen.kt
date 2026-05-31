package com.love.interaction.ui.screens.more

import com.love.interaction.R

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.love.interaction.data.local.CachedWishlist
import com.love.interaction.viewmodel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(onBack: () -> Unit, viewModel: WishlistViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = viewModel.currentUserId
    var showCreateDialog by remember { mutableStateOf(false) }; var showFulfillDialog by remember { mutableStateOf(false) }
    var selectedWish by remember { mutableStateOf<CachedWishlist?>(null) }; var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    var wishName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) { uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() } }
    LaunchedEffect(uiState.successMessage) { uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() } }

    if (zoomedImageUrl != null) {
        Dialog(onDismissRequest = { zoomedImageUrl = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().clickable { zoomedImageUrl = null }, contentAlignment = Alignment.Center) {
                var scale by remember { mutableFloatStateOf(1f) }; var ox by remember { mutableFloatStateOf(0f) }; var oy by remember { mutableFloatStateOf(0f) }
                Image(painter = rememberAsyncImagePainter(zoomedImageUrl), contentDescription = null, modifier = Modifier.fillMaxWidth().pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(0.5f, 5f); ox += pan.x; oy += pan.y } }.graphicsLayer(scaleX = scale, scaleY = scale, translationX = ox, translationY = oy), contentScale = ContentScale.Fit)
                IconButton(onClick = { zoomedImageUrl = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) { Image(painterResource(R.drawable.ic_close), contentDescription = null, modifier = Modifier.size(32.dp)) }
            }
        }
    }

    if (showFulfillDialog && selectedWish != null) { FulfillWishDialog(wishName = selectedWish!!.name, onDismiss = { showFulfillDialog = false }, onFulfill = { desc, uris -> viewModel.fulfillWish(selectedWish!!.id, desc, uris); showFulfillDialog = false }) }

    Scaffold(topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.ic_wishlist), contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text("\u613F\u671B\u6E05\u5355") } }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }) }, snackbarHost = { SnackbarHost(snackbarHostState) }, floatingActionButton = { FloatingActionButton(onClick = { showCreateDialog = true }) { Image(painterResource(R.drawable.ic_add), contentDescription = "添加", modifier = Modifier.size(24.dp)) } }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (uiState.wishlists.isEmpty() && !uiState.isLoading) { item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("\u2728", style = MaterialTheme.typography.displayLarge); Text("\u8FD8\u6CA1\u6709\u613F\u671B\n\u70B9\u51FB\u53F3\u4E0B\u89D2\u6309\u94AE\u8BB8\u4E0B\u7B2C\u4E00\u4E2A\u613F\u671B", color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
            else { items(uiState.wishlists) { wish -> WishCard(wish = wish, isMine = wish.userId == currentUserId, onFulfill = { selectedWish = wish; showFulfillDialog = true }, onDelete = { viewModel.deleteWish(wish.id) }, onImageClick = { zoomedImageUrl = it }) } }
        }
    }

    if (showCreateDialog) { AlertDialog(onDismissRequest = { showCreateDialog = false }, title = { Text("\u8BB8\u4E2A\u613F\u671B") }, text = { OutlinedTextField(value = wishName, onValueChange = { wishName = it }, label = { Text("\u613F\u671B\u540D\u79F0") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }, confirmButton = { TextButton(onClick = { if (wishName.isNotBlank()) { viewModel.createWish(wishName); wishName = ""; showCreateDialog = false } }) { Text("\u521B\u5EFA") } }, dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("\u53D6\u6D88") } }) }
}

@Composable
private fun WishCard(wish: CachedWishlist, isMine: Boolean, onFulfill: () -> Unit, onDelete: () -> Unit, onImageClick: (String) -> Unit) {
    val isCompleted = wish.status == "completed"; var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("\u2728 ${wish.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Surface(shape = RoundedCornerShape(8.dp), color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer) { Text(if (isMine) "\u6211" else "TA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) } }
            if (isCompleted) {
                Spacer(Modifier.height(8.dp)); Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) { Text("\u2705 \u5DF2\u8FD8\u613F", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall) }
                if (wish.fulfillDescription.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(wish.fulfillDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                val fulfillUrls = try { wish.getFulfillImageUrls() } catch (_: Exception) { emptyList() }
                if (fulfillUrls.isNotEmpty()) { Spacer(Modifier.height(8.dp)); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(fulfillUrls) { url -> Image(painter = rememberAsyncImagePainter(url), contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageClick(url) }, contentScale = ContentScale.Crop) } } }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (!isCompleted) { Button(onClick = onFulfill, shape = RoundedCornerShape(8.dp)) { Image(painterResource(R.drawable.ic_like_filled), contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("\u8FD8\u613F") }; Spacer(Modifier.width(8.dp)) }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) { Image(painterResource(R.drawable.ic_delete), contentDescription = "删除", modifier = Modifier.size(18.dp)) }
            }
        }
    }
    if (showDeleteConfirm) { AlertDialog(onDismissRequest = { showDeleteConfirm = false }, title = { Text("\u786E\u8BA4\u5220\u9664\uFF1F") }, text = { Text("\u5220\u9664\u540E\u65E0\u6CD5\u6062\u590D") }, confirmButton = { TextButton(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("\u5220\u9664") } }, dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("\u53D6\u6D88") } }) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FulfillWishDialog(wishName: String, onDismiss: () -> Unit, onFulfill: (String, List<Uri>) -> Unit) {
    var description by remember { mutableStateOf("") }; val selectedImages = remember { mutableStateListOf<Uri>() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris -> selectedImages.clear(); selectedImages.addAll(uris.take(9)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("\u8FD8\u613F: $wishName") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("\u8FD8\u613F\u8BB0\u5F55") }, placeholder = { Text("\u8BB0\u5F55\u4E0B\u8FD9\u4E2A\u7F8E\u597D\u65F6\u523B...") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5)
            Row(verticalAlignment = Alignment.CenterVertically) { Text("\u56FE\u7247", style = MaterialTheme.typography.titleSmall); Spacer(Modifier.weight(1f)); IconButton(onClick = { photoPicker.launch("image/*") }) { Image(painterResource(R.drawable.ic_add), contentDescription = "添加", modifier = Modifier.size(24.dp)) } }
            if (selectedImages.isNotEmpty()) { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(selectedImages) { uri -> Box { Image(painter = rememberAsyncImagePainter(uri), contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop); IconButton(onClick = { selectedImages.remove(uri) }, modifier = Modifier.align(Alignment.TopEnd).size(20.dp)) { Image(painterResource(R.drawable.ic_close), contentDescription = "关闭", modifier = Modifier.size(16.dp)) } } } } }
        }
    }, confirmButton = { TextButton(onClick = { onFulfill(description, selectedImages.toList()) }) { Text("\u8FD8\u613F") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("\u53D6\u6D88") } })
}
