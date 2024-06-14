package com.example.tvim

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class File {

    fun writeFile(uri: Uri, text: String, applicationContext: Context) {
        val contentResolver = applicationContext.contentResolver
        contentResolver.takePersistableUriPermission(
            uri, // The URI of the file
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION // The permissions to grant
        )
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { out ->
                    out.write((text).toByteArray()
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}