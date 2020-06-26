package xevenition.com.runage.adapters

import androidx.databinding.BindingAdapter
import com.mikhaellopez.circularprogressbar.CircularProgressBar

@BindingAdapter("app:cpb_progress")
fun setProgress(view: CircularProgressBar, progress: Float) {
    view.progress = progress
}

@BindingAdapter("app:cpb_progress_max")
fun setMax(view: CircularProgressBar, progress: Float) {
    view.progressMax = progress
}