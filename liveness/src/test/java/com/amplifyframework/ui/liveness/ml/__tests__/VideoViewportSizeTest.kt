/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://aws.amazon.com/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amplifyframework.ui.liveness.ml

import android.graphics.RectF
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.amplifyframework.ui.liveness.ui.helper.VideoViewportSize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VideoViewportSizeTest {
    @Test
    fun `default preview retains the upstream aspect fit geometry`() {
        val viewport = VideoViewportSize.create(
            containerSize = IntSize(393, 852),
            density = Density(1f)
        )

        assertEquals(IntSize(393, 524), viewport.viewportPixelSize)
        val containerRect = viewport.getBoundingRectInContainer(RectF(0f, 0f, 480f, 640f))
        assertEquals(0f, containerRect.left, 0.001f)
        assertEquals(164f, containerRect.top, 0.001f)
        assertEquals(393f, containerRect.right, 0.001f)
        assertEquals(688f, containerRect.bottom, 0.001f)
    }

    @Test
    fun `aspect fill maps the native face oval into the centered preview`() {
        val viewport = VideoViewportSize.create(
            containerSize = IntSize(393, 852),
            density = Density(1f),
            usesAspectFill = true
        )

        assertEquals(IntSize(639, 852), viewport.viewportPixelSize)

        val sourceRect = RectF(120f, 126f, 360f, 514f)
        val containerRect = viewport.getBoundingRectInContainer(sourceRect)
        assertEquals(36.75f, containerRect.left, 0.001f)
        assertEquals(167.7375f, containerRect.top, 0.001f)
        assertEquals(356.25f, containerRect.right, 0.001f)
        assertEquals(684.2625f, containerRect.bottom, 0.001f)
    }
}
