#!/bin/bash
sed -i 's/val lastClearedIndices: Set<Pair<Int, Int>> = emptySet()/val lastClearedIndices: Map<Pair<Int, Int>, Int> = emptyMap()/g' app/src/main/java/com/example/model/GameState.kt
