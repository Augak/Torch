package com.kastinetech.torch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashPatternsTest {
    @Test
    fun sosSequenceEncodesShortLongShortPulses() {
        val onDurations = FlashPatterns.sosSteps.filter { it.isOn }.map { it.durationMillis }
        assertEquals(
            listOf(200L, 200L, 200L, 600L, 600L, 600L, 200L, 200L, 200L),
            onDurations
        )
    }

    @Test
    fun sosSequenceUsesCorrectMorseGaps() {
        val offDurations = FlashPatterns.sosSteps.filterNot { it.isOn }.map { it.durationMillis }
        assertEquals(
            listOf(200L, 200L, 600L, 200L, 200L, 600L, 200L, 200L, 1400L),
            offDurations
        )
    }

    @Test
    fun allStepsHavePositiveDurationsAndStrobeIntervalIsBounded() {
        assertTrue(FlashPatterns.sosSteps.isNotEmpty())
        assertTrue(FlashPatterns.sosSteps.all { it.durationMillis > 0L })
        assertTrue(FlashPatterns.strobeIntervalMillis in 100L..1000L)
    }
}
