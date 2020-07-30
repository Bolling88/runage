package xevenition.com.runage.fragment.requirement

import android.graphics.drawable.Drawable
import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.util.*

class RequirementViewModel(
    private val runningUtil: RunningUtil,
    resourceUtil: ResourceUtil,
    feedbackHandler: FeedbackHandler,
    private val args: RequirementFragmentArgs
) : BaseViewModel() {

    private val _liveTextDistance = MutableLiveData<String>()
    val liveTextDistance : LiveData<String> = _liveTextDistance

    private val _liveTextTime1 = MutableLiveData<String>()
    val liveTextTime1 : LiveData<String> = _liveTextTime1

    private val _liveTextTime2 = MutableLiveData<String>()
    val liveTextTime2 : LiveData<String> = _liveTextTime2

    private val _liveTextTime3 = MutableLiveData<String>()
    val liveTextTime3 : LiveData<String> = _liveTextTime3

    private val _liveTextPace1 = MutableLiveData<String>()
    val liveTextPace1 : LiveData<String> = _liveTextPace1

    private val _liveTextPace2 = MutableLiveData<String>()
    val liveTextPace2 : LiveData<String> = _liveTextPace2

    private val _liveTextPace3 = MutableLiveData<String>()
    val liveTextPace3 : LiveData<String> = _liveTextPace3

    private val _liveTextXp = MutableLiveData<String>()
    val liveTextXp : LiveData<String> = _liveTextXp

    private val _liveTextLevel = MutableLiveData<String>()
    val liveTextLevel : LiveData<String> = _liveTextLevel

    private val _liveStar1Image = MutableLiveData<Drawable>()
    val liveStar1Image: LiveData<Drawable> = _liveStar1Image

    private val _liveStar2Image = MutableLiveData<Drawable>()
    val liveStar2Image: LiveData<Drawable> = _liveStar2Image

    private val _liveStar3Image = MutableLiveData<Drawable>()
    val liveStar3Image: LiveData<Drawable> = _liveStar3Image

    private val _liveRewardTextColor = MutableLiveData<Int>()
    val liveRewardTextColor: LiveData<Int> = _liveRewardTextColor

    init {
        val challenge = args.keyChallenge
        challenge?.let {
            val challengeText = "${resourceUtil.getString(R.string.runage_challenge)} ${it.level}"
            feedbackHandler.speak(challengeText, TextToSpeech.QUEUE_FLUSH)
            _liveTextLevel.postValue(challengeText)
            var timeMultiplier = it.level % 10
            if(timeMultiplier == 0)
                timeMultiplier = 10

            val limit1 = it.time.toLong()
            val limit2 = it.time.toLong()-timeMultiplier * 10
            val limit3 = it.time.toLong()-timeMultiplier * 20

            _liveTextTime1.postValue(runningUtil.convertTimeToDurationString(limit1))
            _liveTextTime2.postValue(runningUtil.convertTimeToDurationString(limit2))
            _liveTextTime3.postValue(runningUtil.convertTimeToDurationString(limit3))

            _liveTextPace1.postValue("(${runningUtil.getPaceString(limit1, it.distance.toDouble(), true)})")
            _liveTextPace2.postValue("(${runningUtil.getPaceString(limit2, it.distance.toDouble(), true)})")
            _liveTextPace3.postValue("(${runningUtil.getPaceString(limit3, it.distance.toDouble(), true)})")

            _liveTextDistance.postValue(runningUtil.getDistanceString(it.distance))

            when(args.keyStars){
                0->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveRewardTextColor.postValue(resourceUtil.getColor(R.color.colorPrimary))
                    _liveTextXp.postValue("${it.experience} ${resourceUtil.getString(R.string.runage_xp)}")
                }
                1->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveRewardTextColor.postValue(resourceUtil.getColor(R.color.grey1))
                    _liveTextXp.postValue(resourceUtil.getString(R.string.runage_reward_claimed))
                }
                2->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveRewardTextColor.postValue(resourceUtil.getColor(R.color.grey1))
                    _liveTextXp.postValue(resourceUtil.getString(R.string.runage_reward_claimed))
                }
                3->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveRewardTextColor.postValue(resourceUtil.getColor(R.color.grey1))
                    _liveTextXp.postValue(resourceUtil.getString(R.string.runage_reward_claimed))
                }
            }
        }
    }

    fun onStartClicked(){
        observableNavigateTo.postValue(RequirementFragmentDirections.actionRequirementFragmentToMapFragment(args.keyChallenge))
    }
}
