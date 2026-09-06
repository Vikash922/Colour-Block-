sed -i '88,103d' app/src/main/java/com/example/ui/GameScreen.kt
sed -i 's/Box(modifier = Modifier.offset { shakeOffset }) {/Box(modifier = Modifier) {/g' app/src/main/java/com/example/ui/GameScreen.kt
sed -i 's/import androidx.compose.ui.draw.clip/import androidx.compose.ui.draw.clip\nimport androidx.compose.ui.draw.scale/g' app/src/main/java/com/example/ui/GameScreen.kt
