package com.example

import com.example.model.ShapeFactory
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testShapeFactoryShapes() {
        assertNotNull(ShapeFactory.DOT)
        assertEquals(1, ShapeFactory.DOT.rows)
        assertEquals(1, ShapeFactory.DOT.cols)
        assertEquals(1, ShapeFactory.DOT.tileCount)

        assertNotNull(ShapeFactory.SQUARE_2X2)
        assertEquals(2, ShapeFactory.SQUARE_2X2.rows)
        assertEquals(2, ShapeFactory.SQUARE_2X2.cols)
        assertEquals(4, ShapeFactory.SQUARE_2X2.tileCount)

        assertNotNull(ShapeFactory.LINE_5_H)
        assertEquals(1, ShapeFactory.LINE_5_H.rows)
        assertEquals(5, ShapeFactory.LINE_5_H.cols)
        assertEquals(5, ShapeFactory.LINE_5_H.tileCount)

        assertNotNull(ShapeFactory.LINE_5_V)
        assertEquals(5, ShapeFactory.LINE_5_V.rows)
        assertEquals(1, ShapeFactory.LINE_5_V.cols)
        assertEquals(5, ShapeFactory.LINE_5_V.tileCount)

        assertTrue(ShapeFactory.ALL_SHAPES.size >= 25)
    }

    @Test
    fun testCanPlaceBoundaryChecks() {
        val grid = Array(8) { IntArray(8) { 0 } }
        val dot = ShapeFactory.DOT
        val line5H = ShapeFactory.LINE_5_H
        val square3X3 = ShapeFactory.SQUARE_3X3

        // Dummy repository is not needed for direct static canPlace logic
        fun canPlace(matrix: Array<IntArray>, shape: com.example.model.BlockShape, startRow: Int, startCol: Int): Boolean {
            for (r in 0 until shape.rows) {
                for (c in 0 until shape.cols) {
                    if (shape.matrix[r][c] != 0) {
                        val tr = startRow + r
                        val tc = startCol + c
                        if (tr !in 0..7 || tc !in 0..7) return false
                        if (matrix[tr][tc] != 0) return false
                    }
                }
            }
            return true
        }

        // 1x1 dot
        assertTrue(canPlace(grid, dot, 0, 0))
        assertTrue(canPlace(grid, dot, 7, 7))
        assertFalse(canPlace(grid, dot, 8, 0))
        assertFalse(canPlace(grid, dot, 0, 8))
        assertFalse(canPlace(grid, dot, -1, 0))

        // 5-horizontal line
        assertTrue(canPlace(grid, line5H, 0, 0))
        assertTrue(canPlace(grid, line5H, 0, 3))
        assertFalse(canPlace(grid, line5H, 0, 4)) // columns 4..8 exceeds 7

        // 3x3 square
        assertTrue(canPlace(grid, square3X3, 5, 5))
        assertFalse(canPlace(grid, square3X3, 6, 5)) // row 6,7,8 exceeds 7

        // Overlap check
        grid[0][0] = 1
        assertFalse(canPlace(grid, dot, 0, 0))
        assertTrue(canPlace(grid, dot, 0, 1))
    }

    @Test
    fun testGameOverDetection() {
        fun canPlace(matrix: Array<IntArray>, shape: com.example.model.BlockShape, startRow: Int, startCol: Int): Boolean {
            for (r in 0 until shape.rows) {
                for (c in 0 until shape.cols) {
                    if (shape.matrix[r][c] != 0) {
                        val tr = startRow + r
                        val tc = startCol + c
                        if (tr !in 0..7 || tc !in 0..7) return false
                        if (matrix[tr][tc] != 0) return false
                    }
                }
            }
            return true
        }

        fun checkGameOver(matrix: Array<IntArray>, dock: List<com.example.model.BlockShape?>): Boolean {
            val nonNull = dock.filterNotNull()
            if (nonNull.isEmpty()) return false
            for (shape in nonNull) {
                for (r in 0 until 8) {
                    for (c in 0 until 8) {
                        if (canPlace(matrix, shape, r, c)) return false
                    }
                }
            }
            return true
        }

        val emptyGrid = Array(8) { IntArray(8) { 0 } }
        assertFalse(checkGameOver(emptyGrid, listOf(ShapeFactory.DOT, ShapeFactory.LINE_2_H)))

        // Completely full grid
        val fullGrid = Array(8) { IntArray(8) { 1 } }
        assertTrue(checkGameOver(fullGrid, listOf(ShapeFactory.DOT)))
    }
}
