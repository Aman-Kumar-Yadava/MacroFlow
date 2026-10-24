package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MacroFlowApplication
import com.example.service.FloatingOverlayService
import com.example.util.PermissionManager
import kotlinx.coroutines.launch

@Composable
fun FloatingControlsScreen() {
    val context = LocalContext.current
    val app = MacroFlowApplication.instance
    val scope = rememberCoroutineScope()
    val prefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(initial = com.example.data.local.UserPreferences())

    val isOverlayGranted = PermissionManager.isOverlayPermissionGranted(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Floating Assistant Controls",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Overlay Permission Warning Banner
        if (!isOverlayGranted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261D1A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Overlay Permission Required", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To display the floating gaming assistant handle over games and other apps, please grant 'Display over other apps' permission.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            context.startActivity(PermissionManager.openOverlayPermissionIntent(context))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Toggle Floating Service Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable Floating Toolbar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Shows floating handle over games for 1-tap macro access.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = prefs.floatingToolbarEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            app.userPreferencesRepository.updateFloatingToolbarEnabled(enabled)
                            val serviceIntent = Intent(context, FloatingOverlayService::class.java)
                            if (enabled && isOverlayGranted) {
                                context.startService(serviceIntent)
                            } else {
                                context.stopService(serviceIntent)
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22C55E))
                )
            }
        }

        // Live Handle Preview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B22)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIVE FLOATING HANDLE PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val buttonSizeDp = when (prefs.floatingSize) {
                    "Small" -> 42.dp
                    "Large" -> 64.dp
                    else -> 52.dp
                }

                Box(
                    modifier = Modifier
                        .size(buttonSizeDp)
                        .alpha(prefs.floatingOpacity.coerceIn(0.2f, 1.0f))
                        .clip(if (prefs.floatingShape == "Circular") CircleShape else RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E222D))
                        .border(
                            2.dp,
                            Color(0xFF22C55E),
                            if (prefs.floatingShape == "Circular") CircleShape else RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Floating Handle Preview",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Tap handle in games to expand assistant panel", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Appearance Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BUTTON OPACITY (${(prefs.floatingOpacity * 100).toInt()}%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Slider(
                    value = prefs.floatingOpacity,
                    onValueChange = { newOpacity ->
                        scope.launch {
                            app.userPreferencesRepository.updateFloatingAppearance(
                                opacity = newOpacity,
                                size = prefs.floatingSize,
                                shape = prefs.floatingShape,
                                accentColor = prefs.floatingAccentColor
                            )
                        }
                    },
                    valueRange = 0.20f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF22C55E),
                        activeTrackColor = Color(0xFF22C55E)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("BUTTON SHAPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Circular", "Rounded Square").forEach { shape ->
                        FilterChip(
                            selected = prefs.floatingShape == shape,
                            onClick = {
                                scope.launch {
                                    app.userPreferencesRepository.updateFloatingAppearance(
                                        opacity = prefs.floatingOpacity,
                                        size = prefs.floatingSize,
                                        shape = shape,
                                        accentColor = prefs.floatingAccentColor
                                    )
                                }
                            },
                            label = { Text(shape) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF22C55E))
                        )
                    }
                }
            }
        }
    }
}
