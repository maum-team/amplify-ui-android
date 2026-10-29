/*
 * Copyright 2026 Lifeoasis. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 */

package com.amplifyframework.ui.liveness.ui

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.amplifyframework.predictions.aws.models.ColorDisplayInformation
import com.amplifyframework.predictions.aws.models.RgbColor
import com.amplifyframework.ui.testing.ComposeTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import io.mockk.every
import io.mockk.mockk
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalFreshnessChallengeTest : ComposeTest() {
    @Test
    fun `native renderer reports colors without an external controller`() {
        val expectedColor = RgbColor(12, 34, 56)
        val colors = listOf(
            mockk<ColorDisplayInformation> {
                every { color } returns expectedColor
                every { duration } returns 1000.0f
                every { shouldScroll } returns false
            }
        )
        val displayed = mutableListOf<RgbColor>()
        composeTestRule.mainClock.autoAdvance = false
        setContent {
            ExternalFreshnessChallenge(
                key = "native-test",
                modifier = Modifier.size(200.dp),
                colors = colors,
                controller = null,
                onColorDisplayed = { current, _, _, _ -> displayed += current },
                onComplete = { }
            )
        }
        composeTestRule.mainClock.advanceTimeBy(32)
        composeTestRule.waitForIdle()
        drawFrame()
        assertEquals(listOf(expectedColor), displayed)
    }

    @Test
    fun `hidden upstream renderer reports colors and completes after host readiness`() {
        val controller = FaceLivenessExternalUiController().apply {
            rendersFreshnessExternally = true
        }
        val expectedColor = RgbColor(12, 34, 56)
        val colors = listOf(
            mockk<ColorDisplayInformation> {
                every { color } returns expectedColor
                every { duration } returns 1000.0f
                every { shouldScroll } returns false
            }
        )
        val displayed = mutableListOf<RgbColor>()
        var completed = false
        var started = false
        controller.onStarted = { started = true }
        var prepared: FaceLivenessExternalFreshnessSequenceEvent? = null
        controller.onFreshnessSequencePrepared = { prepared = it }
        composeTestRule.mainClock.autoAdvance = false

        setContent {
            ExternalFreshnessChallenge(
                key = "test",
                modifier = Modifier.size(200.dp),
                colors = colors,
                controller = controller,
                onColorDisplayed = { current, _, _, _ -> displayed += current },
                onComplete = { completed = true }
            )
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf(12, 34, 56), prepared?.frames?.single()?.currentColor)
        assertTrue(displayed.isEmpty())
        assertFalse(completed)

        composeTestRule.runOnIdle { controller.acknowledgeFreshnessReady() }
        composeTestRule.mainClock.advanceTimeBy(32)
        composeTestRule.waitForIdle()
        repeat(5) {
            composeTestRule.mainClock.advanceTimeBy(32)
            composeTestRule.waitForIdle()
        }
        assertTrue("native playback started after readiness", started)
        drawFrame()
        assertEquals(listOf(expectedColor), displayed)
        assertFalse(completed)

        // The upstream player uses System.currentTimeMillis(), not Compose's virtual clock.
        Thread.sleep(1100)
        repeat(5) {
            composeTestRule.mainClock.advanceTimeBy(32)
            composeTestRule.waitForIdle()
        }
        drawFrame()
        assertTrue(completed)
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun drawFrame() {
        // Robolectric needs an explicit draw to evaluate the upstream Canvas frame callbacks.
        val output = File.createTempFile("freshness-frame", ".png")
        try {
            composeTestRule.onRoot().captureRoboImage(
                file = output,
                roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
            )
        } finally {
            output.delete()
        }
    }
}
