sed -i 's/onRestart = { viewModel.initGame() },/onPlayAgain = { viewModel.initGame() }/g' app/src/main/java/com/example/ui/GameScreen.kt
sed -i '/onHomeClick = onHomeClick/d' app/src/main/java/com/example/ui/GameScreen.kt
