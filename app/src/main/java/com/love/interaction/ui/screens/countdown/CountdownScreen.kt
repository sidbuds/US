package com.love.interaction.ui.screens.countdown

import com.love.interaction.R

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.model.Countdown
import com.love.interaction.data.repository.CountdownRepository
import com.love.interaction.data.repository.SessionManager
import com.love.interaction.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val TAG = "CountdownScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(onBack: () -> Unit, heroTitle: MutableState<String>, heroDays: MutableState<Int>, heroEmoji: MutableState<String>) {
    val today = LocalDate.now(); val scope = rememberCoroutineScope(); val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }; val repo = remember { CountdownRepository(SessionManager(db.sessionDao())) }
    var items by remember { mutableStateOf(listOf<Countdown>()) }; var isLoading by remember { mutableStateOf(true) }
    var editingItem by remember { mutableStateOf<Countdown?>(null) }; var showAddDialog by remember { mutableStateOf(false) }; var errorMsg by remember { mutableStateOf<String?>(null) }
    fun loadItems() { scope.launch { isLoading = true; repo.getCountdowns().onSuccess { items = it.sortedBy { it.date } }.onFailure { Log.e(TAG, "Load failed", it); errorMsg = "\u52A0\u8F7D\u5931\u8D25: ${it.message}" }; isLoading = false } }
    LaunchedEffect(Unit) { loadItems() }
    val homeItem = items.firstOrNull { it.showOnHome }
    if (homeItem != null) { val homeDate = try { LocalDate.parse(homeItem.date) } catch (_: Exception) { null }; if (homeDate != null) { var nextOcc = homeDate.withYear(today.year); if (nextOcc.isBefore(today) || nextOcc.isEqual(today)) nextOcc = nextOcc.plusYears(1); heroTitle.value = homeItem.title; heroDays.value = ChronoUnit.DAYS.between(today, nextOcc).toInt(); heroEmoji.value = homeItem.emoji.ifEmpty { "\uD83C\uDF89" } } }
    val anniversaryItem = items.firstOrNull { it.title.contains("\u7EAA\u5FF5\u65E5") }
    if (anniversaryItem != null) { val d = try { LocalDate.parse(anniversaryItem.date) } catch (_: Exception) { null }; if (d != null) context.getSharedPreferences("countdown_prefs", android.content.Context.MODE_PRIVATE).edit().putString("anniversary_date", d.toString()).apply() }

    if (editingItem != null) {
        CountdownEditScreen(item = editingItem!!, onBack = { editingItem = null }, onSave = { updated -> scope.launch { if (updated.showOnHome) items.filter { it.showOnHome && it.id != updated.id }.forEach { o -> repo.updateCountdown(o.id, o.title, o.date, o.emoji, false) }; repo.updateCountdown(updated.id, updated.title, updated.date, updated.emoji, updated.showOnHome).onSuccess { editingItem = null; loadItems() }.onFailure { errorMsg = "\u4FDD\u5B58\u5931\u8D25: ${it.message}" } } }, onDelete = { scope.launch { repo.deleteCountdown(editingItem!!.id).onSuccess { editingItem = null; loadItems() }.onFailure { errorMsg = "\u5220\u9664\u5931\u8D25: ${it.message}" } } })
        return
    }

    Scaffold(topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.ic_countdown), contentDescription = null, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text("\u5012\u6570\u65E5") } }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }, actions = { IconButton(onClick = { showAddDialog = true }) { Image(painterResource(R.drawable.ic_add), contentDescription = "添加", modifier = Modifier.size(24.dp)) } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (errorMsg != null) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(errorMsg!!, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error); IconButton(onClick = { errorMsg = null }) { Image(painterResource(R.drawable.ic_close), contentDescription = "关闭", modifier = Modifier.size(24.dp)) } } } }
            if (isLoading) Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else if (items.isEmpty()) Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("\u6682\u65E0\u5012\u6570\u65E5", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else items.forEach { item -> CountdownCard(item = item, today = today, onClick = { editingItem = item }) }
        }
    }
    if (showAddDialog) { var t by remember { mutableStateOf("") }; var d by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }; var e by remember { mutableStateOf("\uD83C\uDF89") }
        AlertDialog(onDismissRequest = { showAddDialog = false }, title = { Text("\u6DFB\u52A0\u5012\u6570\u65E5") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = t, onValueChange = { t = it }, label = { Text("\u540D\u79F0") }, singleLine = true, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("\u65E5\u671F") }, singleLine = true, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = e, onValueChange = { e = it }, label = { Text("\u8868\u60C5") }, singleLine = true, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { if (t.isNotBlank() && d.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) scope.launch { repo.createCountdown(e, t, d, false).onSuccess { showAddDialog = false; loadItems() }.onFailure { errorMsg = "\u521B\u5EFA\u5931\u8D25: ${it.message}" } } }) { Text("\u6DFB\u52A0") } }, dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("\u53D6\u6D88") } }) }
}

@Composable
private fun CountdownCard(item: Countdown, today: LocalDate, onClick: () -> Unit) {
    val itemDate = try { LocalDate.parse(item.date) } catch (_: Exception) { return }; var nextOcc = itemDate.withYear(today.year); if (nextOcc.isBefore(today) || nextOcc.isEqual(today)) nextOcc = nextOcc.plusYears(1)
    val daysLeft = ChronoUnit.DAYS.between(today, nextOcc).toInt()
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (item.showOnHome) Pink40.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(item.emoji.ifEmpty { "\uD83C\uDF89" }, fontSize = 36.sp); Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(itemDate.format(DateTimeFormatter.ofPattern("yyyy\u5E74M\u6708d\u65E5")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); if (item.showOnHome) Text("\u2B50 \u4E3B\u9875\u663E\u793A", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("$daysLeft", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text("\u5929", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.width(4.dp)); Image(painterResource(R.drawable.ic_expand), contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountdownEditScreen(item: Countdown, onBack: () -> Unit, onSave: (Countdown) -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(item.title) }; var dateStr by remember { mutableStateOf(item.date) }; var emoji by remember { mutableStateOf(item.emoji) }; var showOnHome by remember { mutableStateOf(item.showOnHome) }; var showDel by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    Scaffold(topBar = { TopAppBar(title = { Text("\u7F16\u8F91\u5012\u6570\u65E5") }, navigationIcon = { IconButton(onClick = onBack) { Image(painterResource(R.drawable.ic_back), contentDescription = "返回", modifier = Modifier.size(24.dp)) } }, actions = { TextButton(onClick = { try { onSave(item.copy(title = title, date = LocalDate.parse(dateStr).toString(), emoji = emoji, showOnHome = showOnHome)) } catch (_: Exception) {} }, enabled = dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { Text("\u4FDD\u5B58") } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val pd = remember(dateStr) { try { LocalDate.parse(dateStr) } catch (_: Exception) { null } }
            if (pd != null) { var n = pd.withYear(today.year); if (n.isBefore(today) || n.isEqual(today)) n = n.plusYears(1); val dl = ChronoUnit.DAYS.between(today, n).toInt(); Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Pink40.copy(alpha = 0.15f))) { Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(emoji.ifEmpty { "\uD83C\uDF89" }, fontSize = 48.sp); Text(title.ifEmpty { "\u672A\u547D\u540D" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("$dl", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text("\u5929\u540E\u5230\u6765", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("\u540D\u79F0") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("\u65E5\u671F") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("\u8868\u60C5") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("\u2B50 \u663E\u793A\u5728\u4E3B\u9875", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium); Text("\u5F00\u542F\u540E\u4E3B\u9875\u4F1A\u663E\u793A\u6B64\u5012\u6570\u65E5", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked = showOnHome, onCheckedChange = { showOnHome = it }) } }
            Spacer(Modifier.weight(1f)); OutlinedButton(onClick = { showDel = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Image(painterResource(R.drawable.ic_delete), contentDescription = "删除", modifier = Modifier.size(24.dp)); Spacer(Modifier.width(8.dp)); Text("\u5220\u9664\u6B64\u5012\u6570\u65E5") }
        }
    }
    if (showDel) AlertDialog(onDismissRequest = { showDel = false }, title = { Text("\u786E\u8BA4\u5220\u9664\uFF1F") }, text = { Text("\u5220\u9664\u540E\u65E0\u6CD5\u6062\u590D") }, confirmButton = { TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("\u5220\u9664") } }, dismissButton = { TextButton(onClick = { showDel = false }) { Text("\u53D6\u6D88") } })
}
