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

class VideoViewportSizeTest {
    @Test
    fun `aspect fill expands and centers a three by four preview`() {
        val viewport = VideoViewportSize.create(
            containerSize = IntSize(393, 852),
            density = Density(1f),
            usesAspectFill = true
        )

        assertEquals(IntSize(639, 852), viewport.viewportPixelSize)

        val sourceRect = RectF(120f, 126f, 360f, 514f)
        val containerRect = viewport.getBoundingRectInContainer(sourceRect)
        val roundTripRect = viewport.getUnscaledBoundingRect(containerRect)

        assertEquals(sourceRect.left, roundTripRect.left, 0.001f)
        assertEquals(sourceRect.top, roundTripRect.top, 0.001f)
        assertEquals(sourceRect.right, roundTripRect.right, 0.001f)
        assertEquals(sourceRect.bottom, roundTripRect.bottom, 0.001f)
    }
}
