package xevenition.com.runage

import android.app.Application
import android.util.TypedValue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Bolling1 on 08/01/15.
 */
@Singleton
class TypedValueUtil @Inject constructor(private val app: Application) {
    fun dipToPixels(dipValue: Float): Int {
        val metrics = app.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics).toInt()
    }

}