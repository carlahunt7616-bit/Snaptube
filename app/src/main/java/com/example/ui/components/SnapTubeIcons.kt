package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lightweight inline Material-style vector icons to eliminate the heavy
 * material-icons-extended artifact and keep SnapTube's APK ultra-compact.
 */
object SnapIcons {

    val Download: ImageVector by lazy {
        icon("Download") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 20f); horizontalLineTo(19f); verticalLineTo(18f); horizontalLineTo(5f); close()
                moveTo(19f, 9f); horizontalLineTo(15f); verticalLineTo(3f); horizontalLineTo(9f)
                verticalLineTo(9f); horizontalLineTo(5f); lineTo(12f, 16f); lineTo(19f, 9f); close()
            }
        }
    }

    val Pause: ImageVector by lazy {
        icon("Pause") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 19f); horizontalLineTo(10f); verticalLineTo(5f); horizontalLineTo(6f); close()
                moveTo(14f, 5f); verticalLineTo(19f); horizontalLineTo(18f); verticalLineTo(5f); close()
            }
        }
    }

    val VideoFile: ImageVector by lazy {
        icon("VideoFile") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 6f)
                curveTo(2.9f, 6f, 2f, 6.9f, 2f, 8f)
                verticalLineTo(16f)
                curveTo(2f, 17.1f, 2.9f, 18f, 4f, 18f)
                horizontalLineTo(16f)
                curveTo(17.1f, 18f, 18f, 17.1f, 18f, 16f)
                verticalLineTo(13.5f)
                lineTo(22f, 17f)
                verticalLineTo(7f)
                lineTo(18f, 10.5f)
                verticalLineTo(8f)
                curveTo(18f, 6.9f, 17.1f, 6f, 16f, 6f)
                close()
            }
        }
    }

    val MusicNote: ImageVector by lazy {
        icon("MusicNote") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                verticalLineTo(13.55f)
                curveTo(11.41f, 13.21f, 10.73f, 13f, 10f, 13f)
                curveTo(7.79f, 13f, 6f, 14.79f, 6f, 17f)
                reflectiveCurveTo(7.79f, 21f, 10f, 21f)
                reflectiveCurveTo(14f, 19.21f, 14f, 17f)
                verticalLineTo(7f)
                horizontalLineTo(18f)
                verticalLineTo(3f)
                close()
            }
        }
    }

    val Link: ImageVector by lazy {
        icon("Link") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10f, 13f)
                arcToRelative(5f, 5f, 0f, false, false, 7.54f, 0.54f)
                lineToRelative(3f, -3f)
                arcToRelative(5f, 5f, 0f, false, false, -7.07f, -7.07f)
                lineToRelative(-1.72f, 1.71f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14f, 11f)
                arcToRelative(5f, 5f, 0f, false, false, -7.54f, -0.54f)
                lineToRelative(-3f, 3f)
                arcToRelative(5f, 5f, 0f, false, false, 7.07f, 7.07f)
                lineToRelative(1.71f, -1.71f)
            }
        }
    }

    val ContentPaste: ImageVector by lazy {
        icon("ContentPaste") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 2f)
                horizontalLineTo(14.82f)
                curveTo(14.4f, 0.84f, 13.3f, 0f, 12f, 0f)
                reflectiveCurveTo(9.6f, 0.84f, 9.18f, 2f)
                horizontalLineTo(5f)
                curveTo(3.9f, 2f, 3f, 2.9f, 3f, 4f)
                verticalLineTo(20f)
                curveTo(3f, 21.1f, 3.9f, 22f, 5f, 22f)
                horizontalLineTo(19f)
                curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
                verticalLineTo(4f)
                curveTo(21f, 2.9f, 20.1f, 2f, 19f, 2f)
                close()
                moveTo(12f, 2f)
                curveTo(12.55f, 2f, 13f, 2.45f, 13f, 3f)
                reflectiveCurveTo(12.55f, 4f, 12f, 4f)
                reflectiveCurveTo(11f, 3.55f, 11f, 3f)
                reflectiveCurveTo(11.45f, 2f, 12f, 2f)
                close()
                moveTo(19f, 20f)
                horizontalLineTo(5f)
                verticalLineTo(4f)
                horizontalLineTo(7f)
                verticalLineTo(7f)
                horizontalLineTo(17f)
                verticalLineTo(4f)
                horizontalLineTo(19f)
                close()
            }
        }
    }

    val Folder: ImageVector by lazy {
        icon("Folder") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
                verticalLineTo(18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                horizontalLineTo(20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                verticalLineTo(8f)
                curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f)
                horizontalLineTo(12f)
                close()
            }
        }
    }

    val OpenInNew: ImageVector by lazy {
        icon("OpenInNew") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 19f); horizontalLineTo(5f); verticalLineTo(5f); horizontalLineTo(12f)
                verticalLineTo(3f); horizontalLineTo(5f); curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f); curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
                horizontalLineTo(19f); curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(12f); horizontalLineTo(19f); close()
                moveTo(14f, 3f); verticalLineTo(5f); horizontalLineTo(17.59f)
                lineTo(7.76f, 14.83f); lineTo(9.17f, 16.24f); lineTo(19f, 6.41f)
                verticalLineTo(10f); horizontalLineTo(21f); verticalLineTo(3f); close()
            }
        }
    }

    val ShieldCheck: ImageVector by lazy {
        icon("ShieldCheck") {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                moveTo(12f, 2f)
                lineTo(4f, 5f)
                verticalLineTo(11.09f)
                curveTo(4f, 16.14f, 7.41f, 20.85f, 12f, 22f)
                curveTo(16.59f, 20.85f, 20f, 16.14f, 20f, 11.09f)
                verticalLineTo(5f)
                close()
                moveTo(10.5f, 15.5f)
                lineTo(7.5f, 12.5f)
                lineTo(8.91f, 11.09f)
                lineTo(10.5f, 12.67f)
                lineTo(15.09f, 8.09f)
                lineTo(16.5f, 9.5f)
                close()
            }
        }
    }

    val Wifi: ImageVector by lazy {
        icon("Wifi") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(5f, 12.55f); arcToRelative(11f, 11f, 0f, false, true, 14.08f, 0f)
                moveTo(1.42f, 9f); arcToRelative(16f, 16f, 0f, false, true, 21.16f, 0f)
                moveTo(8.53f, 16.11f); arcToRelative(6f, 6f, 0f, false, true, 6.95f, 0f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 20f)
                arcToRelative(1.5f, 1.5f, 0f, true, true, 0.01f, 0f)
            }
        }
    }

    val Globe: ImageVector by lazy {
        icon("Globe") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 2f)
                arcToRelative(10f, 10f, 0f, true, false, 0.01f, 0f)
                moveTo(2f, 12f); horizontalLineTo(22f)
                moveTo(12f, 2f); curveTo(14.5f, 5f, 15.8f, 8.4f, 16f, 12f)
                curveTo(15.8f, 15.6f, 14.5f, 19f, 12f, 22f)
                moveTo(12f, 2f); curveTo(9.5f, 5f, 8.2f, 8.4f, 8f, 12f)
                curveTo(8.2f, 15.6f, 9.5f, 19f, 12f, 22f)
            }
        }
    }

    val Palette: ImageVector by lazy {
        icon("Palette") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                reflectiveCurveTo(7.03f, 21f, 12f, 21f)
                curveTo(12.83f, 21f, 13.5f, 20.33f, 13.5f, 19.5f)
                curveTo(13.5f, 19.11f, 13.35f, 18.76f, 13.11f, 18.49f)
                curveTo(12.88f, 18.23f, 12.73f, 17.88f, 12.73f, 17.5f)
                curveTo(12.73f, 16.67f, 13.4f, 16f, 14.23f, 16f)
                horizontalLineTo(16f)
                curveTo(18.76f, 16f, 21f, 13.76f, 21f, 11f)
                curveTo(21f, 6.58f, 16.97f, 3f, 12f, 3f)
                close()
            }
        }
    }

    val Storage: ImageVector by lazy {
        icon("Storage") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(2f, 20f); horizontalLineTo(22f); verticalLineTo(16f); horizontalLineTo(2f); close()
                moveTo(4f, 17f); horizontalLineTo(6f); verticalLineTo(19f); horizontalLineTo(4f); close()
                moveTo(2f, 4f); verticalLineTo(8f); horizontalLineTo(22f); verticalLineTo(4f); close()
                moveTo(6f, 7f); horizontalLineTo(4f); verticalLineTo(5f); horizontalLineTo(6f); close()
                moveTo(2f, 14f); horizontalLineTo(22f); verticalLineTo(10f); horizontalLineTo(2f); close()
                moveTo(4f, 11f); horizontalLineTo(6f); verticalLineTo(13f); horizontalLineTo(4f); close()
            }
        }
    }

    val Speed: ImageVector by lazy {
        icon("Speed") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 14f); lineTo(16.5f, 9.5f)
                moveTo(4.2f, 17f)
                arcToRelative(9f, 9f, 0f, true, true, 15.6f, 0f)
            }
        }
    }

    val Sort: ImageVector by lazy {
        icon("Sort") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 18f); horizontalLineTo(9f); verticalLineTo(16f); horizontalLineTo(3f); close()
                moveTo(3f, 6f); verticalLineTo(8f); horizontalLineTo(21f); verticalLineTo(6f); close()
                moveTo(3f, 13f); horizontalLineTo(15f); verticalLineTo(11f); horizontalLineTo(3f); close()
            }
        }
    }

    val Library: ImageVector by lazy {
        icon("Library") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 6f); horizontalLineTo(2f); verticalLineTo(20f)
                curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f); horizontalLineTo(18f); verticalLineTo(20f)
                horizontalLineTo(4f); close()
                moveTo(20f, 2f); horizontalLineTo(8f); curveTo(6.9f, 2f, 6f, 2.9f, 6f, 4f)
                verticalLineTo(16f); curveTo(6f, 17.1f, 6.9f, 18f, 8f, 18f)
                horizontalLineTo(20f); curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
                verticalLineTo(4f); curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f); close()
                moveTo(12f, 14.5f); verticalLineTo(5.5f); lineTo(18f, 10f); close()
            }
        }
    }

    val Replay10: ImageVector by lazy {
        icon("Replay10") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 5f)
                arcToRelative(7f, 7f, 0f, true, true, -6.5f, 4.4f)
                moveTo(12f, 5f); lineTo(9f, 2f)
                moveTo(12f, 5f); lineTo(9f, 8f)
            }
        }
    }

    val Forward10: ImageVector by lazy {
        icon("Forward10") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 5f)
                arcToRelative(7f, 7f, 0f, true, false, 6.5f, 4.4f)
                moveTo(12f, 5f); lineTo(15f, 2f)
                moveTo(12f, 5f); lineTo(15f, 8f)
            }
        }
    }

    val Repeat: ImageVector by lazy {
        icon("Repeat") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17f, 2f); lineTo(21f, 6f); lineTo(17f, 10f)
                moveTo(3f, 11f); verticalLineTo(9f); arcToRelative(3f, 3f, 0f, false, true, 3f, -3f); horizontalLineTo(21f)
                moveTo(7f, 22f); lineTo(3f, 18f); lineTo(7f, 14f)
                moveTo(21f, 13f); verticalLineTo(15f); arcToRelative(3f, 3f, 0f, false, true, -3f, 3f); horizontalLineTo(3f)
            }
        }
    }

    val VolumeUp: ImageVector by lazy {
        icon("VolumeUp") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 9f); verticalLineTo(15f); horizontalLineTo(7f); lineTo(12f, 20f); verticalLineTo(4f); lineTo(7f, 9f); close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(15.5f, 8.5f); arcToRelative(5f, 5f, 0f, false, true, 0f, 7f)
                moveTo(18.5f, 5.5f); arcToRelative(9f, 9f, 0f, false, true, 0f, 13f)
            }
        }
    }

    val VolumeOff: ImageVector by lazy {
        icon("VolumeOff") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 9f); verticalLineTo(15f); horizontalLineTo(7f); lineTo(12f, 20f); verticalLineTo(4f); lineTo(7f, 9f); close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(16f, 9f); lineTo(22f, 15f)
                moveTo(22f, 9f); lineTo(16f, 15f)
            }
        }
    }

    val SystemUpdate: ImageVector by lazy {
        icon("SystemUpdate") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f); verticalLineTo(15f)
                moveTo(7f, 10f); lineTo(12f, 15f); lineTo(17f, 10f)
                moveTo(5f, 20f); horizontalLineTo(19f)
            }
        }
    }

    val Support: ImageVector by lazy {
        icon("Support") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2f); arcToRelative(10f, 10f, 0f, true, false, 0.01f, 0f)
                moveTo(9.1f, 9f); arcToRelative(3f, 3f, 0f, false, true, 5.8f, 1f)
                curveTo(14.9f, 12f, 12f, 12.5f, 12f, 14f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 17.5f); arcToRelative(1.2f, 1.2f, 0f, true, true, 0.01f, 0f)
            }
        }
    }

    private inline fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()
}
