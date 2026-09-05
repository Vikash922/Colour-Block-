sed -i 's/val intensity = 10f/val intensity = 4f/g' app/src/main/java/com/example/ui/GameScreen.kt
sed -i 's/val intensity = (comboEvent!!.comboCount \* 8f).coerceAtMost(40f)/val intensity = (comboEvent!!.comboCount \* 3f).coerceAtMost(12f)/g' app/src/main/java/com/example/ui/GameScreen.kt
