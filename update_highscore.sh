sed -i 's/import com.example.ui.components.HeaderComposable/import com.example.ui.components.HeaderComposable\nimport com.example.ui.components.NewHighScoreOverlay/g' app/src/main/java/com/example/ui/GameScreen.kt

sed -i '/AnimatedVisibility(/,/} \/\/ end of AnimatedVisibility/c\
        NewHighScoreOverlay(\n            visible = showNewBestAnimation,\n            modifier = Modifier.align(Alignment.Center)\n        )' app/src/main/java/com/example/ui/GameScreen.kt
