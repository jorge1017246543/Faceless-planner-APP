package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Planificador Faceless", appName)
    }

    @Test
    fun `main navigation tabs render and switch screens cleanly`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("currency_selector_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_calculator").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("live_bar_gross").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_scenarios").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tab_saved_scenarios").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_methodology").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("methodology_screen").assertIsDisplayed()
    }
}
