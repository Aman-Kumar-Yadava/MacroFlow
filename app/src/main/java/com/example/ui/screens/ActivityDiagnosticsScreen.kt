package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MacroFlowApplication
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ExecutionLogType
import com.example.util.DeviceCapabilityChecker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityDiagnosticsScreen() {
    val context = LocalContext.current
    val app = MacroFlowApplication.instance
    val scope = rememberCoroutineScope()

    val logs by app.diagnosticsRepository.allLogs.collectAsState(initial = emptyList())
    val successCount by app.diagnosticsRepository.successCount.collectAsState(initial = 0)
    val failedCount by app.diagnosticsRepository.failedCount.collectAsState(initial = 0)

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Activity Logs, 1: Developer Diagnostics

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Activity & Diagnostics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF181B22),
            contentColor = Color(0xFF22C55E)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Activity History", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Device Diagnostics", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Activity Logs Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Success: $successCount | Failed: $failedCount",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (logs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { scope.launch { app.diagnosticsRepository.clearLogs() } },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Logs", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No execution history recorded.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(logs) { log ->
                        ActivityLogCard(log = log)
                    }
                }
            }
        } else {
            // Device Diagnostics Tab
            val caps = remember { DeviceCapabilityChecker.getCapabilities(context) }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    DiagnosticItemCard("Manufacturer & Model", "${caps.manufacturer} ${caps.model}")
                    DiagnosticItemCard("Android OS Version", "Android ${caps.androidVersion} (API ${caps.apiLevel})")
                    DiagnosticItemCard("Display Resolution", "${caps.displayWidthPx} x ${caps.displayHeightPx} px (${caps.densityDpi} DPI)")
                    DiagnosticItemCard("Accessibility Service Status", if (caps.isAccessibilityEnabled) "Active & Connected" else "Disabled")
                    DiagnosticItemCard("Overlay Window Permission", if (caps.isOverlayPermissionGranted) "Granted" else "Missing")
                    DiagnosticItemCard("Shizuku Privilege State", caps.shizukuState.name)
                    DiagnosticItemCard("Gesture Playback Engine", if (caps.canDispatchAccessibilityGestures) "Supported via AccessibilityService" else "Unavailable")
                }
            }
        }
    }
}

@Composable
private fun ActivityLogCard(log: ExecutionLogEntity) {
    val isSuccess = log.logType == ExecutionLogType.PLAYBACK_SUCCESS
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF22C55E) else Color(0xFFEF4444),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${dateFormat.format(Date(log.timestamp))} • ${log.backendUsed}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (!log.errorDetails.isNullOrEmpty()) {
                    Text(
                        text = "Details: ${log.errorDetails}",
                        fontSize = 10.sp,
                        color = Color(0xFFFCA5A5)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItemCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
        }
    }
}
