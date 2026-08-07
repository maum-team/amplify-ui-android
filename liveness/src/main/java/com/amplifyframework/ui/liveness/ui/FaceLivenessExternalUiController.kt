/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amplifyframework.ui.liveness.ui

import android.graphics.RectF
import com.amplifyframework.ui.liveness.ui.helper.VideoViewportSize
import kotlin.math.min

data class FaceLivenessExternalUiStateEvent(
    val phase: String,
    val instruction: String,
    val progress: Double
)

data class FaceLivenessExternalOvalEvent(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
)

data class FaceLivenessExternalMatchingOvalConfiguration(
    val width: Double,
    val height: Double,
    val referenceViewportWidth: Double,
    val referenceViewportHeight: Double
)

/**
 * Optional presentation bridge for hosts that render Face Liveness UI outside Compose.
 * Camera capture, challenge ordering, video streaming, and AWS signaling remain native.
 */
class FaceLivenessExternalUiController {
    var onStarted: ((Long) -> Unit)? = null
    var onCompleted: ((Long) -> Unit)? = null
    var onUiStateChanged: ((FaceLivenessExternalUiStateEvent) -> Unit)? = null
    var onOvalChanged: ((FaceLivenessExternalOvalEvent) -> Unit)? = null

    var rendersUiExternally: Boolean = false
    var usesFullScreenCameraPreview: Boolean = false
    var matchingOvalConfiguration: FaceLivenessExternalMatchingOvalConfiguration? = null

    /** `1` preserves the AWS client-side matching values. */
    var matchingToleranceMultiplier: Double = 1.0

    internal fun start(timestamp: Long = System.currentTimeMillis()) {
        onStarted?.invoke(timestamp)
    }

    internal fun complete(timestamp: Long = System.currentTimeMillis()) {
        onCompleted?.invoke(timestamp)
    }

    internal fun publishUiState(phase: String, instruction: String, progress: Double) {
        onUiStateChanged?.invoke(
            FaceLivenessExternalUiStateEvent(
                phase = phase,
                instruction = instruction,
                progress = progress.coerceIn(0.0, 1.0)
            )
        )
    }

    internal fun publishOval(frame: RectF, viewportWidth: Float, viewportHeight: Float) {
        if (viewportWidth <= 0f || viewportHeight <= 0f) return
        onOvalChanged?.invoke(
            FaceLivenessExternalOvalEvent(
                x = (frame.left / viewportWidth).toDouble(),
                y = (frame.top / viewportHeight).toDouble(),
                width = (frame.width() / viewportWidth).toDouble(),
                height = (frame.height() / viewportHeight).toDouble()
            )
        )
    }

    internal fun resolveMatchingOvalRect(
        awsOvalRect: RectF,
        videoViewportSize: VideoViewportSize?
    ): RectF {
        val configuration = matchingOvalConfiguration ?: return awsOvalRect
        val viewport = videoViewportSize ?: return awsOvalRect
        if (
            configuration.referenceViewportWidth <= 0 ||
            configuration.referenceViewportHeight <= 0
        ) {
            return awsOvalRect
        }

        val containerWidthDp = viewport.containerSize.width / viewport.density
        val containerHeightDp = viewport.containerSize.height / viewport.density
        val scale = min(
            containerWidthDp / configuration.referenceViewportWidth,
            containerHeightDp / configuration.referenceViewportHeight
        )
        val targetWidthPx = min(
            viewport.containerSize.width.toDouble(),
            configuration.width * scale * viewport.density
        ).toFloat()
        val targetHeightPx = min(
            viewport.containerSize.height.toDouble(),
            configuration.height * scale * viewport.density
        ).toFloat()
        val awsFrame = viewport.getBoundingRectInContainer(awsOvalRect)
        val targetFrame = RectF(
            awsFrame.centerX() - targetWidthPx / 2,
            awsFrame.centerY() - targetHeightPx / 2,
            awsFrame.centerX() + targetWidthPx / 2,
            awsFrame.centerY() + targetHeightPx / 2
        )
        return viewport.getUnscaledBoundingRect(targetFrame)
    }

}
