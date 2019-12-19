package xevenition.com.runage

import android.app.Application
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import javax.inject.Inject

class ResourceUtil @Inject constructor(private val app: Application) {

    fun getString(id: Int): String {
        return app.resources.getString(id)
    }

    fun getResources(): Resources {
        return app.resources
    }

    fun getDrawable(id: Int): Drawable {
        return AppCompatResources.getDrawable(app, id)!!
    }

    fun getColor(color: Int): Int {
        return ContextCompat.getColor(app, color)
    }

    fun getStringColor(color: Int): String {
        return app.resources.getString(color)
    }

    fun getDimension(dimen: Int): Float {
        return app.resources.getDimension(dimen)
    }

    fun createVectorDrawable(resourceId: Int): Drawable {
        return VectorDrawableCompat.create(app.resources, resourceId, null)!!
    }

    fun createThemedVectorDrawable(theme: Resources.Theme, resourceId: Int): Drawable? {
        theme.applyStyle(resourceId, false)
        return ResourcesCompat.getDrawable(app.resources, resourceId, theme)
    }
}
