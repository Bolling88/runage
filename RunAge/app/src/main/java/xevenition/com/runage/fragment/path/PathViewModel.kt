package xevenition.com.runage.fragment.path

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.repository.QuestRepository


class PathViewModel(
    private val questRepository: QuestRepository,
    arguments: PathFragmentArgs
) : BaseViewModel() {

    private var mapIsReady: Boolean = false
    private var currentQuest: Quest? = null
    private var questDisposable: Disposable? = null
    private val questId = arguments.keyQuestId

    private val _observableStartMarker = MutableLiveData<LatLng>()
    val observableStartMarker: LiveData<LatLng> = _observableStartMarker

    private val _observableEndMarker = MutableLiveData<LatLng>()
    val observableEndMarker: LiveData<LatLng> = _observableEndMarker

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    init {
        setUpObservableQuest(questId)
    }

    @SuppressLint("CheckResult")
    private fun setUpObservableQuest(id: Int) {
        Timber.d("Register quest flowable with id: $id")
        questRepository.getSingleQuest(id)
            .subscribe({ quest ->
                currentQuest = quest
                if (mapIsReady) {
                    displayPath(quest)
                }
            }, {
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    private fun displayPath(quest: Quest) {
        if (quest.locations.isNotEmpty()) {
            val firstLocation = quest.locations.first()
            _observableStartMarker.postValue(
                LatLng(
                    firstLocation.latitude,
                    firstLocation.longitude
                )
            )
        }
        if (quest.locations.size > 1) {
            val lastLocation = quest.locations.last()
            _observableEndMarker.postValue(
                LatLng(
                    lastLocation.latitude,
                    lastLocation.longitude
                )
            )
        }

        Observable.fromIterable(quest.locations)
            .map { LatLng(it.latitude, it.longitude) }
            .toList()
            .subscribe({
                _observableRunningPath.postValue(it)
            }, {
                Timber.e(it)
            })
    }

    fun onCloseClicked() {
        observableBackNavigation.call()
    }

    fun onMapCreated() {
        mapIsReady = true
        currentQuest?.let {
            displayPath(it)
        }
    }
}
