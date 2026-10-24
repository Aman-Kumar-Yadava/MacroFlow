package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MacroFlowApplication
import com.example.data.model.MacroActionType
import com.example.data.model.MacroEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MacroPreviewScreen(
    macroId: String,
    onBack: () -> Unit
) {
    val app = MacroFlowApplication.instance
    var macro by remember { mutableStateOf<MacroEntity?>(null) }

    var isPreviewRunning by remember { mutableStateOf(false) }
    var currentActionIndex by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(macroId) {
        macro = app.macroRepository.getMacroById(macroId)
    }

    fun startPreview() {
        val targetMacro = macro ?: return
        if (targetMacro.actions.isEmpty()) return

        scope.launch {
            isPreviewRunning = true
            currentActionIndex = 0

            for ((index, action) in targetMacro.actions.withIndex()) {
                if (!isPreviewRunning) break
                currentActionIndex = index

                val scaledDelay = (action.delayBeforeMs / targetMacro.playbackSpeed).toLong()
                delay(scaledDelay.coerceAtLeast(10L))

                val scaledDuration = (action.durationMs / targetMacro.playbackSpeed).toLong()
                delay(scaledDuration.coerceAtLeast(20L))
            }

            isPreviewRunning = false
            currentActionIndex = -1
        }
    }

    fun stopPreview() {
        isPreviewRunning = false
        currentActionIndex = -1
    }

    if (macro == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Macro...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val currentMacro = macro!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(color = Color(0xFF181B22), tonalElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Visual Macro Preview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentMacro.name,
                        fontSize = 12.sp,
                        color = Color(0xFF22C55E)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Safe Preview Disclaimer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SAFE PREVIEW CANVAS — Gestures are rendered visually inside this simulated phone screen and will NOT execute on your background device.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated Phone Display Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0D0E12))
                    .border(2.dp, Color(0xFF2E374A), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw stored macro actions on the simulated display
                    currentMacro.actions.forEachIndexed { index, action ->
                        val startX = action.startXNormalized * width
                        val startY = action.startYNormalized * height
                        val endX = action.endXNormalized * width
                        val endY = action.endYNormalized * height

                        val isActive = index == currentActionIndex
                        val color = if (isActive) Color(0xFF22C55E) else Color(0xFF6B7280)

                        if (action.actionType == MacroActionType.SWIPE || action.actionType == MacroActionType.DRAG) {
                            drawLine(
                                color = color,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isActive) 8f else 4f
                            )
                        }

                        // Touch target circle
                        drawCircle(
                            color = color,
                            radius = if (isActive) 24f else 14f,
                            center = Offset(startX, startY)
                        )
                        if (isActive) {
                            drawCircle(
                                color = Color.White,
                                radius = 32f,
                                center = Offset(startX, startY),
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                }
            }
        }

        // Control Panel
        Surface(color = Color(0xFF181B22), tonalElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isPreviewRunning) stopPreview() else startPreview()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPreviewRunning) Color(0xFFEF4444) else Color(0xFF22C55E),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPreviewRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPreviewRunning) "Stop Preview" else "Play Preview",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
