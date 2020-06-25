package xevenition.com.runage.fragment.summary

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.service.FitnessHelper
import xevenition.com.runage.util.*
import java.time.Instant

class SummaryViewModel(
    private val fitnessHelper: FitnessHelper,
    private val locationUtil: LocationUtil,
    private val feedbackHandler: FeedbackHandler,
    private val questRepository: QuestRepository,
    private val resourceUtil: ResourceUtil,
    private val fireStoreHandler: FireStoreHandler,
    arguments: SummaryFragmentArgs
) : BaseViewModel() {
    private var mapCreated: Boolean = false
    private var quest: Quest? = null
    private var percentageMap: Map<Int, Double> = mutableMapOf()
    private val questId = arguments.keyQuestId

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _observablePlayAnimation = MutableLiveData<Unit>()
    val observablePlayAnimation: LiveData<Unit> = _observablePlayAnimation

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _liveTextTimer = MutableLiveData<String>()
    val liveTextTimer: LiveData<String> = _liveTextTimer

    private val _liveTimerColor = MutableLiveData<Int>()
    val liveTimerColor: LiveData<Int> = _liveTimerColor

    private val _liveButtonEnabled = MutableLiveData<Boolean>()
    val liveButtonEnabled: LiveData<Boolean> = _liveButtonEnabled

    private val _liveButtonText = MutableLiveData<String>()
    val liveButtonText: LiveData<String> = _liveButtonText

    private val _observableRunningPath = MutableLiveData<List<LatLng>>()
    val observableRunningPath: LiveData<List<LatLng>> = _observableRunningPath

    private val _liveTextTitle = MutableLiveData<String>()
    val liveTextTitle: LiveData<String> = _liveTextTitle

    private val _observableStartMarker = MutableLiveData<LatLng>()
    val observableStartMarker: LiveData<LatLng> = _observableStartMarker

    private val _observableEndMarker = MutableLiveData<LatLng>()
    val observableEndMarker: LiveData<LatLng> = _observableEndMarker

    private val _liveRunningProgress = MutableLiveData<Int>()
    val liveRunningProgress: LiveData<Int> = _liveRunningProgress

    private val _liveWalkingProgress = MutableLiveData<Int>()
    val liveWalkingProgress: LiveData<Int> = _liveWalkingProgress

    private val _liveBicyclingProgress = MutableLiveData<Int>()
    val liveBicyclingProgress: LiveData<Int> = _liveBicyclingProgress

    private val _liveStillProgress = MutableLiveData<Int>()
    val liveStillProgress: LiveData<Int> = _liveStillProgress

    private val _liveDrivingProgress = MutableLiveData<Int>()
    val liveDrivingProgress: LiveData<Int> = _liveDrivingProgress

    private val _liveRunningVisibility = MutableLiveData<Int>()
    val liveRunningVisibility: LiveData<Int> = _liveRunningVisibility

    private val _liveWalkingVisibility = MutableLiveData<Int>()
    val liveWalkingVisibility: LiveData<Int> = _liveWalkingVisibility

    private val _liveBicycleVisibility = MutableLiveData<Int>()
    val liveBicycleVisibility: LiveData<Int> = _liveBicycleVisibility

    private val _liveStillVisibility = MutableLiveData<Int>()
    val liveStillVisibility: LiveData<Int> = _liveStillVisibility

    private val _liveDrivingVisibility = MutableLiveData<Int>()
    val liveDrivingVisibility: LiveData<Int> = _liveDrivingVisibility

    private val _liveTextRunningPercentage = MutableLiveData<String>()
    val liveTextRunningPercentage: LiveData<String> = _liveTextRunningPercentage

    private val _liveTextWalkingPercentage = MutableLiveData<String>()
    val liveTextWalkingPercentage: LiveData<String> = _liveTextWalkingPercentage

    private val _liveTextBicyclingPercentage = MutableLiveData<String>()
    val liveTextBicyclingPercentage: LiveData<String> = _liveTextBicyclingPercentage

    private val _liveTextStillPercentage = MutableLiveData<String>()
    val liveTextStillPercentage: LiveData<String> = _liveTextStillPercentage

    private val _liveTextDrivingPercentage = MutableLiveData<String>()
    val liveTextDrivingPercentage: LiveData<String> = _liveTextDrivingPercentage

    init {
        //TODO check if imperial or metric
        _liveButtonEnabled.postValue(false)
        _liveTextTimer.postValue("00:00:00")
        _liveTotalDistance.postValue("0 m")
        _liveCalories.postValue("0")
        _livePace.postValue("0 min/km")
        _liveButtonText.postValue(resourceUtil.getString(R.string.runage_save_progress))
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
        getQuest()
    }

    @SuppressLint("CheckResult")
    private fun getQuest() {
        questRepository.getSingleQuest(questId)
            .subscribe({
                quest = it
                if (mapCreated) {
                    displayPath(it)
                }
                setUpQuestInfo(it)
            }, {
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    private fun setUpQuestInfo(quest: Quest) {
        val lastTimeStamp =
            quest.locations.lastOrNull()?.timeStampEpochSeconds ?: Instant.now().epochSecond
        val duration = lastTimeStamp - quest.startTimeEpochSeconds
        val distance = quest.totalDistance
        _liveTextTimer.postValue(RunningUtil.convertTimeToDurationString(duration))
        _liveTotalDistance.postValue("${distance.toInt()} m")
        _liveCalories.postValue("${quest.calories}")
        _livePace.postValue(RunningUtil.getPaceString(duration, distance))

        if (quest.locations.size < 2) {
            _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
            _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_quest_failed))
            feedbackHandler.speak(resourceUtil.getString(R.string.runage_quest_failed))
            _liveButtonText.postValue(resourceUtil.getString(R.string.runage_close))
        } else {
            _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_quest_completed))
            feedbackHandler.speak(resourceUtil.getString(R.string.runage_quest_completed))
            _observablePlayAnimation.postValue(Unit)
        }

        setUpActivityTypeInfo(quest)
    }

    @SuppressLint("CheckResult")
    private fun setUpActivityTypeInfo(quest: Quest) {
        RunningUtil.calculateActivityDurationPercentage(quest.locations)
            .doFinally {
                _liveButtonEnabled.postValue(true)
            }
            .subscribe({
                percentageMap = it
                val runningPercentage = getActivityPercentage(it, DetectedActivity.RUNNING)
                val walkingPercentage = getActivityPercentage(it, DetectedActivity.WALKING)
                val bicyclingPercentage = getActivityPercentage(it, DetectedActivity.ON_BICYCLE)
                val stillPercentage = getActivityPercentage(it, DetectedActivity.STILL)
                val drivingPercentage = getActivityPercentage(it, DetectedActivity.IN_VEHICLE)
                if (runningPercentage > 0) {
                    _liveRunningProgress.postValue(runningPercentage)
                    _liveTextRunningPercentage.postValue("${resourceUtil.getString(R.string.runage_running_percentage)} - $runningPercentage")
                } else {
                    _liveRunningVisibility.postValue(View.GONE)
                }
                if (walkingPercentage > 0) {
                    _liveWalkingProgress.postValue(walkingPercentage)
                    _liveTextWalkingPercentage.postValue("${resourceUtil.getString(R.string.runage_walking_percentage)} - $walkingPercentage")
                } else {
                    _liveWalkingVisibility.postValue(View.GONE)
                }
                if (bicyclingPercentage > 0) {
                    _liveBicyclingProgress.postValue(bicyclingPercentage)
                    _liveTextBicyclingPercentage.postValue("${resourceUtil.getString(R.string.runage_bicycling_percentage)} - $bicyclingPercentage")
                } else {
                    _liveBicycleVisibility.postValue(View.GONE)
                }
                if (stillPercentage > 0) {
                    _liveStillProgress.postValue(stillPercentage)
                    _liveTextStillPercentage.postValue("${resourceUtil.getString(R.string.runage_still_percentage)} - $stillPercentage")
                } else {
                    _liveStillVisibility.postValue(View.GONE)
                }
                if (drivingPercentage > 0) {
                    _liveDrivingProgress.postValue(drivingPercentage)
                    _liveTextDrivingPercentage.postValue("${resourceUtil.getString(R.string.runage_driving_percentage)} - $drivingPercentage")
                } else {
                    _liveDrivingVisibility.postValue(View.GONE)
                }

                _liveRunningVisibility
                Timber.d("Percentage calculated")
            }, {
                Timber.e(it)
            })
    }

    private fun getActivityPercentage(it: Map<Int, Double>, activityType: Int) =
        it[activityType]?.times(100)?.toInt() ?: 0

    fun onSaveProgressClicked() {
        if (quest?.locations?.size ?: 0 < 2) {
            observableBackNavigation.call()
        } else {
            quest?.let { quest ->
                Timber.d("Saving quest")
                saveExperience(quest)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun saveExperience(quest: Quest) {
        fireStoreHandler.getUserInfo()
            .addOnCompleteListener {
                syncWithGoogleFit(quest)
            }
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userInfo = document.toObject(UserInfo::class.java)
                    val userId = userInfo?.userId ?: ""
                    Timber.d("Got user info")
                    RunningUtil.calculateRunningExperienceDistanceAndDuration(quest.locations, locationUtil)
                        .subscribe({
                            Timber.d("Calculated user experience: $it")
                            val newXp = (userInfo?.xp ?: 0) + it.first
                            val totalCalories = quest.calories
                            val totalRunningDistance = it.second
                            val totalRunningDuration = it.third
                            Timber.d("New xp: $newXp")
                            val newUserInfo = UserInfo(userId = userId, xp = newXp, calories = totalCalories, distance = totalRunningDistance.toInt(), duration = totalRunningDuration.toInt())
                            fireStoreHandler.storeUserInfo(newUserInfo)
                                .addOnSuccessListener { Timber.d("User info have been stored") }
                                .addOnFailureListener { Timber.e("Failed storing user xp") }
                        }, {
                            Timber.e(it)
                        })
                } else {
                    Timber.d("No such document")
                }
            }
            .addOnFailureListener { exception ->
                Timber.e("get failed with $exception")
            }
    }

    private fun syncWithGoogleFit(quest: Quest) {
        val task = fitnessHelper.storeSession(
            quest.id,
            "${quest.totalDistance} meters",
            quest.startTimeEpochSeconds,
            quest.locations.lastOrNull()?.timeStampEpochSeconds
                ?: quest.startTimeEpochSeconds + 1
        )
        task.addOnSuccessListener { Timber.d("Synced with google fit") }
        task.addOnFailureListener { Timber.e("Sync with google fit failed") }
        task.addOnCompleteListener { storeQuestInFirestore(quest) }
    }

    private fun storeQuestInFirestore(quest: Quest): Disposable {
        return fireStoreHandler.storeQuest(quest, percentageMap)
            .subscribe({
                it.addOnCompleteListener {
                    observableBackNavigation.call()
                }
                it.addOnSuccessListener {
                    Timber.d("Quest have been stored")
                }
                it.addOnFailureListener { error -> Timber.e("Quest storing failed $error") }
            }, { error ->
                Timber.e("Quest storing failed $error")
            })
    }

    fun onMapCreated() {
        val localQuest = quest
        mapCreated = true
        if (localQuest != null) {
            displayPath(localQuest)
        }
    }

    fun onMapClicked() {
        observableNavigateTo.postValue(
            SummaryFragmentDirections.actionSummaryFragmentToPathFragment(
                questId
            )
        )
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
}
