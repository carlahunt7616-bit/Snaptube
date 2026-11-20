package com.example.analyzer

import com.example.data.model.AnalyzedMedia
import com.example.data.model.FormatUtils
import com.example.data.model.MediaFormat
import com.example.data.model.MediaOption
import com.example.data.model.MediaType
import com.example.data.model.PlatformType
import com.example.data.model.SampleMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

interface PlatformHandler {
    val platform: PlatformType
    fun matches(host: String): Boolean
    suspend fun analyze(url: String, client: OkHttpClient): AnalyzedMedia
}

object SampleMediaCatalog {
    val items: List<SampleMediaItem> = listOf(
        SampleMediaItem(
            title = "For Bigger Blazes (HD)",
            subtitle = "Google Public Sample • MP4 • 720p HD",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            mediaType = MediaType.VIDEO,
            badge = "MP4"
        ),
        SampleMediaItem(
            title = "For Bigger Escapes (HD)",
            subtitle = "Google Public Sample • MP4 • 720p HD",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            mediaType = MediaType.VIDEO,
            badge = "MP4"
        ),
        SampleMediaItem(
            title = "SoundHelix Electronic Track",
            subtitle = "Creative Commons Audio • MP3 • 192 kbps",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            mediaType = MediaType.AUDIO,
            badge = "MP3"
        )
    )
}

class MediaLinkAnalyzer(private val okHttpClient: OkHttpClient) {

    private val restrictedHandlers: List<PlatformHandler> = listOf(
        RestrictedSocialPlatformHandler(
            platform = PlatformType.YOUTUBE,
            oEmbedEndpoint = "https://www.youtube.com/oembed?format=json&url="
        ),
        RestrictedSocialPlatformHandler(platform = PlatformType.FACEBOOK),
        RestrictedSocialPlatformHandler(platform = PlatformType.INSTAGRAM),
        RestrictedSocialPlatformHandler(
            platform = PlatformType.TIKTOK,
            oEmbedEndpoint = "https://www.tiktok.com/oembed?url="
        ),
        RestrictedSocialPlatformHandler(platform = PlatformType.X_TWITTER),
        RestrictedSocialPlatformHandler(platform = PlatformType.PINTEREST),
        RestrictedSocialPlatformHandler(
            platform = PlatformType.REDDIT,
            oEmbedEndpoint = "https://www.reddit.com/oembed?url="
        ),
        RestrictedSocialPlatformHandler(
            platform = PlatformType.VIMEO,
            oEmbedEndpoint = "https://vimeo.com/api/oembed.json?url="
        ),
        RestrictedSocialPlatformHandler(
            platform = PlatformType.DAILYMOTION,
            oEmbedEndpoint = "https://www.dailymotion.com/services/oembed?format=json&url="
        )
    )

    fun detectPlatform(url: String): PlatformType {
        val host = runCatching { URI(url.trim()).host?.lowercase().orEmpty() }.getOrDefault("")
        if (host.isBlank()) return PlatformType.DIRECT
        return PlatformType.entries.firstOrNull { platform ->
            platform != PlatformType.DIRECT && platform.hosts.any { h ->
                host == h || host.endsWith(".$h")
            }
        } ?: PlatformType.DIRECT
    }

    suspend fun analyze(rawUrl: String): Result<AnalyzedMedia> = withContext(Dispatchers.IO) {
        val cleanUrl = FormatUtils.extractFirstHttpUrl(rawUrl) ?: rawUrl.trim()
        if (!FormatUtils.isValidHttpUrl(cleanUrl)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid http:// or https:// media URL."))
        }

        val host = runCatching { URI(cleanUrl).host?.lowercase().orEmpty() }.getOrDefault("")

        // Check if this is a direct media file URL first (even on CDN subdomains)
        val probe = probeDirectMedia(cleanUrl)
        if (probe != null && probe.isDirectMedia) {
            return@withContext Result.success(buildDirectAnalyzedMedia(cleanUrl, detectPlatform(cleanUrl), probe))
        }

        // Check modular platform handlers for known social/video platforms
        val matchedHandler = restrictedHandlers.firstOrNull { it.matches(host) }
        if (matchedHandler != null) {
            return@withContext Result.success(matchedHandler.analyze(cleanUrl, okHttpClient))
        }

        // For general open-access web pages, inspect public HTML OpenGraph / <video> / <audio> tags
        val openPageMedia = inspectOpenHtmlPage(cleanUrl)
        if (openPageMedia != null) {
            return@withContext Result.success(openPageMedia)
        }

        Result.success(
            AnalyzedMedia(
                sourceUrl = cleanUrl,
                platform = PlatformType.DIRECT,
                title = inferTitleFromUrl(cleanUrl),
                isSupportedForDownload = false,
                unsupportedReason = "No direct downloadable video or audio stream was found at this URL. Paste a direct media file URL (.mp4, .mp3, .m4a, .webm) or an authorized open-access media page."
            )
        )
    }

    private data class DirectProbeResult(
        val finalUrl: String,
        val contentType: String,
        val contentLength: Long,
        val acceptsRanges: Boolean,
        val dispositionFileName: String?,
        val isDirectMedia: Boolean
    )

    private fun probeDirectMedia(url: String): DirectProbeResult? {
        // Try lightweight HEAD first
        val headResult = runCatching {
            val req = Request.Builder()
                .url(url)
                .head()
                .header("User-Agent", USER_AGENT)
                .build()
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                parseProbeHeaders(resp.request.url.toString(), resp)
            }
        }.getOrNull()

        if (headResult != null && headResult.isDirectMedia) return headResult

        // Fallback tiny byte-range GET (bytes=0-0) for servers rejecting HEAD
        return runCatching {
            val req = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", USER_AGENT)
                .header("Range", "bytes=0-0")
                .build()
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful && resp.code != 206) return@use null
                parseProbeHeaders(resp.request.url.toString(), resp)
            }
        }.getOrNull()
    }

    private fun parseProbeHeaders(finalUrl: String, resp: okhttp3.Response): DirectProbeResult {
        val contentType = resp.header("Content-Type")?.substringBefore(';')?.trim()?.lowercase().orEmpty()
        val contentRangeTotal = resp.header("Content-Range")
            ?.substringAfter('/', "")
            ?.trim()
            ?.toLongOrNull()
        val headerLength = resp.header("Content-Length")?.toLongOrNull() ?: -1L
        val totalLength = when {
            contentRangeTotal != null && contentRangeTotal > 0L -> contentRangeTotal
            resp.code == 206 -> -1L
            else -> headerLength
        }
        val acceptsRanges = resp.code == 206 ||
            resp.header("Accept-Ranges")?.contains("bytes", ignoreCase = true) == true
        val dispositionName = extractDispositionFileName(resp.header("Content-Disposition"))
        val pathLower = finalUrl.substringBefore('?').lowercase()
        val hasMediaExt = MEDIA_EXTENSIONS.any { pathLower.endsWith(".$it") }
        val isMediaMime = contentType.startsWith("video/") || contentType.startsWith("audio/") ||
            (contentType == "application/octet-stream" && hasMediaExt)

        return DirectProbeResult(
            finalUrl = finalUrl,
            contentType = contentType,
            contentLength = totalLength,
            acceptsRanges = acceptsRanges,
            dispositionFileName = dispositionName,
            isDirectMedia = isMediaMime || hasMediaExt
        )
    }

    private fun buildDirectAnalyzedMedia(
        sourceUrl: String,
        platform: PlatformType,
        probe: DirectProbeResult
    ): AnalyzedMedia {
        val rawName = probe.dispositionFileName ?: inferTitleFromUrl(probe.finalUrl)
        val extFromUrl = probe.finalUrl.substringBefore('?').substringAfterLast('.', "").lowercase()
        val format = if (extFromUrl in MEDIA_EXTENSIONS) {
            MediaFormat.fromExtension(extFromUrl)
        } else {
            MediaFormat.fromMime(probe.contentType)
        }

        val isAudio = probe.contentType.startsWith("audio/") ||
            format == MediaFormat.MP3 || format == MediaFormat.M4A || format == MediaFormat.OGG

        val detectedQuality = inferQualityLabel(rawName + " " + probe.finalUrl, isAudio)
        val cleanTitle = rawName
            .replace(Regex("\\.(mp4|webm|mov|mp3|m4a|ogg|aac)$", RegexOption.IGNORE_CASE), "")
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "Media File" }

        val option = MediaOption(
            id = "${if (isAudio) "audio" else "video"}_${format.ext}_source",
            mediaType = if (isAudio) MediaType.AUDIO else MediaType.VIDEO,
            format = format,
            qualityLabel = detectedQuality,
            downloadUrl = probe.finalUrl,
            estimatedBytes = probe.contentLength,
            supportsRange = probe.acceptsRanges,
            isExtraFormat = false
        )

        return AnalyzedMedia(
            sourceUrl = sourceUrl,
            platform = platform,
            title = cleanTitle,
            thumbnailUrl = null,
            durationLabel = null,
            primarySizeBytes = probe.contentLength,
            options = listOf(option),
            isSupportedForDownload = true
        )
    }

    private fun inspectOpenHtmlPage(url: String): AnalyzedMedia? {
        val html = runCatching {
            val req = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", USER_AGENT)
                .build()
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val ct = resp.header("Content-Type").orEmpty()
                if (!ct.contains("text/html", ignoreCase = true)) return@use null
                resp.body?.source()?.let { source ->
                    source.request(MAX_HTML_BYTES)
                    source.buffer.clone().readUtf8(minOf(source.buffer.size, MAX_HTML_BYTES))
                }
            }
        }.getOrNull() ?: return null

        val title = extractMeta(html, "og:title")
            ?: Regex("<title[^>]*>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                .find(html)?.groupValues?.getOrNull(1)?.trim()
            ?: inferTitleFromUrl(url)
        val thumbnail = extractMeta(html, "og:image")

        val candidateUrls = linkedSetOf<String>()
        extractMeta(html, "og:video:secure_url")?.let { candidateUrls.add(it) }
        extractMeta(html, "og:video")?.let { candidateUrls.add(it) }
        extractMeta(html, "og:audio")?.let { candidateUrls.add(it) }

        val sourceRegex = Regex(
            """<(?:video|audio|source)[^>]+src=["'](https?://[^"']+)["']""",
            RegexOption.IGNORE_CASE
        )
        sourceRegex.findAll(html).take(6).forEach { match ->
            candidateUrls.add(match.groupValues[1])
        }

        val verifiedOptions = mutableListOf<MediaOption>()
        var primarySize = -1L

        for ((index, candidate) in candidateUrls.withIndex()) {
            val probed = probeDirectMedia(candidate) ?: continue
            if (!probed.isDirectMedia) continue
            val isAudio = probed.contentType.startsWith("audio/") ||
                candidate.substringBefore('?').lowercase().let { it.endsWith(".mp3") || it.endsWith(".m4a") || it.endsWith(".ogg") }
            val ext = candidate.substringBefore('?').substringAfterLast('.', "").lowercase()
            val format = if (ext in MEDIA_EXTENSIONS) MediaFormat.fromExtension(ext) else MediaFormat.fromMime(probed.contentType)
            if (primarySize < 0L && probed.contentLength > 0L) {
                primarySize = probed.contentLength
            }
            verifiedOptions.add(
                MediaOption(
                    id = "open_${index}_${format.ext}",
                    mediaType = if (isAudio) MediaType.AUDIO else MediaType.VIDEO,
                    format = format,
                    qualityLabel = inferQualityLabel(candidate, isAudio),
                    downloadUrl = probed.finalUrl,
                    estimatedBytes = probed.contentLength,
                    supportsRange = probed.acceptsRanges,
                    isExtraFormat = format == MediaFormat.WEBM || format == MediaFormat.OGG
                )
            )
        }

        if (verifiedOptions.isEmpty()) return null

        return AnalyzedMedia(
            sourceUrl = url,
            platform = PlatformType.DIRECT,
            title = decodeHtmlEntities(title).take(120),
            thumbnailUrl = thumbnail,
            durationLabel = null,
            primarySizeBytes = primarySize,
            options = verifiedOptions,
            isSupportedForDownload = true
        )
    }

    private class RestrictedSocialPlatformHandler(
        override val platform: PlatformType,
        private val oEmbedEndpoint: String? = null
    ) : PlatformHandler {

        override fun matches(host: String): Boolean =
            platform.hosts.any { h -> host == h || host.endsWith(".$h") }

        override suspend fun analyze(url: String, client: OkHttpClient): AnalyzedMedia {
            var title: String? = null
            var thumbnail: String? = null

            // Attempt polite public oEmbed metadata inspection where offered officially
            if (oEmbedEndpoint != null) {
                runCatching {
                    val encoded = URLEncoder.encode(url, "UTF-8")
                    val req = Request.Builder()
                        .url(oEmbedEndpoint + encoded)
                        .header("User-Agent", USER_AGENT)
                        .build()
                    client.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string().orEmpty()
                            val json = JSONObject(body)
                            title = json.optString("title").takeIf { it.isNotBlank() }
                            thumbnail = json.optString("thumbnail_url").takeIf { it.isNotBlank() }
                        }
                    }
                }
            }

            return AnalyzedMedia(
                sourceUrl = url,
                platform = platform,
                title = title ?: "${platform.displayName} Media Link",
                thumbnailUrl = thumbnail,
                durationLabel = null,
                primarySizeBytes = -1L,
                options = emptyList(),
                isSupportedForDownload = false,
                unsupportedReason = "${platform.displayName} does not provide an authorized public download API for third-party apps. SnapTube respects DRM, copyright, and platform terms and will not bypass access restrictions."
            )
        }
    }

    private fun extractMeta(html: String, property: String): String? {
        val pattern = Regex(
            """<meta[^>]+(?:property|name)=["']${Regex.escape(property)}["'][^>]+content=["']([^"']+)["']""",
            RegexOption.IGNORE_CASE
        )
        val reversePattern = Regex(
            """<meta[^>]+content=["']([^"']+)["'][^>]+(?:property|name)=["']${Regex.escape(property)}["']""",
            RegexOption.IGNORE_CASE
        )
        return pattern.find(html)?.groupValues?.getOrNull(1)
            ?: reversePattern.find(html)?.groupValues?.getOrNull(1)
    }

    private fun extractDispositionFileName(header: String?): String? {
        if (header.isNullOrBlank()) return null
        val match = Regex("""filename\*?=(?:UTF-8''|")?([^";]+)""", RegexOption.IGNORE_CASE).find(header)
        val raw = match?.groupValues?.getOrNull(1)?.trim()?.trim('"') ?: return null
        return runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
    }

    private fun inferTitleFromUrl(url: String): String {
        val segment = runCatching {
            val path = URI(url).path.orEmpty().trimEnd('/')
            val last = path.substringAfterLast('/')
            URLDecoder.decode(last, "UTF-8")
        }.getOrDefault("")
        return segment.ifBlank { "SnapTube Media" }
    }

    private fun inferQualityLabel(hint: String, isAudio: Boolean): String {
        val lower = hint.lowercase()
        return if (isAudio) {
            when {
                lower.contains("320") -> "320 kbps"
                lower.contains("256") -> "256 kbps"
                lower.contains("192") -> "192 kbps"
                lower.contains("128") -> "128 kbps"
                lower.contains("96") -> "96 kbps"
                else -> "Original Audio"
            }
        } else {
            when {
                lower.contains("2160") || lower.contains("4k") -> "4K"
                lower.contains("1440") || lower.contains("2k") -> "2K"
                lower.contains("1080") -> "1080p Full HD"
                lower.contains("720") -> "720p HD"
                lower.contains("480") -> "480p"
                lower.contains("360") -> "360p"
                lower.contains("240") -> "240p"
                lower.contains("144") -> "144p"
                else -> "Original Quality"
            }
        }
    }

    private fun decodeHtmlEntities(input: String): String =
        input
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")

    companion object {
        private const val USER_AGENT = "SnapTubeAndroid/1.0 (MediaDownloader)"
        private const val MAX_HTML_BYTES = 256L * 1024L
        private val MEDIA_EXTENSIONS = setOf("mp4", "webm", "mov", "mp3", "m4a", "ogg", "aac")
    }
}
