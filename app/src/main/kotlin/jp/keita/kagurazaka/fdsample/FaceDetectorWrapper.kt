package jp.keita.kagurazaka.fdsample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.SparseArray
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.FaceDetector
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

internal class SparseArrayIterator<T>(private val array: SparseArray<T>) {

    private var current = 0

    operator fun next(): T = array.valueAt(current++)

    operator fun hasNext(): Boolean = current < array.size()
}

internal operator fun <T> SparseArray<T>.iterator() = SparseArrayIterator(this)

class FaceDetectorWrapper(private val context: Context) {

    private val detector: FaceDetector

    val isReady: Observable<Boolean>

    init {
        detector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setTrackingEnabled(false)
                .setProminentFaceOnly(false)
                .build()

        isReady = Observable.timer(500, TimeUnit.MILLISECONDS, Schedulers.newThread())
                .map { detector.isOperational }
                .repeat()
    }

    fun detectFaces(image: File, callback: ((FaceInfo, Bitmap?) -> Unit)? = null): List<FaceInfo> {
        val result = arrayListOf<FaceInfo>()

        // Construct frame from the specified image file.
        var bitmap: Bitmap? = BitmapFactory.decodeFile(image.absolutePath)
        val frame = Frame.Builder().setBitmap(bitmap).build()

        // Detect faces.
        val faces = detector.detect(frame)
        for (face in faces) {
            val info = FaceInfo.create(face)
            result.add(info)
            callback?.invoke(info, bitmap)
        }

        // Explicitly release the image.
        bitmap?.recycle()
        bitmap = null

        return result
    }

    fun release() {
        detector.release()
    }
}
