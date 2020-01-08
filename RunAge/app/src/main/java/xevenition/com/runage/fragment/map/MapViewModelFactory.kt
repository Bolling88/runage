package xevenition.com.runage.fragment.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.ResourceUtil
import javax.inject.Inject

class MapViewModelFactory @Inject constructor(private val resourceUtil: ResourceUtil) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(resourceUtil) as T
    }
}