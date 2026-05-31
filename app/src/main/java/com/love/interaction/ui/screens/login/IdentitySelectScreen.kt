package com.love.interaction.ui.screens.login

import com.love.interaction.R

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.love.interaction.ui.theme.*
import com.love.interaction.viewmodel.Lover

@Composable
fun IdentitySelectScreen(
    onIdentitySelected: (Lover) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heart")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "heartScale"
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "\u2764\uFE0F", fontSize = 64.sp, modifier = Modifier.scale(heartScale))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u79D8\u5BC6\u57FA\u5730",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "\u4F60\u662F\u8C01\uFF1F",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Lover.entries.forEach { lover ->
                    IdentityCard(lover = lover, modifier = Modifier.weight(1f), onClick = { onIdentitySelected(lover) })
                }
            }
        }
    }
}

@Composable
private fun IdentityCard(lover: Lover, modifier: Modifier, onClick: () -> Unit) {
    val colors = when (lover) {
        Lover.LONGTENG -> listOf(Coral40, Coral80)
        Lover.YANHUIXIN -> listOf(Pink40, Pink80)
    }
    Card(
        modifier = modifier.height(200.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors.map { it.copy(alpha = 0.15f) })).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val avatarRes = when (lover) {
    Lover.LONGTENG -> R.drawable.avatar_male
    Lover.YANHUIXIN -> R.drawable.avatar_female
}
Image(painterResource(avatarRes), contentDescription = lover.displayName, modifier = Modifier.size(80.dp).clip(CircleShape))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = lover.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors[0])
        }
    }
}
