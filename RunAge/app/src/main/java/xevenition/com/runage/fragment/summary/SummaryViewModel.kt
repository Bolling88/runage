package xevenition.com.runage.fragment.summary

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.model.RunStats
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.room.repository.QuestRepository
import xevenition.com.runage.service.FitnessHelper
import xevenition.com.runage.util.*
import java.time.Instant

class SummaryViewModel(
    private val gameServicesUtil: GameServicesUtil,
    private val accountUtil: AccountUtil,
    private val fitnessHelper: FitnessHelper,
    private val locationUtil: LocationUtil,
    private val feedbackHandler: FeedbackHandler,
    private val questRepository: QuestRepository,
    private val resourceUtil: ResourceUtil,
    private val fireStoreHandler: FireStoreHandler,
    private val saveUtil: SaveUtil,
    private val runningUtil: RunningUtil,
    arguments: SummaryFragmentArgs
) : BaseViewModel() {
    private var runStats: RunStats? = null
    private var mapCreated: Boolean = false
    private var quest: Quest? = null
    private val questId = arguments.keyQuestId

    private val _liveTotalDistance = MutableLiveData<String>()
    val liveTotalDistance: LiveData<String> = _liveTotalDistance

    private val _liveCalories = MutableLiveData<String>()
    val liveCalories: LiveData<String> = _liveCalories

    private val _livePace = MutableLiveData<String>()
    val livePace: LiveData<String> = _livePace

    private val _observablePlaySuccessAnimation = MutableLiveData<Unit>()
    val observablePlaySuccessAnimation: LiveData<Unit> = _observablePlaySuccessAnimation

    private val _observablePlayFailAnimation = MutableLiveData<Unit>()
    val observablePlayFailAnimation: LiveData<Unit> = _observablePlayFailAnimation

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

    private val _liveDeleteButtonVisibility = MutableLiveData<Int>()
    val liveDeleteButtonVisibility: LiveData<Int> = _liveDeleteButtonVisibility

    private val _liveTextStillPercentage = MutableLiveData<String>()
    val liveTextStillPercentage: LiveData<String> = _liveTextStillPercentage

    private val _liveTextDrivingPercentage = MutableLiveData<String>()
    val liveTextDrivingPercentage: LiveData<String> = _liveTextDrivingPercentage

    private val _liveTextExperience = MutableLiveData<String>()
    val liveTextExperience: LiveData<String> = _liveTextExperience

    private val _liveLoadingVisibility = MutableLiveData<Int>()
    val liveLoadingVisibility: LiveData<Int> = _liveLoadingVisibility

    val observableYesNoDialog = SingleLiveEvent<Pair<String, String>>()
    val observableShowAd = SingleLiveEvent<Unit>()
    val observableBackPressedEnabled = SingleLiveEvent<Boolean>()

    init {
        _liveButtonEnabled.postValue(false)
        _liveTextTimer.postValue("00:00:00")
        _liveTotalDistance.postValue("0 m")
        _liveCalories.postValue("0")
        _liveLoadingVisibility.postValue(View.GONE)
        _liveButtonText.postValue(resourceUtil.getString(R.string.runage_save_run))
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.colorPrimary))

        if (saveUtil.getBoolean(SaveUtil.KEY_IS_USING_METRIC, true)) {
            _livePace.postValue("0 ${resourceUtil.getString(R.string.runage_min_km)}")
        } else {
            _livePace.postValue("0 ${resourceUtil.getString(R.string.runage_min_mi)}")
        }

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
        _liveTextTimer.postValue(runningUtil.convertTimeToDurationString(duration))
        _liveTotalDistance.postValue(runningUtil.getDistanceString(quest.totalDistance.toInt()))
        _liveCalories.postValue("${quest.calories}")
        _livePace.postValue(runningUtil.getPaceString(duration, distance, true))

        if (quest.locations.size < 2) {
            _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
            _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_quest_failed))
            feedbackHandler.speak(resourceUtil.getString(R.string.runage_quest_failed))
            _liveButtonText.postValue(resourceUtil.getString(R.string.runage_close))
            _liveDeleteButtonVisibility.postValue(View.GONE)
            _observablePlayFailAnimation.postValue(Unit)
        } else {
            _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_quest_completed))
            feedbackHandler.speak(resourceUtil.getString(R.string.runage_quest_completed))
            _observablePlaySuccessAnimation.postValue(Unit)
        }

        setUpActivityTypeInfo(quest)
    }

    @SuppressLint("CheckResult")
    private fun setUpActivityTypeInfo(quest: Quest) {
        runningUtil.processRunningStats(quest.locations, locationUtil)
            .subscribe({
                runStats = it
                val runningPercentage =
                    getActivityPercentage(it.activityPercentage, DetectedActivity.RUNNING)
                val walkingPercentage =
                    getActivityPercentage(it.activityPercentage, DetectedActivity.WALKING)
                val bicyclingPercentage =
                    getActivityPercentage(it.activityPercentage, DetectedActivity.ON_BICYCLE)
                val stillPercentage =
                    getActivityPercentage(it.activityPercentage, DetectedActivity.STILL)
                val drivingPercentage =
                    getActivityPercentage(it.activityPercentage, DetectedActivity.IN_VEHICLE)
                if (runningPercentage > 0) {
                    _liveRunningProgress.postValue(runningPercentage)
                    _liveTextRunningPercentage.postValue("${resourceUtil.getString(R.string.runage_running)} - $runningPercentage%")
                } else {
                    _liveRunningVisibility.postValue(View.GONE)
                }
                if (walkingPercentage > 0) {
                    _liveWalkingProgress.postValue(walkingPercentage)
                    _liveTextWalkingPercentage.postValue("${resourceUtil.getString(R.string.runage_walking)} - $walkingPercentage%")
                } else {
                    _liveWalkingVisibility.postValue(View.GONE)
                }
                if (bicyclingPercentage > 0) {
                    _liveBicyclingProgress.postValue(bicyclingPercentage)
                    _liveTextBicyclingPercentage.postValue("${resourceUtil.getString(R.string.runage_bicycling)} - $bicyclingPercentage%")
                } else {
                    _liveBicycleVisibility.postValue(View.GONE)
                }
                if (stillPercentage > 0) {
                    _liveStillProgress.postValue(stillPercentage)
                    _liveTextStillPercentage.postValue("${resourceUtil.getString(R.string.runage_still)} - $stillPercentage%")
                } else {
                    _liveStillVisibility.postValue(View.GONE)
                }
                if (drivingPercentage > 0) {
                    _liveDrivingProgress.postValue(drivingPercentage)
                    _liveTextDrivingPercentage.postValue("${resourceUtil.getString(R.string.runage_driving)} - $drivingPercentage%")
                } else {
                    _liveDrivingVisibility.postValue(View.GONE)
                }

                _liveTextExperience.postValue("${it.xp} ${resourceUtil.getString(R.string.runage_xp)}+")

                _liveRunningVisibility
                Timber.d("Percentage calculated")

                updateUserStats(quest, it)
            }, {
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    private fun updateUserStats(quest: Quest, runStats: RunStats) {
        fireStoreHandler.getUserInfo()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userInfo = document.toObject(UserInfo::class.java)
                    val userId = userInfo?.userId ?: ""
                    val newXp = (userInfo?.xp ?: 0) + runStats.xp
                    val totalCalories = (userInfo?.calories ?: 0) + quest.calories
                    val totalRunningDistance =
                        (userInfo?.distance ?: 0) + runStats.runningDistance
                    val totalRunningDuration =
                        (userInfo?.duration ?: 0) + runStats.runningDuration
                    Timber.d("New xp: $newXp")

                    val newUserInfo = UserInfo(
                        userId = userId,
                        xp = newXp,
                        calories = totalCalories,
                        distance = totalRunningDistance,
                        challengeScore = userInfo?.challengeScore ?: mapOf(),
                        duration = totalRunningDuration
                    )

                    fireStoreHandler.storeUserInfo(newUserInfo)
                        .addOnCompleteListener { _liveButtonEnabled.postValue(true) }
                        .addOnSuccessListener {
                            Timber.d("User info have been stored")
                            storeAchievementsAndLeaderboards(quest, runStats, newUserInfo)
                        }
                        .addOnFailureListener { Timber.e("get failed with $it") }
                    Timber.d("Got user info")
                } else {
                    Timber.d("No such document")
                }
            }
            .addOnFailureListener { exception ->
                Timber.e("get failed with $exception")
                _liveButtonEnabled.postValue(true)
            }
    }

    @SuppressLint("CheckResult")
    private fun storeAchievementsAndLeaderboards(
        quest: Quest,
        runStats: RunStats,
        userInfo: UserInfo
    ) {
        Timber.d("Saving achievements and leaderboards")
        Single.fromCallable {
            gameServicesUtil.storeAchievementsAndLeaderboards(quest, runStats, userInfo)
        }
            .subscribeOn(Schedulers.io())
            .subscribe({
                Timber.d("Achievements stored")
            }, {
                Timber.e(it)
            })
    }

    fun onSaveProgressClicked() {
        if (quest?.locations?.size ?: 0 < 2) {
            quest?.let {
                deleteAndExit(it)
            }
        } else {
            quest?.let { quest ->
                Timber.d("Saving quest")
                _liveLoadingVisibility.postValue(View.VISIBLE)
                Observable.just(quest)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        storeQuestInFirestore(quest, runStats!!)
                    }, {
                        Timber.e(it)
                        _liveLoadingVisibility.postValue(View.GONE)
                        showErrorDialog()
                    })
            }
        }
    }

    private fun showErrorDialog() {
        observableInfoDialog.postValue(
            Triple(
                resourceUtil.getString(R.string.runage_save_quest_failed_title),
                resourceUtil.getString(R.string.runage_save_quest_failed_message),
                resourceUtil.getString(R.string.runage_ok)
            )
        )
    }

    private fun storeQuestInFirestore(
        quest: Quest,
        runStats: RunStats
    ): Disposable {
        return fireStoreHandler.storeQuest(quest, runStats)
            .subscribe({
                it.addOnCompleteListener {
                    syncWithGoogleFit(quest)
                }
                it.addOnSuccessListener {
                    Timber.d("Quest have been stored")
                }
                it.addOnFailureListener { error -> Timber.e("Quest storing failed $error") }
            }, { error ->
                Timber.e("Quest storing failed $error")
            })
    }

    private fun syncWithGoogleFit(quest: Quest) {
        if (saveUtil.getBoolean(SaveUtil.KEY_SYNC_GOOGLE_FIT, true)) {
            val task = fitnessHelper.storeSession(
                quest.id,
                "${quest.totalDistance} meters",
                quest.startTimeEpochSeconds,
                quest.locations.lastOrNull()?.timeStampEpochSeconds
                    ?: quest.startTimeEpochSeconds + 1
            )
            task.addOnSuccessListener { Timber.d("Synced with google fit") }
            task.addOnFailureListener { Timber.e("Sync with google fit failed") }
            task.addOnCompleteListener {
                //deleteLocalQuestAfterSaveCompletion(quest)
                observableNavigateTo.postValue(SummaryFragmentDirections.actionSummaryFragmentToShareFragment(questId))
            }
        } else {
            //deleteLocalQuestAfterSaveCompletion(quest)
            observableNavigateTo.postValue(SummaryFragmentDirections.actionSummaryFragmentToShareFragment(questId))
        }
    }

    @SuppressLint("CheckResult")
    private fun deleteAndExit(quest: Quest) {
        Observable.just(quest)
            .subscribeOn(Schedulers.io())
            .subscribe({
                questRepository.dbDeleteQuest(quest)
                Timber.d("All done!")
                observableShowAd.call()
                observableBackPressedEnabled.postValue(false)
            }, {
                Timber.e(it)
                observableShowAd.call()
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

    fun onDeleteClicked() {
        observableYesNoDialog.postValue(
            Pair(
                resourceUtil.getString(R.string.runage_delete_run_title),
                resourceUtil.getString(R.string.runage_delete_run_message)
            )
        )
    }

    fun onDeleteConfirmed() {
        _liveLoadingVisibility.postValue(View.VISIBLE)
        quest?.let {
            deleteAndExit(it)
        }
    }

    private fun getActivityPercentage(it: Map<Int, Double>, activityType: Int) =
        it[activityType]?.times(100)?.toInt() ?: 0
}
