package jp.keita.kagurazaka.fdsample

import android.graphics.PointF
import com.google.android.gms.vision.face.Face

data class FaceInfo(
        var id: Int,
        var leftTop: PointF,
        var rightBottom: PointF,
        var eulerY: Float,
        var eulerZ: Float
) {
    companion object {
        fun create(face: Face): FaceInfo = FaceInfo(
                face.id,
                PointF(face.position.x, face.position.y),
                PointF(face.position.x + face.width, face.position.y + face.height),
                face.eulerY,
                face.eulerZ
        )
    }
}
