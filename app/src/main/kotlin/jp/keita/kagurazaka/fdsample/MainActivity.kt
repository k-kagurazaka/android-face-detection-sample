package jp.keita.kagurazaka.fdsample

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.jakewharton.rxbinding.widget.RxTextView
import com.nononsenseapps.filepicker.FilePickerActivity
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileFilter

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

    private lateinit var detector: FaceDetectorWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        detector = FaceDetectorWrapper(applicationContext)
        initializeUI()
    }

    override fun onDestroy() {
        finalizeUI()
        detector.release()
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

        // FloatingActionButton settings
        fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            fab.visibility = View.GONE

            val faceDetectionStream = detectFace(inputDirText.text.toString())
                    .subscribeOn(Schedulers.newThread())

            subscribeOnMainThread(faceDetectionStream) {
                val gson = GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()
                val json = gson.toJson(it)
                Utils.writeTextFile(File(outputDirText.text.toString(), "result.json"), json)

                fab.visibility = View.VISIBLE
            }
        }

        onRxErrorListener = { Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show() }

        // Observe Rx streams.
        val isDirSpecified = Observable.combineLatest(
                RxTextView.textChanges(inputDirText),
                RxTextView.textChanges(outputDirText),
                detector.isReady) { input, output, ready ->
            input.isNotBlank() && output.isNotBlank() && ready
        }
        subscribeOnMainThread(isDirSpecified) {
            fab.visibility = if (it) View.VISIBLE else View.GONE
        }
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

    private fun detectFace(path: String) = createPromise {
        val dir = File(path)
        if (!dir.exists()) return@createPromise arrayListOf<ImageInfo>()

        val result = arrayListOf<ImageInfo>()

        val targets = dir.listFiles(FileFilter { Utils.isImageFile(it) })
        for (image in targets) {
            val faces = detector.detectFaces(image) { info, bmp ->
                // TODO
            }
            val imageInfo = ImageInfo(image.absolutePath, faces)
            result.add(imageInfo)
        }

        return@createPromise result
    }

}
