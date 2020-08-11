package xevenition.com.runage.fragment.summary

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.games.Player
import xevenition.com.runage.util.SingleLiveEvent
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
import xevenition.com.runage.room.entity.RunageUser
import xevenition.com.runage.room.entity.Quest
import xevenition.com.runage.repository.QuestRepository
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.service.FireStoreService
import xevenition.com.runage.service.GameServicesService
import xevenition.com.runage.service.GoogleFitService
import xevenition.com.runage.util.*
import java.time.Instant

class SummaryViewModel(
    private val gameServicesUtil: GameServicesUtil,
    private val gameServicesService: GameServicesService,
    private val googleFitService: GoogleFitService,
    private val locationUtil: LocationUtil,
    private val feedbackHandler: FeedbackHandler,
    private val questRepository: QuestRepository,
    private val resourceUtil: ResourceUtil,
    private val saveUtil: SaveUtil,
    private val runningUtil: RunningUtil,
    private val userRepository: UserRepository,
    private val fireStoreService: FireStoreService,
    args: SummaryFragmentArgs
) : BaseViewModel() {
    private var runStats: RunStats? = null
    private var mapCreated: Boolean = false
    private var quest: Quest? = null
    private val questId = args.keyQuestId
    private var challengeStars = 0
    private var haveCheated = false
    private var totalNewXp = 0

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

    private val _liveXpTextColor = MutableLiveData<Int>()
    val liveXpTextColor: LiveData<Int> = _liveXpTextColor

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

    private val _liveTextRewardTitle = MutableLiveData<String>()
    val liveTextRewardTitle: LiveData<String> = _liveTextRewardTitle

    private val _liveTextRewardVisibility = MutableLiveData<Int>()
    val liveTextRewardVisibility: LiveData<Int> = _liveTextRewardVisibility

    private val _liveTextReward = MutableLiveData<String>()
    val liveTextReward: LiveData<String> = _liveTextReward

    private val _liveRewardTextColor = MutableLiveData<Int>()
    val liveRewardTextColor: LiveData<Int> = _liveRewardTextColor

    private val _liveLoadingVisibility = MutableLiveData<Int>()
    val liveLoadingVisibility: LiveData<Int> = _liveLoadingVisibility

    private val _liveStar1Image = MutableLiveData<Drawable>()
    val liveStar1Image: LiveData<Drawable> = _liveStar1Image

    private val _liveStar2Image = MutableLiveData<Drawable>()
    val liveStar2Image: LiveData<Drawable> = _liveStar2Image

    private val _liveStar3Image = MutableLiveData<Drawable>()
    val liveStar3Image: LiveData<Drawable> = _liveStar3Image

    private val _liveStarVisibility = MutableLiveData<Int>()
    val liveStarVisibility: LiveData<Int> = _liveStarVisibility

    private val _liveCheatingTextVisibility = MutableLiveData<Int>()
    val liveCheatingTextVisibility: LiveData<Int> = _liveCheatingTextVisibility

    private val _liveSaveButtonBackgroundColor = MutableLiveData<Int>()
    val liveSaveButtonBackgroundColor: LiveData<Int> = _liveSaveButtonBackgroundColor

    val observableYesNoDialog = SingleLiveEvent<Pair<String, String>>()
    val observableShowAd = SingleLiveEvent<Unit>()
    val observableBackPressedEnabled = SingleLiveEvent<Boolean>()

    init {
        _liveButtonEnabled.postValue(false)
        _liveTextTimer.postValue("00:00:00")
        _liveStarVisibility.postValue(View.GONE)
        _liveTextRewardVisibility.postValue(View.GONE)
        _liveCheatingTextVisibility.postValue(View.GONE)
        _liveXpTextColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
        _liveRewardTextColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
        _liveTotalDistance.postValue("0 m")
        _liveCalories.postValue("0")
        _liveLoadingVisibility.postValue(View.GONE)
        _liveButtonText.postValue(resourceUtil.getString(R.string.runage_save_run))
        _liveSaveButtonBackgroundColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
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
                setUpActivityTypeInfo(it)
            }, {
                Timber.e(it)
            })
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
                setUpQuestInfo(quest, it)
            }, {
                Timber.e(it)
            })
    }

    @SuppressLint("CheckResult")
    private fun setUpQuestInfo(quest: Quest, runStats: RunStats) {
        val duration = getQuestDuration(quest)
        val distance = quest.totalDistance
        _liveTextTimer.postValue(runningUtil.convertTimeToDurationString(duration))
        _liveTotalDistance.postValue(runningUtil.getDistanceString(quest.totalDistance.toInt()))
        _liveCalories.postValue("${quest.calories}")
        _livePace.postValue(runningUtil.getPaceString(duration, distance, true))

        if (quest.locations.size < 2) {
            if (quest.level > 0 || quest.isPlayerChallenge) {
                _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_challenge_failed))
                feedbackHandler.speak(resourceUtil.getString(R.string.runage_challenge_failed))
            } else {
                _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_run_failed))
                feedbackHandler.speak(resourceUtil.getString(R.string.runage_run_failed))
            }

            //Quest is so short there is no reason for saving it
            setSaveButtonAsCloseButton()
            _observablePlayFailAnimation.postValue(Unit)
            _liveButtonEnabled.postValue(true)
            //don't update user stats in this case
        } else {
            haveCheated = haveCheated(runStats)
            if (quest.level > 0 || quest.isPlayerChallenge) {
                challengeStars = quest.levelStars
                _liveTextRewardVisibility.postValue(View.VISIBLE)
                if (challengeStars > 0) {
                    if (haveCheated) {
                        //Cheating detected!
                        challengeStars = 0
                        _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_challenge_failed))
                        feedbackHandler.speak(resourceUtil.getString(R.string.runage_challenge_failed))
                        _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
                        if(quest.isPlayerChallenge){
                            _liveTextRewardTitle.postValue(resourceUtil.getString(R.string.runage_challenge_failed_penalty))
                        }else{
                            _liveTextRewardTitle.postValue(resourceUtil.getString(R.string.runage_reward))
                        }
                        setSaveButtonAsCloseButton()
                        _observablePlayFailAnimation.postValue(Unit)
                        _liveCheatingTextVisibility.postValue(View.VISIBLE)
                    } else {
                        if(quest.isPlayerChallenge){
                            _liveStarVisibility.postValue(View.GONE)
                        }else {
                            _liveStarVisibility.postValue(View.VISIBLE)
                            when (challengeStars) {
                                3 -> {
                                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                                }
                                2 -> {
                                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                                }
                                else -> {
                                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                                }
                            }
                        }
                        _liveTextRewardTitle.postValue(resourceUtil.getString(R.string.runage_reward))
                        _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_challenge_completed))
                        feedbackHandler.speak(resourceUtil.getString(R.string.runage_challenge_completed))
                        _observablePlaySuccessAnimation.postValue(Unit)
                    }
                } else {
                    //Challenge failed
                    _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_challenge_failed))
                    feedbackHandler.speak(resourceUtil.getString(R.string.runage_challenge_failed))
                    _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
                    if(quest.isPlayerChallenge){
                        _liveTextRewardTitle.postValue(resourceUtil.getString(R.string.runage_challenge_failed_penalty))
                    }else{
                        _liveTextRewardTitle.postValue(resourceUtil.getString(R.string.runage_reward))
                    }
                    _observablePlayFailAnimation.postValue(Unit)
                }
            } else {
                if(haveCheated){
                    _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_challenge_failed))
                    feedbackHandler.speak(resourceUtil.getString(R.string.runage_challenge_failed))
                    _observablePlayFailAnimation.postValue(Unit)
                    _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
                    _liveCheatingTextVisibility.postValue(View.VISIBLE)
                    setSaveButtonAsCloseButton()
                }else {
                    _liveTextTitle.postValue(resourceUtil.getString(R.string.runage_run_completed))
                    feedbackHandler.speak(resourceUtil.getString(R.string.runage_run_completed))
                    _observablePlaySuccessAnimation.postValue(Unit)
                }
            }
            updateUserStats(quest, runStats)
        }
    }

    private fun setSaveButtonAsCloseButton() {
        _liveTimerColor.postValue(resourceUtil.getColor(R.color.red))
        _liveButtonText.postValue(resourceUtil.getString(R.string.runage_close))
        _liveTextExperience.postValue("0 ${resourceUtil.getString(R.string.runage_xp)}")
        _liveSaveButtonBackgroundColor.postValue(resourceUtil.getColor(R.color.red))
        _liveDeleteButtonVisibility.postValue(View.GONE)
    }

    private fun haveCheated(runStats: RunStats): Boolean {
        return runStats.activityPercentage.getOrDefault(
            DetectedActivity.ON_BICYCLE,
            0.0
        ) > 0.05 || runStats.activityPercentage.getOrDefault(
            DetectedActivity.IN_VEHICLE,
            0.0
        ) > 0.05 || isProbablyAnEmulator()
    }

    private fun isProbablyAnEmulator() = Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.BOARD == "QC_Reference_Phone" //bluestacks
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST.startsWith("Build") //MSI App Player
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk" == Build.PRODUCT

    @SuppressLint("CheckResult")
    private fun updateUserStats(quest: Quest, runStats: RunStats) {
        userRepository.getSingleUser()
            .subscribe({
                val oldUserScoreMap = it?.challengeScore ?: mapOf()
                var challengeCompletedFirstTime = false
                val scoreMap = if (quest.level > 0) {
                    val stars = oldUserScoreMap.getOrDefault(quest.level.toString(), 0)
                    if (stars < challengeStars) {
                        if (stars == 0) {
                            //Previous score was 0, and we have gotten at least one star, which means first time completion
                            challengeCompletedFirstTime = true
                        }
                        val newScoreMap = oldUserScoreMap.toMutableMap()
                        newScoreMap[quest.level.toString()] = challengeStars
                        newScoreMap.toMap()
                    } else {
                        oldUserScoreMap
                    }
                } else {
                    oldUserScoreMap
                }

                totalNewXp =
                    if (quest.level > 0 && challengeStars > 0 && challengeCompletedFirstTime && !haveCheated) {
                        _liveTextReward.postValue("+${quest.levelExperience} ${resourceUtil.getString(R.string.runage_xp)}")
                        runStats.xp + quest.levelExperience
                    }else if(quest.isPlayerChallenge && challengeStars > 0 && !haveCheated){
                        _liveTextReward.postValue("+${quest.levelExperience} ${resourceUtil.getString(R.string.runage_xp)}")
                        userRepository.incrementPlayerChallengesWon()
                        runStats.xp + quest.levelExperience
                    }else if(quest.isPlayerChallenge && (challengeStars <= 0 || haveCheated)){
                        val minusXp = (quest.levelExperience.toDouble()/2).toInt()
                        _liveTextReward.postValue("-$minusXp ${resourceUtil.getString(R.string.runage_xp)}")
                        _liveRewardTextColor.postValue(resourceUtil.getColor(R.color.red))
                        userRepository.incrementPlayerChallengesLost()
                        runStats.xp - minusXp
                    } else if(haveCheated){
                      0
                    }else{
                        runStats.xp
                    }

                if(totalNewXp > 0){
                    _liveTextExperience.postValue("+$totalNewXp ${resourceUtil.getString(R.string.runage_xp)}")
                }else{
                    _liveTextExperience.postValue("$totalNewXp ${resourceUtil.getString(R.string.runage_xp)}")
                    _liveXpTextColor.postValue(resourceUtil.getColor(R.color.red))
                }
                val totalXp = totalNewXp + (it?.xp ?: 0)

                val totalCalories = (it?.calories ?: 0) + quest.calories
                val totalRunningDistance =
                    (it?.distance ?: 0) + runStats.runningDistance
                val totalRunningDuration =
                    (it?.duration ?: 0) + runStats.runningDuration

                val completedRuns = (it?.completedRuns ?: 0) + 1

                val newUserInfo = RunageUser(
                    userId = it.userId,
                    xp = totalXp,
                    calories = totalCalories,
                    distance = totalRunningDistance,
                    challengeScore = scoreMap,
                    playerName = it?.playerName ?: "",
                    completedRuns = completedRuns,
                    following = it?.following ?: listOf(),
                    followers = it?.followers ?: listOf(),
                    playerChallengesLost = it?.playerChallengesLost ?: 0,
                    playerChallengesWon = it?.playerChallengesWon ?: 0,
                    duration = totalRunningDuration
                )

                userRepository.updateUser(newUserInfo)
                    .subscribe({ task ->
                        task.addOnCompleteListener { _liveButtonEnabled.postValue(true) }
                            .addOnSuccessListener {
                                Timber.d("User info have been stored")
                                storeAchievementsAndLeaderboards(quest, runStats, newUserInfo)
                            }
                            .addOnFailureListener { Timber.e("get failed with $it") }
                    }, { throwable ->
                        Timber.e(throwable)
                    })
            },{
                Timber.e(it)
                _liveButtonEnabled.postValue(true)
            })
    }

    @SuppressLint("CheckResult")
    private fun storeAchievementsAndLeaderboards(
        quest: Quest,
        runStats: RunStats,
        userInfo: RunageUser
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
        Timber.d("Save clicked")
        if (quest?.locations?.size ?: 0 < 2 || haveCheated) {
            quest?.let {
                deleteAndExit(it)
            }
        } else {
            quest?.let { quest ->
                Timber.d("Saving quest")
                _liveLoadingVisibility.postValue(View.VISIBLE)
                val task = gameServicesService.getGamesPlayerInfo()
                task?.addOnSuccessListener {
                    commenceSavingQuest(quest, it)
                }
                task?.addOnFailureListener {
                    Timber.e(it)
                }
            }
        }
    }

    private fun commenceSavingQuest(quest: Quest, player: Player): Disposable? {
        return Observable.just(quest)
            .subscribeOn(Schedulers.io())
            .subscribe({
                storeQuestInFirestore(quest, runStats!!, totalNewXp, player)
            }, {
                Timber.e(it)
                _liveLoadingVisibility.postValue(View.GONE)
                showErrorDialog()
            })
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

    private fun getQuestDuration(quest: Quest): Long {
        val lastTimeStamp =
            quest.locations.lastOrNull()?.timeStampEpochSeconds ?: Instant.now().epochSecond
        return lastTimeStamp - quest.startTimeEpochSeconds
    }

    private fun storeQuestInFirestore(
        quest: Quest,
        runStats: RunStats,
        totalXp: Int,
        player: Player
    ): Disposable {
        return fireStoreService.storeQuest(quest, runStats, player, totalXp)
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
            val task = googleFitService.storeSession(
                quest.id,
                "${quest.totalDistance} meters",
                quest.startTimeEpochSeconds,
                quest.locations.lastOrNull()?.timeStampEpochSeconds
                    ?: quest.startTimeEpochSeconds + 1
            )
            task.addOnSuccessListener { Timber.d("Synced with google fit") }
            task.addOnFailureListener { Timber.e("Sync with google fit failed") }
            task.addOnCompleteListener {
                observableNavigateTo.postValue(
                    SummaryFragmentDirections.actionSummaryFragmentToShareFragment(
                        questId
                    )
                )
            }
        } else {
            observableNavigateTo.postValue(
                SummaryFragmentDirections.actionSummaryFragmentToShareFragment(
                    questId
                )
            )
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
