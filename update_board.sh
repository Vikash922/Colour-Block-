sed -i '/    \/\/ Right shadow trapezoid (darker)/i\
    \/\/ Inner diagonal reflection shine\n    val shinePath = Path().apply {\n        moveTo(x + bevelWidth, y + bevelWidth)\n        lineTo(x + cellSize * 0.6f, y + bevelWidth)\n        lineTo(x + bevelWidth, y + cellSize * 0.6f)\n        close()\n    }\n    drawPath(path = shinePath, color = Color.White.copy(alpha = 0.2f * color.alpha))\n' app/src/main/java/com/example/ui/components/BlockShapeComposable.kt

sed -i '/    \/\/ Right shadow trapezoid (darker)/i\
    \/\/ Inner diagonal reflection shine\n    val shinePath = Path().apply {\n        moveTo(x + bevelWidth, y + bevelWidth)\n        lineTo(x + cellSize * 0.6f, y + bevelWidth)\n        lineTo(x + bevelWidth, y + cellSize * 0.6f)\n        close()\n    }\n    drawPath(path = shinePath, color = Color.White.copy(alpha = 0.2f * color.alpha))\n' app/src/main/java/com/example/ui/components/BoardComposable.kt
