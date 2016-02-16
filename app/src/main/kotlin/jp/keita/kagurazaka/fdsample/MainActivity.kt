package jp.keita.kagurazaka.fdsample

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.nononsenseapps.filepicker.FilePickerActivity
import java.io.File

class MainActivity : BaseActivity() {

    enum class RequestCode(val code: Int) {
        SELECT_INPUT_DIR(1001),
        SELECT_OUTPUT_DIR(1002)
    }

    private lateinit var inputDirText: EditText
    private lateinit var outputDirText: EditText
    private lateinit var selectInputDirButton: Button
    private lateinit var selectOutputDirButton: Button
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUI()
    }

    override fun onDestroy() {
        finalizeUI()
        super.onDestroy()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return

        val path = (data?.data?.toString() ?: "").removePrefix("file://")
        when (requestCode) {
            RequestCode.SELECT_INPUT_DIR.code -> {
                inputDirText.setText(path)
            }
            RequestCode.SELECT_OUTPUT_DIR.code -> {
                outputDirText.setText(path)
            }
            else -> throw IllegalStateException()
        }
    }

    private fun initializeUI() {
        // Toolbar settings
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Input box settings
        inputDirText = findViewById(R.id.input_dir_text) as EditText
        outputDirText = findViewById(R.id.output_dir_text) as EditText

        // Directory chooser button settings
        val externalDir = Environment.getExternalStorageDirectory().absolutePath
        selectInputDirButton = findViewById(R.id.select_input_dir_button) as Button
        selectInputDirButton.setOnClickListener {
            val inputDir = inputDirText.text.toString()
            val intent = createDirectoryChooser(if (File(inputDir).exists()) inputDir else externalDir)
            startActivityForResult(intent, RequestCode.SELECT_INPUT_DIR.code)
        }

        selectOutputDirButton = findViewById(R.id.select_output_dir_button) as Button
        selectOutputDirButton.setOnClickListener {
            val outputDir = outputDirText.text.toString()
            val intent = createDirectoryChooser(if (File(outputDir).exists()) outputDir else externalDir)
            startActivityForResult(intent, RequestCode.SELECT_OUTPUT_DIR.code)
        }

        fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show() }

        onRxErrorListener = { Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show() }
    }

    private fun finalizeUI() {
        // Release captured activity instance from button click callbacks.
        selectInputDirButton.setOnClickListener(null)
        selectOutputDirButton.setOnClickListener(null)
        fab.setOnClickListener(null)
        onRxErrorListener = null
    }

    private fun createDirectoryChooser(initialDir: String): Intent =
            Intent(applicationContext, FilePickerActivity::class.java).apply {
                putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR)
                putExtra(FilePickerActivity.EXTRA_START_PATH, initialDir)
            }

}
