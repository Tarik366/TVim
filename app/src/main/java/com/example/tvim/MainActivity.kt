package com.example.tvim

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val buttonS = R.id.button3
    private lateinit var aView:View
    private lateinit var fileName:TextView
    private lateinit var text:MultiAutoCompleteTextView
    private var newFileContentText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        actionBar?.hide()
        setContentView(R.layout.activity_main)

        val firstClose = findViewById<FloatingActionButton>(R.id.closeF)
        firstClose.setOnClickListener {
            closeFile(firstClose)
        }

        val secondClose = findViewById<FloatingActionButton>(R.id.closeS)
        secondClose.setOnClickListener {
            closeFile(secondClose)
        }

        val firstSave = findViewById<FloatingActionButton>(R.id.saveF)
        firstSave.setOnClickListener {
            saveFile(firstSave)
        }

        val secondSave = findViewById<FloatingActionButton>(R.id.saveS)
        secondSave.setOnClickListener {
            saveFile(secondSave)
        }

        val firstLock = findViewById<FloatingActionButton>(R.id.lockF)
        firstLock.setOnClickListener {
            lockFile(firstLock)
        }

        val secondLock = findViewById<FloatingActionButton>(R.id.lockS)
        secondLock.setOnClickListener {
            lockFile(secondLock)
        }

    }

    private fun lockFile(lockView: View) {
        val textView = when(lockView.id == R.id.lockF) {
            true -> {findViewById<MultiAutoCompleteTextView>(R.id.mACTF)}
            false -> {findViewById<MultiAutoCompleteTextView>(R.id.mACTS)}
        }

        when (textView.isFocusable){
            true -> {
                val draw = findViewById<FloatingActionButton>(lockView.id)
                draw.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.baseline_lock_outline_24))
                textView.isFocusable = false
                textView.isFocusableInTouchMode = false
            }
            false -> {
                val draw = findViewById<FloatingActionButton>(lockView.id)
                draw.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.baseline_lock_open_24))
                textView.isFocusable = true
                textView.isFocusableInTouchMode = true
            }
        }

    }

    fun openFile(view: View) {
        // Example for checking permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
        aView = view
        showFilePicker()

    }

    // Request code for file select
    private val fileSelectCode = 0
    private val fileWriteCode = 1

    private fun showFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // All file types
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), fileSelectCode)
        } catch (ex: android.content.ActivityNotFoundException) {
            // If no file manager app is found
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == fileSelectCode && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                readTextFromUri(uri)
            }
        }; else if(requestCode == fileWriteCode && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                File().writeFile(uri, newFileContentText, this)
            }
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri) {
        val card: LinearLayout

        when(aView.id == buttonS) {
            false -> {
                card = findViewById(R.id.editorHorz)
                text = findViewById(R.id.mACTF)
                fileName = findViewById(R.id.fileNameFirst)
            }
            true -> {
                card = findViewById(R.id.editorVert)
                text = findViewById(R.id.mACTS)
                fileName = findViewById(R.id.fileNameSecond)
            }
        }

        card.visibility = View.VISIBLE
        aView.visibility = View.GONE

        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                text.setText(reader.readText())
                text.hint = uri.toString()

            }
        }
        fileName.text = uri.lastPathSegment.toString().substring(uri.lastPathSegment.toString().indexOf(":")+1)
    }

    private fun saveFile(view: View) {
        val textBoxForClose:MultiAutoCompleteTextView = when(view.id == R.id.closeF) {
            true -> {
                findViewById<MultiAutoCompleteTextView>(R.id.mACTF)
            }

            false -> {
                findViewById<MultiAutoCompleteTextView>(R.id.mACTS)
            }
        }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")
        newFileContentText = textBoxForClose.text.toString()
        startActivityForResult(intent, fileWriteCode)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun closeFile(view: View) {
        val card: LinearLayout
        val bu: Button
        when(view.id == R.id.closeF) {
            true -> {
                card = findViewById<LinearLayout>(R.id.editorHorz)
                bu = findViewById<Button>(R.id.button)
            }
            false -> {
                card = findViewById<LinearLayout>(R.id.editorVert)
                bu = findViewById<Button>(buttonS)
            }
        }
        card.visibility = View.GONE
        bu.visibility = View.VISIBLE
    }
}