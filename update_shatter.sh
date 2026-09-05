sed -i '/\/\/ 5. Draw Cleared Line Burst Animation/,/            }/c\
            // 5. Draw Cleared Line Burst & Shatter Animation\
            if (clearAlpha > 0.01f && lastClearedIndices.isNotEmpty()) {\
                val clearedRows = lastClearedIndices.groupBy { it.first }.filter { it.value.size == 8 }.keys\
                val clearedCols = lastClearedIndices.groupBy { it.second }.filter { it.value.size == 8 }.keys\
\
                val isCombo = clearedRows.size + clearedCols.size >= 2\
\
                val glowBrush = if (isCombo) {\
                    Brush.linearGradient(\
                        colors = listOf(Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF4CD964), Color(0xFF5AC8FA), Color(0xFF5856D6)),\
                        start = Offset(0f, 0f), end = Offset(boardWidth, boardHeight)\
                    )\
                } else {\
                    Brush.horizontalGradient(\
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFF8E7), Color(0xFFFFD700))\
                    )\
                }\
\
                // Draw initial bright flash lines that fade fast\
                val flashAlpha = (clearAlpha - 0.5f).coerceAtLeast(0f) * 2f\
                if (flashAlpha > 0f) {\
                    clearedRows.forEach { r ->\
                        val y = r * (cellSize + spacing)\
                        drawRect(brush = glowBrush, topLeft = Offset(0f, y), size = Size(boardWidth, cellSize), alpha = flashAlpha)\
                    }\
                    clearedCols.forEach { c ->\
                        val x = c * (cellSize + spacing)\
                        drawRect(brush = glowBrush, topLeft = Offset(x, 0f), size = Size(cellSize, boardHeight), alpha = flashAlpha)\
                    }\
                }\
\
                // Draw Shattering Particles\
                val progress = 1f - clearAlpha\
                val gravityY = progress * progress * 400f\
\
                lastClearedIndices.forEach { (r, c) ->\
                    val baseX = c * (cellSize + spacing)\
                    val baseY = r * (cellSize + spacing)\
\
                    for (i in 0..3) {\
                        val isLeft = i % 2 == 0\
                        val isTop = i < 2\
                        val pieceSize = cellSize / 2f\
\
                        val seed = r * 31 + c * 17 + i\
                        val spreadX = ((seed % 10) - 5) * 12f * progress\
                        val spreadY = -((seed % 15) + 5) * 10f * progress + gravityY\
                        val rot = ((seed % 360) * progress * 2f)\
\
                        val startX = baseX + if (isLeft) 0f else pieceSize\
                        val startY = baseY + if (isTop) 0f else pieceSize\
\
                        val finalX = startX + spreadX\
                        val finalY = startY + spreadY\
\
                        withTransform({\
                            translate(left = finalX + pieceSize / 2, top = finalY + pieceSize / 2)\
                            rotate(rot)\
                        }) {\
                            drawRect(\
                                color = Color.White.copy(alpha = clearAlpha),\
                                topLeft = Offset(-pieceSize / 2, -pieceSize / 2),\
                                size = Size(pieceSize, pieceSize)\
                            )\
                        }\
                    }\
                }\
            }' app/src/main/java/com/example/ui/components/BoardComposable.kt
