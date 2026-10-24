package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MacroFlowApplication
import com.example.data.model.MacroAction
import com.example.data.model.MacroActionType
import com.example.data.model.MacroCategory
import com.example.data.model.MacroEntity
import com.example.engine.MacroRecordingEngine
import com.example.engine.RecordingState
import kotlinx.coroutines.launch

@Composable
fun MacroRecordingWizard(
    initialMacroName: String,
    onBack: () -> Unit,
    onMacroSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val app = MacroFlowApplication.instance
    val scope = rememberCoroutineScope()
    val recordingEngine = remember { MacroRecordingEngine.getInstance() }

    var currentStep by remember { mutableIntStateOf(1) } // Steps 1 to 4

    // Step 1 State
    var name by remember { mutableStateOf(initialMacroName.ifBlank { "Custom Touch Routine" }) }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MacroCategory.GAMING) }

    // Step 2 State
    var recordingMode by remember { mutableStateOf("IN_APP") } // "IN_APP", "FLOATING_BAR"

    // Step 3 State
    val recordingState by recordingEngine.recordingState.collectAsState()
    val ripplePoints = remember { mutableStateListOf<Offset>() }

    // Step 4 State
    var reviewActions by remember { mutableStateOf<List<MacroAction>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Step Navigation Bar
        Surface(color = Color(0xFF181B22), tonalElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Macro Creation Wizard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step $currentStep of 4: ${
                            when (currentStep) {
                                1 -> "Name & Category"
                                2 -> "Recording Mode"
                                3 -> "Touch Capture Canvas"
                                else -> "Review & Save"
                            }
                        }",
                        fontSize = 12.sp,
                        color = Color(0xFF22C55E)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            when (currentStep) {
                1 -> WizardStep1(
                    name = name,
                    onNameChange = { name = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    category = category,
                    onCategoryChange = { category = it },
                    onNext = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Please enter a macro name", Toast.LENGTH_SHORT).show()
                        } else {
                            currentStep = 2
                        }
                    }
                )

                2 -> WizardStep2(
                    recordingMode = recordingMode,
                    onModeChange = { recordingMode = it },
                    onStartRecording = {
                        recordingEngine.startRecordingSession(name)
                        currentStep = 3
                    }
                )

                3 -> WizardStep3(
                    recordingState = recordingState,
                    ripplePoints = ripplePoints,
                    onRecordTouch = { xNorm, yNorm ->
                        recordingEngine.captureTouchAction(
                            actionType = MacroActionType.TAP,
                            startXNormalized = xNorm,
                            startYNormalized = yNorm,
                            durationMs = 50L
                        )
                    },
                    onPause = { recordingEngine.pauseRecording() },
                    onResume = { recordingEngine.resumeRecording() },
                    onStop = {
                        reviewActions = recordingEngine.stopRecordingSession()
                        currentStep = 4
                    },
                    onCancel = {
                        recordingEngine.cancelRecording()
                        currentStep = 1
                    }
                )

                4 -> WizardStep4(
                    macroName = name,
                    actions = reviewActions,
                    onDeleteAction = { idx ->
                        recordingEngine.removeActionAt(idx)
                        reviewActions = (recordingState as? RecordingState.Review)?.actions ?: reviewActions
                    },
                    onSave = {
                        val newMacro = MacroEntity(
                            name = name,
                            description = description,
                            category = category,
                            actions = reviewActions,
                            actionCount = reviewActions.size,
                            durationMs = reviewActions.sumOf { it.delayBeforeMs + it.durationMs }
                        )

                        scope.launch {
                            app.macroRepository.saveMacro(newMacro)
                            Toast.makeText(context, "Macro '$name' saved successfully!", Toast.LENGTH_SHORT).show()
                            onMacroSaved(newMacro.id)
                        }
                    },
                    onDiscard = {
                        recordingEngine.cancelRecording()
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun WizardStep1(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: MacroCategory,
    onCategoryChange: (MacroCategory) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "STEP 1: NAME & CATEGORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Macro Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF22C55E))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
        ) {
            Text("Continue to Step 2", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WizardStep2(
    recordingMode: String,
    onModeChange: (String) -> Unit,
    onStartRecording: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CHOOSE RECORDING METHOD",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // In-App Surface Option
        Card(
            colors = CardDefaults.cardColors(containerColor = if (recordingMode == "IN_APP") Color(0xFF1E2820) else Color(0xFF1C202B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (recordingMode == "IN_APP") Color(0xFF22C55E) else Color(0xFF2E374A),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "A. In-App Touch Recording Canvas (Recommended)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ADE80)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Record touch gestures directly on an application-controlled surface. Standard baseline for guaranteed precision and reliability.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Disclaimer Note
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Android System API Capability Note",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Android standard Accessibility APIs permit dispatching gestures, but do not allow apps to secretly intercept raw touch events outside their own window without elevated ADB/Shizuku privileges.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartRecording,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
        ) {
            Icon(imageVector = Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Recording Canvas", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WizardStep3(
    recordingState: RecordingState,
    ripplePoints: MutableList<Offset>,
    onRecordTouch: (Float, Float) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    val isRecording = recordingState is RecordingState.Recording
    val actionCount = (recordingState as? RecordingState.Recording)?.actionCount ?: 0
    val elapsedTimeMs = (recordingState as? RecordingState.Recording)?.elapsedTimeMs ?: 0L

    Column(modifier = Modifier.fillMaxSize()) {
        // Ticker Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B22)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) Color.Red else Color.Yellow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "RECORDING ACTIVE" else "PAUSED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) Color.Red else Color.Yellow
                    )
                }

                Text(
                    text = "Actions: $actionCount | Time: ${elapsedTimeMs / 1000}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22C55E)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Touch Recording Canvas Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F1117))
                .border(2.dp, Color(0xFF22C55E), RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (isRecording) {
                            ripplePoints.add(offset)
                            val normX = offset.x / size.width
                            val normY = offset.y / size.height
                            onRecordTouch(normX, normY)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                ripplePoints.forEach { pt ->
                    drawCircle(color = Color(0xFF22C55E), radius = 24f, center = pt)
                    drawCircle(color = Color.White, radius = 32f, center = pt)
                }
            }

            Text(
                text = "TAP ANYWHERE ON THIS CANVAS TO RECORD TOUCH STEPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Cancel") }

            Button(
                onClick = onStop,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WizardStep4(
    macroName: String,
    actions: List<MacroAction>,
    onDeleteAction: (Int) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REVIEW RECORDING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Macro: $macroName",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ADE80)
                )
                Text(
                    text = "${actions.size} Actions Recorded",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(actions) { index, act ->
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
                        Text("#${index + 1} ${act.actionType.name} (X:${(act.startXNormalized * 100).toInt()}%, Y:${(act.startYNormalized * 100).toInt()}%)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onDeleteAction(index) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDiscard,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Discard") }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Macro", fontWeight = FontWeight.Bold)
            }
        }
    }
}
