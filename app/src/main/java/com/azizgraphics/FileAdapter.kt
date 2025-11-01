package com.azizgraphics

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class FileAdapter(private val context: Context, private val fileList: List<FileItem>) : BaseAdapter() {

    override fun getCount(): Int = fileList.size

    override fun getItem(position: Int): Any = fileList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val fileItem = fileList[position]

        holder.fileName.text = fileItem.displayName
        
        // The folder icon is a template, we will use the color to tint the pin icon
        // and potentially the text, but the main icon is a drawable resource.
        // For the glassy effect, we rely on the pre-generated drawable.
        
        // Set pin indicator visibility and color
        if (fileItem.isPinned) {
            holder.pinIndicator.visibility = View.VISIBLE
            holder.pinIndicator.setColorFilter(fileItem.iconColor)
        } else {
            holder.pinIndicator.visibility = View.GONE
        }

        return view
    }

    private class ViewHolder(view: View) {
        val fileIcon: ImageView = view.findViewById(R.id.file_icon)
        val fileName: TextView = view.findViewById(R.id.file_name)
        val pinIndicator: ImageView = view.findViewById(R.id.pin_indicator)
    }
}
