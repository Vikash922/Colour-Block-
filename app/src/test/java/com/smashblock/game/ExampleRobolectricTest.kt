package com.smashblock.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.smashblock.game.data.ScorePreferencesRepository
import com.smashblock.game.model.ShapeFactory
import com.smashblock.game.viewmodel.GameViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smash Block", appName)
    }

    @Test
    fun `test score preferences dataStore save and load`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ScorePreferencesRepository(context)

        repo.saveHighScore(250)
        val loaded = repo.highScoreFlow.first()
        assertEquals(250, loaded)

        // Lower score shouldn't overwrite high score
        repo.saveHighScore(100)
        val loadedAgain = repo.highScoreFlow.first()
        assertEquals(250, loadedAgain)
    }
}
