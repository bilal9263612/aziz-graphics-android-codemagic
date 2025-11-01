package com.azizgraphics.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.azizgraphics.FileAdapter
import com.azizgraphics.FileItem
import com.azizgraphics.FileManager
import com.azizgraphics.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var filesGrid: GridView
    private lateinit var emptyState: LinearLayout
    private lateinit var fileList: MutableList<FileItem>
    private lateinit var fileAdapter: FileAdapter

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Simple file name from URI for display name
                val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "New HTML File"
                val newItem = FileManager.embedHtmlFile(this, uri, fileName)
                if (newItem != null) {
                    loadFiles()
                    Snackbar.make(filesGrid, "File embedded successfully: ${newItem.displayName}", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(filesGrid, "Failed to embed file.", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        filesGrid = findViewById(R.id.files_grid)
        emptyState = findViewById(R.id.empty_state)
        val fabAddFile: FloatingActionButton = findViewById(R.id.fab_add_file)

        loadFiles()

        fabAddFile.setOnClickListener {
            openFilePicker()
        }

        filesGrid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val fileItem = fileList[position]
            openHtmlViewer(fileItem)
        }

        // Long press for context menu (rename, delete, pin)
        filesGrid.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val fileItem = fileList[position]
            showContextMenu(fileItem)
            true
        }
    }

    private fun loadFiles() {
        fileList = FileManager.loadFiles(this)
        // Sort: Pinned first, then by name
        fileList.sortWith(compareByDescending<FileItem> { it.isPinned }.thenBy { it.displayName })
        fileAdapter = FileAdapter(this, fileList)
        filesGrid.adapter = fileAdapter

        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (fileList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            filesGrid.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            filesGrid.visibility = View.VISIBLE
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/html" // Filter for HTML files
        }
        filePickerLauncher.launch(intent)
    }

    private fun openHtmlViewer(fileItem: FileItem) {
        val intent = Intent(this, HtmlViewerActivity::class.java).apply {
            putExtra("FILE_ITEM", fileItem)
        }
        startActivity(intent)
    }

    private fun showContextMenu(fileItem: FileItem) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_context_menu)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnRename = dialog.findViewById<View>(R.id.btn_rename)
        val btnPinUnpin = dialog.findViewById<android.widget.Button>(R.id.btn_pin_unpin)
        val btnDelete = dialog.findViewById<View>(R.id.btn_delete)

        btnPinUnpin.text = if (fileItem.isPinned) getString(R.string.unpin_file) else getString(R.string.pin_file)

        btnRename.setOnClickListener {
            dialog.dismiss()
            showRenameDialog(fileItem)
        }

        btnPinUnpin.setOnClickListener {
            dialog.dismiss()
            togglePin(fileItem)
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmation(fileItem)
        }

        dialog.show()
    }

    private fun showRenameDialog(fileItem: FileItem) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_rename)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editTextNewName = dialog.findViewById<android.widget.EditText>(R.id.edit_text_new_name)
        val btnCancel = dialog.findViewById<View>(R.id.btn_cancel)
        val btnConfirm = dialog.findViewById<View>(R.id.btn_confirm)

        editTextNewName.setText(fileItem.displayName)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val newName = editTextNewName.text.toString().trim()
            if (newName.isNotEmpty()) {
                fileItem.displayName = newName
                FileManager.saveFiles(this, fileList)
                loadFiles()
                Snackbar.make(filesGrid, getString(R.string.file_renamed), Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                editTextNewName.error = "Name cannot be empty"
            }
        }

        dialog.show()
    }

    private fun togglePin(fileItem: FileItem) {
        fileItem.isPinned = !fileItem.isPinned
        FileManager.saveFiles(this, fileList)
        loadFiles()
    }

    private fun showDeleteConfirmation(fileItem: FileItem) {
        android.app.AlertDialog.Builder(this, R.style.Theme_AzizGraphics_Dialog)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete this file? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                if (FileManager.deleteFile(this, fileItem)) {
                    loadFiles()
                    Snackbar.make(filesGrid, getString(R.string.file_deleted), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(filesGrid, getString(R.string.error_deleting_file), Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
