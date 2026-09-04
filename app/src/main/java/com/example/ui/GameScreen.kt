package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BlockShape
import com.example.ui.components.BlockShapeComposable
import com.example.ui.components.BoardComposable
import com.example.ui.components.ComboBlastOverlay
import com.example.ui.components.DockComposable
import com.example.ui.components.GameOverDialog
import com.example.ui.components.HeaderComposable
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BlockRed
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.PreviewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onHomeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isDailyChallenge by viewModel.isDailyChallenge.collectAsState()
    val comboEvent by viewModel.lastComboEvent.collectAsState()

    val density = LocalDensity.current

    // Board layout coordinates for touch translation
    var boardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val slotCoordinates = remember { mutableMapOf<Int, LayoutCoordinates>() }

    // Drag-and-drop state
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragWindowPosition by remember { mutableStateOf(Offset.Zero) }
    var ghostPreviewState by remember { mutableStateOf<PreviewState?>(null) }
    var currentTargetRowCol by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // High Score animation
    var showNewBestAnimation by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(comboEvent) {
        if (comboEvent != null) {
            val intensity = (comboEvent!!.comboCount * 8f).coerceAtMost(40f)
            scope.launch {
                repeat(4) {
                    shakeOffset.animateTo(if (it % 2 == 0) intensity else -intensity, tween(50, easing = LinearEasing))
                }
                shakeOffset.animateTo(0f, tween(50))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.newHighScoreEvent.collect {
            showNewBestAnimation = true
            delay(2500)
            showNewBestAnimation = false
        }
    }

    // Constants in pixels
    val verticalOffsetPx = with(density) { 75.dp.toPx() }
    val boardCellSizeDp = 40.dp
    val boardCellSizePx = with(density) { boardCellSizeDp.toPx() }

    fun calculateDropTarget(shape: BlockShape, dragWindowPos: Offset): Pair<Int, Int>? {
        val board = boardCoordinates ?: return null
        val boardPosInWindow = board.positionInWindow()

        val shapeWidthPx = shape.cols * boardCellSizePx
        val shapeHeightPx = shape.rows * boardCellSizePx

        // The top-left corner of the piece floating above the finger
        val pieceTopLeftX = dragWindowPos.x - (shapeWidthPx / 2f)
        val pieceTopLeftY = dragWindowPos.y - (shapeHeightPx / 2f) - verticalOffsetPx

        val relativeX = pieceTopLeftX - boardPosInWindow.x
        val relativeY = pieceTopLeftY - boardPosInWindow.y

        val boardWidth = board.size.width.toFloat()
        // Account for board padding: 10dp outer + 2dp inner = 12dp
        val boardPaddingPx = with(density) { 12.dp.toPx() }
        val spacingPx = with(density) { 4.5f.dp.toPx() }
        val effectiveCellSize = (boardWidth - (boardPaddingPx * 2) - (spacingPx * 7)) / 8f
        val step = effectiveCellSize + spacingPx

        val targetCol = ((relativeX - boardPaddingPx + step * 0.5f) / step).toInt().coerceIn(-1, 8)
        val targetRow = ((relativeY - boardPaddingPx + step * 0.5f) / step).toInt().coerceIn(-1, 8)

        return if (targetRow in 0..7 && targetCol in 0..7) {
            Pair(targetRow, targetCol)
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header (Scores, Combo, Actions)
            HeaderComposable(
                onSettingsClick = { showSettingsDialog = true },
                score = gameState.score,
                highScore = gameState.highScore,
                comboCount = gameState.comboCount,
                isSoundEnabled = isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onRestartGame = { viewModel.initGame() },
                isDailyChallenge = isDailyChallenge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Board Container (8x8 Grid with relative Combo Overlay)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BoardComposable(
                    grid = gameState.grid,
                    previewState = ghostPreviewState,
                    ghostColor = draggingIndex?.let { gameState.dock.getOrNull(it)?.color },
                    lastClearedIndices = gameState.lastClearedIndices,
                    onBoardGloballyPositioned = { coords ->
                        boardCoordinates = coords
                    }
                )

                // Floating Combo Blast Overlay over the board center
                ComboBlastOverlay(
                    comboEvent = comboEvent,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Dock Container
            DockComposable(
                dock = gameState.dock,
                activeDraggingIndex = draggingIndex,
                onDragStart = { index, startPosInSlot ->
                    val slotCoord = slotCoordinates[index]
                    val initialWindowPos = if (slotCoord != null && slotCoord.isAttached) {
                        slotCoord.positionInWindow() + startPosInSlot
                    } else {
                        startPosInSlot
                    }
                    draggingIndex = index
                    dragWindowPosition = initialWindowPos

                    val shape = gameState.dock.getOrNull(index)
                    if (shape != null) {
                        val target = calculateDropTarget(shape, initialWindowPos)
                        currentTargetRowCol = target
                        ghostPreviewState = if (target != null) {
                            viewModel.getPreviewState(shape, target.first, target.second)
                        } else null
                    }
                },
                onDrag = { dragAmount ->
                    dragWindowPosition += dragAmount
                    val currentIndex = draggingIndex
                    val shape = if (currentIndex != null) gameState.dock.getOrNull(currentIndex) else null
                    if (shape != null) {
                        val target = calculateDropTarget(shape, dragWindowPosition)
                        currentTargetRowCol = target
                        ghostPreviewState = if (target != null) {
                            viewModel.getPreviewState(shape, target.first, target.second)
                        } else null
                    }
                },
                onDragEnd = {
                    val currentIndex = draggingIndex
                    val target = currentTargetRowCol
                    if (currentIndex != null && target != null) {
                        viewModel.placeShape(currentIndex, target.first, target.second)
                    }
                    draggingIndex = null
                    ghostPreviewState = null
                    currentTargetRowCol = null
                },
                onDragCancel = {
                    draggingIndex = null
                    ghostPreviewState = null
                    currentTargetRowCol = null
                },
                onSlotPositioned = { index, coords ->
                    slotCoordinates[index] = coords
                },
                dockCellSize = 24.dp // Slightly larger dock cells as requested
            )
        }

        // 4. Floating Drag Layer (Full 1.0x scale board cell size with vertical finger offset)
        if (draggingIndex != null) {
            val draggedShape = gameState.dock.getOrNull(draggingIndex!!)
            if (draggedShape != null) {
                val shapeWidthPx = draggedShape.cols * boardCellSizePx
                val shapeHeightPx = draggedShape.rows * boardCellSizePx

                val xOffset = (dragWindowPosition.x - shapeWidthPx / 2f).roundToInt()
                val yOffset = (dragWindowPosition.y - shapeHeightPx / 2f - verticalOffsetPx).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xOffset, yOffset) }
                ) {
                    BlockShapeComposable(
                        shape = draggedShape,
                        cellSize = boardCellSizeDp,
                        cellSpacing = 4.dp
                    )
                }
            }
        }
        
        // 5. New High Score Center Animation
        AnimatedVisibility(
            visible = showNewBestAnimation,
            enter = scaleIn(tween(500, easing = FastOutSlowInEasing)) + fadeIn(),
            exit = scaleOut(tween(300)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xD9000000))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NEW BEST!",
                        color = Color(0xFFFFD700),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = gameState.score.toString(),
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 6. Game Over Dialog
        if (gameState.isGameOver) {
            GameOverDialog(
                score = gameState.score,
                highScore = gameState.highScore,
                onPlayAgain = {
                    viewModel.initGame()
                }
            )
        }

        // 7. Settings / Pause Dialog
        if (showSettingsDialog) {
            Dialog(onDismissRequest = { showSettingsDialog = false }) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2C39B0))
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "PAUSED",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )

                        // Sound Toggle
                        Button(
                            onClick = { viewModel.toggleSound() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSoundEnabled) Color(0xFF00C853) else BlockRed),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = "Toggle Sound"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (isSoundEnabled) "Sound: ON" else "Sound: OFF", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        // Home Button
                        Button(
                            onClick = { 
                                showSettingsDialog = false
                                onHomeClick() 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Quit to Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        // Resume Button
                        Button(
                            onClick = { showSettingsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Resume", color = Color(0xFF2C39B0), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}
