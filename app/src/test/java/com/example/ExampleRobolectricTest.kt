package com.example

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appName_matchesSnapTubeBranding() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("SnapTube", context.getString(R.string.app_name))
        assertEquals("Download Anything, Anywhere.", context.getString(R.string.app_tagline))
    }

    @Test
    fun shareToSnapTubeIntentFilter_isRegistered() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/video.mp4")
        }
        val resolved = context.packageManager.queryIntentActivities(shareIntent, 0)
        assertTrue(
            "SharePopupActivity should handle ACTION_SEND text/plain intents",
            resolved.any { it.activityInfo.name.endsWith("SharePopupActivity") }
        )
    }

    @Test
    fun mainActivity_launchesWithoutCompositionLocalCrash() {
        composeRule.onNodeWithTag("splash_screen").assertExists()
        composeRule.onNodeWithTag("splash_watermark").assertExists()
    }
}
