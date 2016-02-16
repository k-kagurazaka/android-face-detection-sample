package jp.keita.kagurazaka.fdsample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

object Utils {

    private val IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "gif", "png")

    fun isImageFile(file: File): Boolean = IMAGE_EXTENSIONS.contains(file.extension)

    fun decodeWithResize(file: File, width: Int, height: Int): Bitmap {
        // When zero is specified for the width or the height, decode without resizing.
        if (width == 0 || height == 0) {
            return BitmapFactory.decodeFile(file.absolutePath)
        }

        // Obtain the specified image size.
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val tmp: Bitmap? = BitmapFactory.decodeFile(file.absolutePath, options)
        tmp?.recycle()

        // Calculate resize scale.
        val scaleWidth = options.outWidth.toFloat() / width.toFloat()
        val scaleHeight = options.outHeight.toFloat() / height.toFloat()
        val scale = Math.max(scaleWidth, scaleHeight)

        // Decode the image with resizing.
        return if (scale > 1) {
            val resizeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale.toInt()
            }
            BitmapFactory.decodeFile(file.absolutePath, resizeOptions)
        } else {
            BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    fun writeTextFile(file: File, content: String) {
        val writer = PrintWriter(BufferedWriter(FileWriter(file)))
        writer.write(content)
        writer.flush()
        writer.close()
    }
}
