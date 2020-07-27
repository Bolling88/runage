package xevenition.com.runage.adapters

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar

@BindingAdapter("app:cpb_progress")
fun setProgress(view: CircularProgressBar, progress: Float) {
    view.progress = progress
}

@BindingAdapter("app:cpb_progress_max")
fun setMax(view: CircularProgressBar, progress: Float) {
    view.progressMax = progress
}

@BindingAdapter("app:tint")
fun setTint(view: FloatingActionButton, tint: Int) {
    view.imageTintList = ColorStateList.valueOf(tint)
}

@BindingAdapter("android:backgroundTint")
fun setBackgroundTint(view: FloatingActionButton, tint: Int) {
    view.backgroundTintList = ColorStateList.valueOf(tint)
}

@BindingAdapter("app:srcCompat")
fun setDrawable(view: AppCompatImageView, drawable: Drawable?) {
    drawable?.let {
        view.setImageDrawable(it)
    }
}