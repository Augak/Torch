package com.kastinetech.torch

/**
 * SOS Morse timing: short pulse 200 ms, long pulse 600 ms,
 * intra-letter gap 200 ms, letter gap 600 ms, word gap 1400 ms.
 */
internal data class FlashStep(val isOn: Boolean, val durationMillis: Long)

internal object FlashPatterns {
    val sosSteps = listOf(
        // S
        FlashStep(true, 200L), FlashStep(false, 200L),
        FlashStep(true, 200L), FlashStep(false, 200L),
        FlashStep(true, 200L), FlashStep(false, 600L),
        // O
        FlashStep(true, 600L), FlashStep(false, 200L),
        FlashStep(true, 600L), FlashStep(false, 200L),
        FlashStep(true, 600L), FlashStep(false, 600L),
        // S, followed by a word-length pause
        FlashStep(true, 200L), FlashStep(false, 200L),
        FlashStep(true, 200L), FlashStep(false, 200L),
        FlashStep(true, 200L), FlashStep(false, 1400L)
    )
    const val strobeIntervalMillis = 250L
}
