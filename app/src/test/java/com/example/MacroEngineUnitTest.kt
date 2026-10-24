package com.example

import com.example.data.local.MacroConverters
import com.example.data.model.MacroAction
import com.example.data.model.MacroActionType
import com.example.data.model.MacroCategory
import com.example.data.model.MacroEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MacroEngineUnitTest {

    private val converters = MacroConverters()

    @Test
    fun testMacroActionSerialization() {
        val actions = listOf(
            MacroAction(
                id = UUID.randomUUID().toString(),
                actionType = MacroActionType.TAP,
                startXNormalized = 0.5f,
                startYNormalized = 0.5f,
                durationMs = 50L,
                delayBeforeMs = 100L
            ),
            MacroAction(
                id = UUID.randomUUID().toString(),
                actionType = MacroActionType.SWIPE,
                startXNormalized = 0.2f,
                startYNormalized = 0.2f,
                endXNormalized = 0.8f,
                endYNormalized = 0.8f,
                durationMs = 300L,
                delayBeforeMs = 200L
            )
        )

        val json = converters.fromActionList(actions)
        assertTrue(json.contains("TAP"))
        assertTrue(json.contains("SWIPE"))

        val deserialized = converters.toActionList(json)
        assertEquals(2, deserialized.size)
        assertEquals(MacroActionType.TAP, deserialized[0].actionType)
        assertEquals(MacroActionType.SWIPE, deserialized[1].actionType)
    }

    @Test
    fun testCoordinateNormalization() {
        val rawX = 1.2f
        val rawY = -0.5f
        val normalizedX = rawX.coerceIn(0.0f, 1.0f)
        val normalizedY = rawY.coerceIn(0.0f, 1.0f)

        assertEquals(1.0f, normalizedX, 0.001f)
        assertEquals(0.0f, normalizedY, 0.001f)
    }

    @Test
    fun testPlaybackSpeedScalingCalculations() {
        val originalDelay = 200L
        val originalDuration = 100L
        val speedMultiplier = 2.0f

        val scaledDelay = (originalDelay / speedMultiplier).toLong()
        val scaledDuration = (originalDuration / speedMultiplier).toLong()

        assertEquals(100L, scaledDelay)
        assertEquals(50L, scaledDuration)
    }

    @Test
    fun testMacroTotalDurationCalculation() {
        val actions = listOf(
            MacroAction(
                id = "1",
                actionType = MacroActionType.TAP,
                startXNormalized = 0.5f,
                startYNormalized = 0.5f,
                durationMs = 50L,
                delayBeforeMs = 150L
            ),
            MacroAction(
                id = "2",
                actionType = MacroActionType.TAP,
                startXNormalized = 0.5f,
                startYNormalized = 0.5f,
                durationMs = 50L,
                delayBeforeMs = 250L
            )
        )

        val totalDuration = actions.sumOf { it.delayBeforeMs + it.durationMs }
        assertEquals(500L, totalDuration)
    }
}
