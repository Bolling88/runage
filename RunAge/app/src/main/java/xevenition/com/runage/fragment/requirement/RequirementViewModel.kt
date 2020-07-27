package xevenition.com.runage.fragment.requirement

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bokus.play.util.SingleLiveEvent
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.fragment.start.StartFragmentDirections
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.model.UserInfo
import xevenition.com.runage.util.*

class RequirementViewModel(
    private val gameServicesUtil: GameServicesUtil,
    fireStoreHandler: FireStoreHandler,
    private val accountUtil: AccountUtil,
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

    val observableOpenMenu = SingleLiveEvent<Unit>()

    private val _observableProfileImage = MutableLiveData<Uri>()
    val observableProfileImage: LiveData<Uri> = _observableProfileImage

    val observableShowAchievements = SingleLiveEvent<Intent>()

    init {
        val challenge = args.keyChallenge
        challenge?.let {
            val challengeText = "${resourceUtil.getString(R.string.runage_challenge)} ${it.level}"
            feedbackHandler.speak(challengeText)
            _liveTextLevel.postValue(challengeText)
            _liveTextTime1.postValue(runningUtil.convertTimeToDurationString(it.time.toLong()))
            _liveTextTime2.postValue(runningUtil.convertTimeToDurationString(it.time.toLong()-20))
            _liveTextTime3.postValue(runningUtil.convertTimeToDurationString(it.time.toLong()-40))
            _liveTextXp.postValue("${it.experience} ${resourceUtil.getString(R.string.runage_xp)}")
            _liveTextDistance.postValue(runningUtil.getDistanceString(it.distance))

            when(args.keyStars){
                0->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                }
                1->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                }
                2->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star_border))
                }
                3->{
                    _liveStar1Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar2Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                    _liveStar3Image.postValue(resourceUtil.getDrawable(R.drawable.ic_star))
                }
            }
        }
    }

    fun onStartClicked(){
        observableNavigateTo.postValue(RequirementFragmentDirections.actionRequirementFragmentToMapFragment())
    }
}
