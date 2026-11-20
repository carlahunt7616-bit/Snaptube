package com.example.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MediaStorageManager(private val context: Context) {

    fun getPartialDir(): File {
        val dir = File(context.filesDir, "downloads_part")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPartialFile(taskId: Long): File = File(getPartialDir(), "task_$taskId.part")

    suspend fun saveCompletedFile(
        sourcePartFile: File,
        fileName: String,
        format: MediaFormat,
        safTreeUriString: String?
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Prefer user-selected Storage Access Framework folder if persisted permission is valid
            if (!safTreeUriString.isNullOrBlank()) {
                val safResult = runCatching {
                    saveToSafTree(sourcePartFile, fileName, format.mimeType, Uri.parse(safTreeUriString))
                }.getOrNull()
                if (safResult != null) {
                    sourcePartFile.delete()
                    return@runCatching safResult
                }
            }

            // 2. Android 10+ (API 29+): Save directly to public Downloads/SnapTube via MediaStore (zero permission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, format.mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/SnapTube")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("Could not create MediaStore entry")

                resolver.openOutputStream(itemUri)?.use { out ->
                    FileInputStream(sourcePartFile).use { input ->
                        input.copyTo(out, bufferSize = 32 * 1024)
                    }
                } ?: error("Could not open MediaStore output stream")

                val publishValues = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                resolver.update(itemUri, publishValues, null, null)
                sourcePartFile.delete()
                return@runCatching Pair(itemUri.toString(), fileName)
            }

            // 3. API 24-28 fallback: Save in app external Downloads/SnapTube folder & expose via FileProvider
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: File(context.filesDir, "downloads")
            val snapDir = File(baseDir, "SnapTube")
            if (!snapDir.exists()) snapDir.mkdirs()

            var targetFile = File(snapDir, fileName)
            var suffix = 1
            val baseName = fileName.substringBeforeLast('.')
            val ext = fileName.substringAfterLast('.', format.ext)
            while (targetFile.exists()) {
                targetFile = File(snapDir, "${baseName}_($suffix).$ext")
                suffix++
            }

            FileInputStream(sourcePartFile).use { input ->
                FileOutputStream(targetFile).use { out ->
                    input.copyTo(out, bufferSize = 32 * 1024)
                }
            }
            sourcePartFile.delete()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )
            Pair(contentUri.toString(), targetFile.name)
        }
    }

    private fun saveToSafTree(
        sourcePartFile: File,
        fileName: String,
        mimeType: String,
        treeUri: Uri
    ): Pair<String, String>? {
        val resolver = context.contentResolver
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
        val createdUri = DocumentsContract.createDocument(resolver, parentDocUri, mimeType, fileName)
            ?: return null

        resolver.openOutputStream(createdUri)?.use { out ->
            FileInputStream(sourcePartFile).use { input ->
                input.copyTo(out, bufferSize = 32 * 1024)
            }
        } ?: return null

        return Pair(createdUri.toString(), fileName)
    }

    fun buildOpenExternalIntent(uriString: String, mimeType: String): Intent {
        val uri = Uri.parse(uriString)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun buildShareFileIntent(uriString: String, mimeType: String, title: String): Intent {
        val uri = Uri.parse(uriString)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    suspend fun renameFile(uriString: String?, newFileName: String): String? = withContext(Dispatchers.IO) {
        if (uriString.isNullOrBlank()) return@withContext null
        val uri = Uri.parse(uriString)
        runCatching {
            when {
                DocumentsContract.isDocumentUri(context, uri) -> {
                    DocumentsContract.renameDocument(context.contentResolver, uri, newFileName)?.toString() ?: uriString
                }
                uri.scheme == "content" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
                    }
                    context.contentResolver.update(uri, values, null, null)
                    uriString
                }
                else -> uriString
            }
        }.getOrDefault(uriString)
    }

    suspend fun deleteFile(uriString: String?): Boolean = withContext(Dispatchers.IO) {
        if (uriString.isNullOrBlank()) return@withContext false
        val uri = Uri.parse(uriString)
        runCatching {
            when {
                DocumentsContract.isDocumentUri(context, uri) -> {
                    DocumentsContract.deleteDocument(context.contentResolver, uri)
                }
                uri.scheme == "content" -> {
                    context.contentResolver.delete(uri, null, null) > 0
                }
                else -> {
                    File(uri.path.orEmpty()).delete()
                }
            }
        }.getOrDefault(false)
    }

    fun resolveSafFolderLabel(treeUri: Uri): String {
        val raw = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull().orEmpty()
        return raw.substringAfter(':', raw).ifBlank { treeUri.lastPathSegment ?: "Selected Folder" }
    }
}
