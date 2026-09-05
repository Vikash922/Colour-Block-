package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BlockShape
import kotlinx.coroutines.delay

@Composable
fun DockSlot(
    index: Int,
    shape: BlockShape?,
    isBeingDragged: Boolean,
    dockCellSize: Dp,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember(shape) { Animatable(if (shape != null) 0f else 1f) }

    LaunchedEffect(shape) {
        if (shape != null) {
            delay((index * 150).toLong()) // Staggered appearance
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                onPositioned(coordinates)
            }
            .then(
                if (shape != null) {
                    Modifier.pointerInput(shape) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                onDragStart(offset)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            },
                            onDragEnd = {
                                onDragEnd()
                            },
                            onDragCancel = {
                                onDragCancel()
                            }
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (shape != null) {
            BlockShapeComposable(
                shape = shape,
                cellSize = dockCellSize,
                cellSpacing = 2.dp,
                alpha = if (isBeingDragged) 0f else 1f, // Hide completely when dragging to match reference
                modifier = Modifier
                    .padding(6.dp)
                    .scale(scaleAnim.value)
            )
        }
    }
}
