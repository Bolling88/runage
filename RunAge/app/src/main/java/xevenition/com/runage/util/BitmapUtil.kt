package xevenition.com.runage.util

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import javax.inject.Inject


class BitmapUtil @Inject constructor(private val app: Application) {

    fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        val drawable =
            ContextCompat.getDrawable(app, drawableId)
        return if(drawable == null)
            null
        else {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}