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

package com.amplifyframework.ui.liveness.ui

import android.content.Context
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.amplifyframework.predictions.aws.models.ColorDisplayInformation
import com.amplifyframework.ui.liveness.camera.OnFreshnessColorDisplayed
import com.amplifyframework.ui.liveness.model.LivenessCheckState
import com.amplifyframework.ui.liveness.state.LivenessState
import com.amplifyframework.ui.liveness.ui.helper.VideoViewportSize

@Composable
internal fun ExternalUiStateEffect(
    controller: FaceLivenessExternalUiController?,
    state: LivenessState,
    context: Context
) {
    val phase = if (state.livenessCheckState is LivenessCheckState.Success) {
        "verifying"
    } else {
        "positioning"
    }
    val instruction = state.livenessCheckState.instructionId
        ?.let(context::getString)
        .orEmpty()
    val progress = if (state.faceGuideRect == null) 0.0 else state.faceMatchPercentage.toDouble()

    LaunchedEffect(controller, phase, instruction, progress) {
        if (controller?.rendersUiExternally == true) {
            controller.publishUiState(phase, instruction, progress)
        }
    }
}

@Composable
internal fun ExternalOvalEffect(
    controller: FaceLivenessExternalUiController?,
    oval: RectF?,
    viewport: VideoViewportSize
) {
    val containerRect = oval?.let(viewport::getBoundingRectInContainer)
    LaunchedEffect(controller, containerRect, viewport.containerSize) {
        if (controller?.rendersUiExternally == true && containerRect != null) {
            controller.publishOval(
                frame = containerRect,
                viewportWidth = viewport.containerSize.width.toFloat(),
                viewportHeight = viewport.containerSize.height.toFloat()
            )
        }
    }
}

@Composable
internal fun ExternalFreshnessChallenge(
    key: Any,
    modifier: Modifier,
    colors: List<ColorDisplayInformation>,
    controller: FaceLivenessExternalUiController?,
    onColorDisplayed: OnFreshnessColorDisplayed,
    onComplete: () -> Unit
) {
    LaunchedEffect(key, controller) {
        controller?.start()
    }

    FreshnessChallenge(
        key = key,
        modifier = modifier,
        colors = colors,
        onColorDisplayed = onColorDisplayed,
        onComplete = {
            controller?.complete()
            onComplete()
        }
    )
}
