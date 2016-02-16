package jp.keita.kagurazaka.fdsample

import android.graphics.PointF
import com.google.android.gms.vision.face.Face

data class FaceInfo(
        var leftTop: PointF,
        var rightBottom: PointF
) {
    companion object {
        fun create(face: Face): FaceInfo = FaceInfo(
                PointF(face.position.x, face.position.y),
                PointF(face.position.x + face.width, face.position.y + face.height)
        )
    }
}
