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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class FaceLivenessExternalFreshnessHandshakeTest {
    @Test
    fun `native playback waits until RN Skia acknowledges the prepared sequence`() {
        val controller = FaceLivenessExternalUiController()
        val frames = listOf(
            FaceLivenessExternalFreshnessFrame(
                currentColor = listOf(12, 34, 56),
                previousColor = listOf(12, 34, 56),
                durationMilliseconds = 480.0,
                shouldScroll = true
            )
        )
        var prepared: FaceLivenessExternalFreshnessSequenceEvent? = null
        var playbackStarted = false
        controller.onFreshnessSequencePrepared = { prepared = it }

        controller.prepareFreshness(frames) { playbackStarted = true }

        assertEquals(frames, prepared?.frames)
        assertFalse(playbackStarted)

        controller.acknowledgeFreshnessReady()

        assertTrue(playbackStarted)
    }

    @Test
    fun `freshness ready acknowledgement is consumed once`() {
        val controller = FaceLivenessExternalUiController()
        var starts = 0
        controller.prepareFreshness(emptyList()) { starts += 1 }

        controller.acknowledgeFreshnessReady()
        controller.acknowledgeFreshnessReady()

        assertEquals(1, starts)
    }

    @Test
    fun `missing host subscriber starts native playback immediately`() {
        val controller = FaceLivenessExternalUiController()
        var starts = 0

        controller.prepareFreshness(emptyList()) { starts += 1 }

        assertEquals(1, starts)
    }

    @Test
    fun `disposing a pending sequence prevents late acknowledgement from starting it`() {
        val controller = FaceLivenessExternalUiController()
        var starts = 0
        controller.onFreshnessSequencePrepared = { }
        controller.prepareFreshness(emptyList()) { starts += 1 }

        controller.cancelPendingFreshness()
        controller.acknowledgeFreshnessReady()

        assertEquals(0, starts)
    }
}
