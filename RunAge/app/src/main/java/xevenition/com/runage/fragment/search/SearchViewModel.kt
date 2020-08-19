package xevenition.com.runage.fragment.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.architecture.BaseViewModel

class SearchViewModel : BaseViewModel() {

    private val _liveProgressVisibility = MutableLiveData<Int>()
    val liveProgressVisibility: LiveData<Int> = _liveProgressVisibility

    private val _liveNoRunsTextVisibility = MutableLiveData<Int>()
    val liveNoRunsTextVisibility: LiveData<Int> = _liveNoRunsTextVisibility

    private val _liveEmptyText = MutableLiveData<String>()
    val liveEmptyText: LiveData<String> = _liveEmptyText
}