sed -i 's/animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)/animationSpec = tween(durationMillis = 600, easing = LinearEasing)/g' app/src/main/java/com/example/ui/components/BoardComposable.kt

sed -i 's/import androidx.compose.ui.graphics.drawscope.DrawScope/import androidx.compose.ui.graphics.drawscope.DrawScope\nimport androidx.compose.ui.graphics.drawscope.withTransform/g' app/src/main/java/com/example/ui/components/BoardComposable.kt
