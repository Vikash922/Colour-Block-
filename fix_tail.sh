cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/GameScreen.kt
        NewHighScoreOverlay(
            visible = showNewBestAnimation,
            modifier = Modifier.align(Alignment.Center)
        )
        
        if (showNoSpaceOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO MOVES LEFT",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    if (showGameOverScreen) {
        GameOverDialog(
            score = gameState.score,
            highScore = gameState.highScore,
            onRestart = { viewModel.initGame() },
            onHomeClick = onHomeClick
        )
    }
}
INNER_EOF
