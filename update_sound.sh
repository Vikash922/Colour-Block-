sed -i 's/val freq = 520.0 - (t \/ duration) \* 260.0/val freq = 700.0 - (t \/ duration) \* 600.0/g' app/src/main/java/com/example/audio/SoundManager.kt
sed -i 's/val env = exp(-t \* 45.0)/val env = exp(-t \* 60.0)/g' app/src/main/java/com/example/audio/SoundManager.kt

sed -i 's/val duration = 0.28/val duration = 0.50/g' app/src/main/java/com/example/audio/SoundManager.kt
sed -i 's/val note1 = sin(2.0 \* PI \* 587.33 \* t) \/\/ D5/val note1 = sin(2.0 \* PI \* 523.25 \* t) \/\/ C5/g' app/src/main/java/com/example/audio/SoundManager.kt
sed -i 's/val note2 = sin(2.0 \* PI \* 880.00 \* t) \/\/ A5/val note2 = sin(2.0 \* PI \* 659.25 \* t) \/\/ E5/g' app/src/main/java/com/example/audio/SoundManager.kt
sed -i 's/val note3 = sin(2.0 \* PI \* 1174.66 \* t) \/\/ D6/val note3 = sin(2.0 \* PI \* 783.99 \* t) \/\/ G5/g' app/src/main/java/com/example/audio/SoundManager.kt

sed -i 's/val freqs = doubleArrayOf(523.25, 659.25, 1046.50) \/\/ C5, E5, C6/val freqs = doubleArrayOf(783.99, 1046.50, 1567.98) \/\/ G5, C6, G6/g' app/src/main/java/com/example/audio/SoundManager.kt
