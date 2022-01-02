/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eillia.ehya.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.eillia.ehya.model.data.entity.Sunnah
import com.eillia.ehya.ui.theme.BottomSheetShape
import com.eillia.ehya.viewmodels.SwipeResult
import kotlinx.coroutines.launch
import kotlin.math.abs

/*
* The reference for this code is Compose Cook Book repository
*      https://github.com/Gurupreet/ComposeCookBook
* */

@Composable
fun DraggableCard(
  item: Sunnah,
  modifier: Modifier = Modifier,
  onSwiped: (SwipeResult, Sunnah) -> Unit,
  content: @Composable () -> Unit
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val swipeXLeft = -(screenWidth.value * 3.2).toFloat()
  val swipeXRight = (screenWidth.value * 3.2).toFloat()
  val swipeYTop = -1000f
  val swipeYBottom = 1000f
  val swipeX = remember { Animatable(0f) }
  val swipeY = remember { Animatable(0f) }
  swipeX.updateBounds(swipeXLeft, swipeXRight)
  swipeY.updateBounds(swipeYTop, swipeYBottom)
  val rotationFraction = (swipeX.value / 60).coerceIn(-40f, 40f)
  val graphicLayer = Modifier.graphicsLayer(
    translationX = swipeX.value,
    translationY = swipeY.value,
    rotationZ = rotationFraction,
  )
  if (abs(swipeX.value) < swipeXRight - 50f) {
    Card(
      elevation = 16.dp,
      shape = BottomSheetShape,
      modifier = modifier
        .dragContent(
          swipeX = swipeX,
          swipeY = swipeY,
          maxX = swipeXRight,
          onSwiped = { _, _ ->
          }
        )
        .then(graphicLayer)
    ) {
      content()
    }
  } else {
    val swipeResult = if (swipeX.value > 0) SwipeResult.TRY else SwipeResult.PASS
    onSwiped(swipeResult, item)
  }
}

fun Modifier.dragContent(
  swipeX: Animatable<Float, AnimationVector1D>,
  swipeY: Animatable<Float, AnimationVector1D>,
  maxX: Float,
  onSwiped: (Any, Any) -> Unit
): Modifier = composed {
  val coroutineScope = rememberCoroutineScope()
  pointerInput(Unit) {
    this.detectDragGestures(
      onDragCancel = {
        coroutineScope.launch {
          swipeX.animateTo(0f)
          swipeY.animateTo(0f)
        }
      },
      onDragEnd = {
        coroutineScope.launch {
          if (abs(swipeX.targetValue) < abs(maxX) / 4) {
            swipeX.animateTo(0f, tween(400))
            swipeY.animateTo(0f, tween(400))
          } else {
            if (swipeX.targetValue > 0) {
              swipeX.animateTo(maxX, tween(400))
            } else {
              swipeX.animateTo(-maxX, tween(400))
            }
          }
        }
      }
    ) { change, dragAmount ->
      change.consumePositionChange()
      coroutineScope.launch {
        swipeX.animateTo(swipeX.targetValue + dragAmount.x)
        swipeY.animateTo(swipeY.targetValue + dragAmount.y)
      }
    }
  }
}