package xevenition.com.runage.fragment.historysummarypath

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.FirestoreLocation
import xevenition.com.runage.model.SavedQuest


class HistorySummaryPathViewModel(
    arguments: HistorySummaryPathFragmentArgs
) : BaseViewModel() {

    private var mapIsReady: Boolean = false
    private val quest = arguments.keySavedQuest

    private val _observableStartMarker = MutableLiveData<LatLng>()
    val observableStartMarker: LiveData<LatLng> = _observableStartMarker

    private val _observableEndMarker = MutableLiveData<LatLng>()
    val observableEndMarker: LiveData<LatLng> = _observableEndMarker

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    @SuppressLint("CheckResult")
    private fun displayPath(quest: SavedQuest) {
        Observable.just(quest)
            .subscribeOn(Schedulers.computation())
            .map {
                val itemType = object : TypeToken<List<FirestoreLocation>>() {}.type
                Gson().fromJson<List<FirestoreLocation>>(quest.locations, itemType)
            }
            .map {
                if (it.isNotEmpty()) {
                    val firstLocation = it.first()
                    _observableStartMarker.postValue(
                        LatLng(
                            firstLocation.lat,
                            firstLocation.lon
                        )
                    )
                }
                if (it.size > 1) {
                    val lastLocation = it.last()
                    _observableEndMarker.postValue(
                        LatLng(
                            lastLocation.lat,
                            lastLocation.lon
                        )
                    )
                }
                it
            }
            .flatMap { Observable.fromIterable(it) }
            .map { LatLng(it.lat, it.lon) }
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
        displayPath(quest)
    }
}
