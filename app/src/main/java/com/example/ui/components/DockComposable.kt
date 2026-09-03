package com.example.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BlockShape

@Composable
fun DockComposable(
    dock: List<BlockShape?>,
    activeDraggingIndex: Int?,
    onDragStart: (index: Int, startPositionInWindow: Offset) -> Unit,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onSlotPositioned: (index: Int, coordinates: LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
    dockCellSize: Dp = 24.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag("game_dock"),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val shape = dock.getOrNull(i)
            val isBeingDragged = (activeDraggingIndex == i)

            DockSlot(
                shape = shape,
                isBeingDragged = isBeingDragged,
                dockCellSize = dockCellSize,
                onDragStart = { startPos -> onDragStart(i, startPos) },
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onPositioned = { coords -> onSlotPositioned(i, coords) },
                modifier = Modifier
                    .weight(1f)
                    .height(118.dp)
                    .testTag("dock_slot_$i")
            )
        }
    }
}

@Composable
private fun DockSlot(
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
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}
