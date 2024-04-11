package com.example.lab3_mso

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private val FILE_NAME = "example.txt"
    private val REQUEST_CODE_PERMISSIONS = 101

    private val storagePermissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                // Permission has been granted.
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
                // You can perform operations requiring storage permission here
            } else {
                // Permission not granted
                Toast.makeText(this, "Storage permission not granted", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        checkStoragePermission()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonOpen = findViewById<Button>(R.id.button1)
        val buttonSave = findViewById<Button>(R.id.button2)

        buttonOpen.setOnClickListener {
            pickFile()
        }

        buttonSave.setOnClickListener {
            saveFile(findViewById<EditText>(R.id.fileName).text.toString(), findViewById<EditText>(R.id.TextEditor).text.toString())
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            storagePermissionResultLauncher.launch(intent)
        }
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    /*private fun readFile() {
        val file = File(getExternalFilesDir(null), FILE_NAME)
        if (file.exists()) {
            val text = file.readText()
            findViewById<EditText>(R.id.TextEditor).text = file.readText().toEditable()
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun saveFile(fileName: String, text: String) {
        val downloadDirPath = File(Environment.getExternalStorageDirectory(), "Download")
        val file = File(downloadDirPath, fileName + ".txt")
        val fos = FileOutputStream(file)
        fos.write(text.toByteArray())
        fos.close()
        Toast.makeText(this, "File saved to private directory", Toast.LENGTH_SHORT).show()
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        filePickerLauncher.launch(intent)
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                inputStream?.bufferedReader()?.useLines { lines ->
                    val stringBuilder = StringBuilder()
                    lines.forEach {
                        stringBuilder.append(it).append("\n")
                    }
                    findViewById<EditText>(R.id.TextEditor).text = stringBuilder.toString().toEditable()
                }
            }
        }
    }
}