package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.MacroAction
import com.example.data.model.MacroActionType
import com.example.data.model.MacroEntity
import kotlinx.coroutines.launch

@Composable
fun MacroEditorScreen(
    macroId: String,
    onBack: () -> Unit,
    onRecordAgain: (String) -> Unit,
    onPreview: (String) -> Unit
) {
    val context = LocalContext.current
    val app = MacroFlowApplication.instance
    val scope = rememberCoroutineScope()

    var macro by remember { mutableStateOf<MacroEntity?>(null) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Editable States
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var repetitions by remember { mutableIntStateOf(1) }
    var isContinuous by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var opacity by remember { mutableFloatStateOf(0.85f) }
    var buttonSize by remember { mutableStateOf("Medium") }
    var buttonShape by remember { mutableStateOf("Circular") }
    var showButtonLabel by remember { mutableStateOf(true) }
    var actionsList by remember { mutableStateOf<List<MacroAction>>(emptyList()) }

    LaunchedEffect(macroId) {
        val loaded = app.macroRepository.getMacroById(macroId)
        if (loaded != null) {
            macro = loaded
            name = loaded.name
            description = loaded.description
            repetitions = loaded.repetitions
            isContinuous = loaded.isContinuous
            speed = loaded.playbackSpeed
            opacity = loaded.buttonOpacity
            buttonSize = loaded.buttonSize
            buttonShape = loaded.buttonShape
            showButtonLabel = loaded.showButtonLabel
            actionsList = loaded.actions
        }
    }

    // Handle back button press when unsaved changes exist
    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    fun handleBack() {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    fun saveChanges() {
        macro?.let { current ->
            val updated = current.copy(
                name = name.ifBlank { "Untitled Macro" },
                description = description,
                repetitions = repetitions,
                isContinuous = isContinuous,
                playbackSpeed = speed,
                buttonOpacity = opacity,
                buttonSize = buttonSize,
                buttonShape = buttonShape,
                showButtonLabel = showButtonLabel,
                actions = actionsList,
                actionCount = actionsList.size,
                durationMs = actionsList.sumOf { it.delayBeforeMs + it.durationMs }
            )

            scope.launch {
                app.macroRepository.updateMacro(updated)
                hasUnsavedChanges = false
                Toast.makeText(context, "Macro changes saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (macro == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Macro...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Navigation & Action Header
        Surface(
            color = Color(0xFF181B22),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { handleBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Edit Macro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Preview Macro") },
                            onClick = {
                                showOverflowMenu = false
                                onPreview(macroId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = {
                                showOverflowMenu = false
                                scope.launch {
                                    app.macroRepository.duplicateMacro(macroId)
                                    Toast.makeText(context, "Macro duplicated", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action Buttons: Record Again & Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onRecordAgain(name) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Again", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onPreview(macroId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Command Name & Description Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COMMAND DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("Macro Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }
                }
            }

            // Repetitions Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "REPETITIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Continuous Repeat", fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = isContinuous,
                                onCheckedChange = {
                                    isContinuous = it
                                    hasUnsavedChanges = true
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22C55E))
                            )
                        }

                        if (!isContinuous) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Repetition Count: $repetitions")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (repetitions > 1) {
                                                repetitions--
                                                hasUnsavedChanges = true
                                            }
                                        }
                                    ) { Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold) }

                                    Text(
                                        text = repetitions.toString(),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (repetitions < 999) {
                                                repetitions++
                                                hasUnsavedChanges = true
                                            }
                                        }
                                    ) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }

            // Playback Speed Slider Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PLAYBACK SPEED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${String.format("%.2f", speed)}x",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = speed,
                            onValueChange = {
                                speed = it
                                hasUnsavedChanges = true
                            },
                            valueRange = 0.25f..3.0f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF22C55E),
                                activeTrackColor = Color(0xFF22C55E)
                            )
                        )

                        if (speed > 2.0f) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "High playback speeds (>2.0x) depend on device touchscreen response limits.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }

            // Button Opacity & Appearance Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FLOATING BUTTON OPACITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${(opacity * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E)
                            )
                        }

                        Slider(
                            value = opacity,
                            onValueChange = {
                                opacity = it
                                hasUnsavedChanges = true
                            },
                            valueRange = 0.20f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF22C55E),
                                activeTrackColor = Color(0xFF22C55E)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("BUTTON SIZE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (size in listOf("Small", "Medium", "Large")) {
                                FilterChip(
                                    selected = buttonSize == size,
                                    onClick = {
                                        buttonSize = size
                                        hasUnsavedChanges = true
                                    },
                                    label = { Text(size) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF22C55E))
                                )
                            }
                        }
                    }
                }
            }

            // Action Timeline Section
            item {
                Text(
                    text = "ACTION TIMELINE (${actionsList.size} Touch Steps)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            itemsIndexed(actionsList) { index, action ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222733)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = action.actionType.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Delay: ${action.delayBeforeMs}ms • Duration: ${action.durationMs}ms",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                actionsList = actionsList.toMutableList().apply { removeAt(index) }
                                hasUnsavedChanges = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Action",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Bar: Delete & Save
        Surface(
            color = Color(0xFF181B22),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }

                Button(
                    onClick = { saveChanges() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Unsaved Changes Confirmation Dialog
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes", fontWeight = FontWeight.Bold) },
            text = { Text("You have unsaved changes to this macro. Do you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        onBack()
                    }
                ) {
                    Text("Discard", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    // Delete Macro Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Macro?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '$name'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            app.macroRepository.deleteMacroById(macroId)
                            onBack()
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
