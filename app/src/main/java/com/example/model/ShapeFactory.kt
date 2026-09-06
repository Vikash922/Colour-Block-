package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import kotlin.random.Random

object ShapeColors {
    val Amber = BlockYellow      // ID 1
    val Cyan = BlockCyan         // ID 2
    val Rose = BlockRed          // ID 3
    val Emerald = BlockGreen     // ID 4
    val Violet = BlockPurple     // ID 5
    val Coral = BlockOrange      // ID 6
    val ElectricBlue = BlockBlue // ID 7
    val Magenta = BlockPink      // ID 8

    fun getColorForId(id: Int): Color {
        return when (id) {
            1 -> Amber
            2 -> Cyan
            3 -> Rose
            4 -> Emerald
            5 -> Violet
            6 -> Coral
            7 -> ElectricBlue
            8 -> Magenta
            else -> Cyan
        }
    }

    fun getIdForColor(color: Color): Int {
        return when (color) {
            Amber -> 1
            Cyan -> 2
            Rose -> 3
            Emerald -> 4
            Violet -> 5
            Coral -> 6
            ElectricBlue -> 7
            Magenta -> 8
            else -> 2
        }
    }
}

object ShapeFactory {

    private fun createShape(id: String, matrix: Array<IntArray>, color: Color): BlockShape {
        return BlockShape(id = id, matrix = matrix, color = color)
    }

    // 1x1 Dot
    val DOT = createShape("dot_1x1", arrayOf(intArrayOf(1)), ShapeColors.Amber)

    // Lines - Horizontal
    val LINE_2_H = createShape("line_2_h", arrayOf(intArrayOf(1, 1)), ShapeColors.Cyan)
    val LINE_3_H = createShape("line_3_h", arrayOf(intArrayOf(1, 1, 1)), ShapeColors.Cyan)
    val LINE_4_H = createShape("line_4_h", arrayOf(intArrayOf(1, 1, 1, 1)), ShapeColors.ElectricBlue)
    val LINE_5_H = createShape("line_5_h", arrayOf(intArrayOf(1, 1, 1, 1, 1)), ShapeColors.ElectricBlue)

    // Lines - Vertical
    val LINE_2_V = createShape("line_2_v", arrayOf(intArrayOf(1), intArrayOf(1)), ShapeColors.Cyan)
    val LINE_3_V = createShape("line_3_v", arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1)), ShapeColors.Cyan)
    val LINE_4_V = createShape("line_4_v", arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)), ShapeColors.ElectricBlue)
    val LINE_5_V = createShape("line_5_v", arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)), ShapeColors.ElectricBlue)

    // Squares
    val SQUARE_2X2 = createShape(
        "square_2x2",
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 1)
        ),
        ShapeColors.Amber
    )
    val SQUARE_3X3 = createShape(
        "square_3x3",
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 1, 1),
            intArrayOf(1, 1, 1)
        ),
        ShapeColors.Coral
    )

    // Corner Shapes 2x2 (3 cells)
    val CORNER_TL = createShape(
        "corner_tl",
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        ),
        ShapeColors.Emerald
    )
    val CORNER_TR = createShape(
        "corner_tr",
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        ),
        ShapeColors.Emerald
    )
    val CORNER_BL = createShape(
        "corner_bl",
        arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1)
        ),
        ShapeColors.Emerald
    )
    val CORNER_BR = createShape(
        "corner_br",
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1)
        ),
        ShapeColors.Emerald
    )

    // Large Corner Shapes 3x3 (5 cells)
    val CORNER_3X3_TL = createShape(
        "corner_3x3_tl",
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 0, 0),
            intArrayOf(1, 0, 0)
        ),
        ShapeColors.Magenta
    )
    val CORNER_3X3_TR = createShape(
        "corner_3x3_tr",
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 1),
            intArrayOf(0, 0, 1)
        ),
        ShapeColors.Magenta
    )
    val CORNER_3X3_BL = createShape(
        "corner_3x3_bl",
        arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1)
        ),
        ShapeColors.Magenta
    )
    val CORNER_3X3_BR = createShape(
        "corner_3x3_br",
        arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(0, 0, 1),
            intArrayOf(1, 1, 1)
        ),
        ShapeColors.Magenta
    )

    // L-Shapes 3x2 (4 cells)
    val L_SHAPE_3X2_TL = createShape(
        "l_3x2_tl",
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 0),
            intArrayOf(1, 0)
        ),
        ShapeColors.Rose
    )
    val L_SHAPE_3X2_TR = createShape(
        "l_3x2_tr",
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(0, 1),
            intArrayOf(0, 1)
        ),
        ShapeColors.Rose
    )
    val L_SHAPE_3X2_BL = createShape(
        "l_3x2_bl",
        arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 0),
            intArrayOf(1, 1)
        ),
        ShapeColors.Rose
    )
    val L_SHAPE_3X2_BR = createShape(
        "l_3x2_br",
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(0, 1),
            intArrayOf(1, 1)
        ),
        ShapeColors.Rose
    )

    // L-Shapes 2x3 Horizontal (4 cells)
    val L_SHAPE_2X3_TL = createShape(
        "l_2x3_tl",
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 0, 0)
        ),
        ShapeColors.Rose
    )
    val L_SHAPE_2X3_TR = createShape(
        "l_2x3_tr",
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 1)
        ),
        ShapeColors.Rose
    )
    val L_SHAPE_2X3_BL = createShape(
        "l_2x3_bl",
        arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 1)
        ),
        ShapeColors.Rose
    )
    val L_SHAPE_2X3_BR = createShape(
        "l_2x3_br",
        arrayOf(
            intArrayOf(0, 0, 1),
            intArrayOf(1, 1, 1)
        ),
        ShapeColors.Rose
    )

    // T-Shapes 4 rotations
    val T_SHAPE_UP = createShape(
        "t_up",
        arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1)
        ),
        ShapeColors.Violet
    )
    val T_SHAPE_DOWN = createShape(
        "t_down",
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 0)
        ),
        ShapeColors.Violet
    )
    val T_SHAPE_LEFT = createShape(
        "t_left",
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        ),
        ShapeColors.Violet
    )
    val T_SHAPE_RIGHT = createShape(
        "t_right",
        arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        ),
        ShapeColors.Violet
    )

    // S and Z shapes
    val S_SHAPE_H = createShape(
        "s_h",
        arrayOf(
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0)
        ),
        ShapeColors.Coral
    )
    val S_SHAPE_V = createShape(
        "s_v",
        arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        ),
        ShapeColors.Coral
    )
    val Z_SHAPE_H = createShape(
        "z_h",
        arrayOf(
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1)
        ),
        ShapeColors.ElectricBlue
    )
    val Z_SHAPE_V = createShape(
        "z_v",
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        ),
        ShapeColors.ElectricBlue
    )

    val ALL_SHAPES = listOf(
        DOT,
        LINE_2_H, LINE_2_V,
        LINE_3_H, LINE_3_V,
        LINE_4_H, LINE_4_V,
        LINE_5_H, LINE_5_V,
        SQUARE_2X2, SQUARE_3X3,
        CORNER_TL, CORNER_TR, CORNER_BL, CORNER_BR,
        CORNER_3X3_TL, CORNER_3X3_TR, CORNER_3X3_BL, CORNER_3X3_BR,
        L_SHAPE_3X2_TL, L_SHAPE_3X2_TR, L_SHAPE_3X2_BL, L_SHAPE_3X2_BR,
        L_SHAPE_2X3_TL, L_SHAPE_2X3_TR, L_SHAPE_2X3_BL, L_SHAPE_2X3_BR,
        T_SHAPE_UP, T_SHAPE_DOWN, T_SHAPE_LEFT, T_SHAPE_RIGHT,
        S_SHAPE_H, S_SHAPE_V,
        Z_SHAPE_H, Z_SHAPE_V
    )

    fun getRandomShape(random: Random = Random.Default): BlockShape {
        return ALL_SHAPES.random(random)
    }

    private val EASY_SHAPES = listOf(DOT, LINE_2_H, LINE_2_V, LINE_3_H, LINE_3_V, SQUARE_2X2, CORNER_TL, CORNER_TR, CORNER_BL, CORNER_BR)

    fun getRandomDockShapes(count: Int = 3, random: Random = Random.Default): List<BlockShape> {
        return List(count) {
            if (random.nextFloat() < 0.75f) EASY_SHAPES.random(random) else ALL_SHAPES.random(random)
        }
    }
}
