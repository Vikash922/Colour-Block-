package com.smashblock.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.smashblock.game.model.GameState
import com.smashblock.game.model.ShapeFactory
import com.smashblock.game.ui.components.BoardComposable
import com.smashblock.game.ui.components.HeaderComposable
import com.smashblock.game.ui.theme.ColorBlockTheme
import com.smashblock.game.ui.theme.DeepSpace
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val grid = Array(8) { IntArray(8) { 0 } }
    // Add sample colorful blocks on grid
    grid[3][3] = 1
    grid[3][4] = 2
    grid[4][3] = 3
    grid[4][4] = 4

    composeTestRule.setContent {
      ColorBlockTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = DeepSpace) {
          BoardComposable(
            grid = grid,
            ghostCells = null,
            ghostColor = null,
            lastClearedIndices = emptySet(),
            onBoardGloballyPositioned = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
