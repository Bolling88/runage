package xevenition.com.runage.fragment.challenges

import androidx.lifecycle.ViewModel
import xevenition.com.runage.MainApplication
import xevenition.com.runage.architecture.BaseViewModelFactory
import xevenition.com.runage.repository.UserRepository
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class ChallengesViewModelFactory @Inject constructor(
    app: MainApplication
) :
    BaseViewModelFactory() {

    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var resourceUtil: ResourceUtil

    init {
        app.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChallengesViewModel(resourceUtil, userRepository) as T
    }
}