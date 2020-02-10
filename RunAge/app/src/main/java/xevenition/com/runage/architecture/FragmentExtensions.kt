package xevenition.com.runage.architecture

import androidx.fragment.app.Fragment
import xevenition.com.runage.MainApplication

fun Fragment.getApplication(): MainApplication {
    return activity?.application as MainApplication
}