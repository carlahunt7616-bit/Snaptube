package com.example

import com.example.analyzer.MediaLinkAnalyzer
import com.example.data.model.FormatUtils
import com.example.data.model.PlatformType
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun urlExtractionAndValidation_worksCorrectly() {
        val sharedText = "Check out this video! https://example.com/clips/demo_720p.mp4?ref=share."
        val extracted = FormatUtils.extractFirstHttpUrl(sharedText)
        assertEquals("https://example.com/clips/demo_720p.mp4?ref=share", extracted)
        assertTrue(FormatUtils.isValidHttpUrl(extracted!!))
        assertFalse(FormatUtils.isValidHttpUrl("ftp://invalid.host/file.mp4"))
        assertFalse(FormatUtils.isValidHttpUrl("not a url"))
    }

    @Test
    fun platformDetection_recognizesAllPlatformsAndDirectUrls() {
        val analyzer = MediaLinkAnalyzer(OkHttpClient())
        assertEquals(PlatformType.YOUTUBE, analyzer.detectPlatform("https://youtu.be/abc123"))
        assertEquals(PlatformType.FACEBOOK, analyzer.detectPlatform("https://www.facebook.com/watch/?v=123"))
        assertEquals(PlatformType.INSTAGRAM, analyzer.detectPlatform("https://www.instagram.com/reel/xyz/"))
        assertEquals(PlatformType.TIKTOK, analyzer.detectPlatform("https://vm.tiktok.com/ZM8abc/"))
        assertEquals(PlatformType.X_TWITTER, analyzer.detectPlatform("https://x.com/user/status/123"))
        assertEquals(PlatformType.PINTEREST, analyzer.detectPlatform("https://pin.it/abc123"))
        assertEquals(PlatformType.REDDIT, analyzer.detectPlatform("https://www.reddit.com/r/videos/comments/abc/"))
        assertEquals(PlatformType.VIMEO, analyzer.detectPlatform("https://vimeo.com/123456"))
        assertEquals(PlatformType.DAILYMOTION, analyzer.detectPlatform("https://dai.ly/x8abc"))
        assertEquals(PlatformType.DIRECT, analyzer.detectPlatform("https://commondatastorage.googleapis.com/sample/video.mp4"))
    }

    @Test
    fun formattingHelpers_formatBytesSpeedAndFileName() {
        assertEquals("1.5 MB", FormatUtils.formatBytes(1_572_864L))
        assertEquals("512 KB/s", FormatUtils.formatSpeed(512L * 1024L))
        assertEquals("2m 05s", FormatUtils.formatEta(125L))
        assertEquals("My_Video_Clip.mp4", FormatUtils.sanitizeFileName("My/Video:Clip", "mp4"))
    }
}
