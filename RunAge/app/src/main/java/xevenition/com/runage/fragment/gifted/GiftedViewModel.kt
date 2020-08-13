package xevenition.com.runage.fragment.gifted

import android.annotation.SuppressLint
import timber.log.Timber
import xevenition.com.runage.architecture.BaseViewModel
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.SaveUtil
import java.time.Instant

class GiftedViewModel(
    saveUtil: SaveUtil,
    userRepository: UserRepository
) : BaseViewModel() {

  init {
      saveUtil.saveLong(SaveUtil.KEY_REWARD_CLAIMED_DATE, Instant.now().epochSecond)
      saveUserXp(userRepository)
    }

    @SuppressLint("CheckResult")
    private fun saveUserXp(userRepository: UserRepository) {
        userRepository.addUserXp(100)
            .subscribe({
                Timber.d("Xp added to user")
            }, {
                Timber.e(it)
            })
    }

    fun onCloseClicked() {
        observableBackNavigation.call()
    }
}
