package com.azizgraphics

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileManager {
    private const val TAG = "FileManager"
    private const val METADATA_FILE = "file_metadata.json"
    private const val HTML_DIR = "embedded_html"

    private fun getMetadataFile(context: Context): File {
        return File(context.filesDir, METADATA_FILE)
    }

    private fun getHtmlDir(context: Context): File {
        val dir = File(context.filesDir, HTML_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun loadFiles(context: Context): MutableList<FileItem> {
        val metadataFile = getMetadataFile(context)
        if (!metadataFile.exists()) {
            return mutableListOf()
        }
        return try {
            val json = metadataFile.readText()
            val type = object : TypeToken<MutableList<FileItem>>() {}.type
            Gson().fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading file metadata", e)
            mutableListOf()
        }
    }

    fun saveFiles(context: Context, files: List<FileItem>) {
        val metadataFile = getMetadataFile(context)
        try {
            val json = Gson().toJson(files)
            metadataFile.writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving file metadata", e)
        }
    }

    fun embedHtmlFile(context: Context, uri: Uri, displayName: String): FileItem? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val uniqueId = UUID.randomUUID().toString()
            val embeddedFileName = "$uniqueId.html"
            val embeddedFile = File(getHtmlDir(context), embeddedFileName)

            inputStream?.use { input ->
                FileOutputStream(embeddedFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Simple color assignment logic for demonstration (can be expanded)
            val colorOptions = context.resources.getIntArray(R.array.folder_icon_colors)
            val colorIndex = loadFiles(context).size % colorOptions.size
            val iconColor = colorOptions[colorIndex]

            val newItem = FileItem(
                originalPath = embeddedFile.absolutePath, // Store the path to the *embedded* file
                displayName = displayName,
                uniqueId = uniqueId,
                iconColor = iconColor
            )

            val files = loadFiles(context)
            files.add(newItem)
            saveFiles(context, files)
            newItem
        } catch (e: Exception) {
            Log.e(TAG, "Error embedding HTML file", e)
            null
        }
    }

    fun deleteFile(context: Context, fileItem: FileItem): Boolean {
        val files = loadFiles(context)
        val success = files.remove(fileItem)
        if (success) {
            saveFiles(context, files)
            // Also delete the physical embedded file
            val embeddedFile = File(fileItem.originalPath)
            if (embeddedFile.exists()) {
                embeddedFile.delete()
            }
        }
        return success
    }

    fun getEmbeddedFileUri(fileItem: FileItem): Uri {
        return Uri.fromFile(File(fileItem.originalPath))
    }
}
