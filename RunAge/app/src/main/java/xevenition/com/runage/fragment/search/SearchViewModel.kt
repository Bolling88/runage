package xevenition.com.runage.fragment.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.GameServicesUtil
import xevenition.com.runage.util.ResourceUtil

class SearchViewModel(
) : BaseViewModel() {

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    private val _liveEmptyText = MutableLiveData<String>()
    val liveEmptyText: LiveData<String> = _liveEmptyText
}