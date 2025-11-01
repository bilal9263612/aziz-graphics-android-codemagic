package com.azizgraphics

import java.io.Serializable

data class FileItem(
    val originalPath: String, // The path where the file was originally located
    var displayName: String,  // The user-editable name
    var isPinned: Boolean = false,
    val uniqueId: String,     // A unique ID for the file, used for internal storage
    var iconColor: Int        // A color for the folder icon
) : Serializable
