package xevenition.com.runage.fragment.viewpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.MainApplication.Companion.serviceIsRunning
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentViewPageBinding

class ViewPageFragment : BaseFragment<ViewPageViewModel>() {

    private lateinit var binding: FragmentViewPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity?.applicationContext as MainApplication).appComponent.inject(this)
       // args = ViewPageFragmentArgs.fromBundle(requireArguments())
        val factory = ViewPageViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(ViewPageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_page, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
        Timber.d("onViewCreated")
        if (serviceIsRunning) {
            binding.viewPager.currentItem = 1
            binding.swipeButton.isChecked = true
            binding.lottieSwipeEnd.visibility = View.VISIBLE
            binding.lottieSwipeEnd.playAnimation()
            setLottieSwipeState(true)
        } else {
            binding.lottieSwipeStart.visibility = View.VISIBLE
            binding.lottieSwipeStart.playAnimation()
            setLottieSwipeState(false)
        }

        binding.swipeButton.onSwipedOnListener = {
            binding.viewPager.setCurrentItem(1, true)
            if (serviceIsRunning) {
                //don't run the count down if a quest is active
                binding.lottieCountDown.visibility = View.GONE
                binding.lottieCountDown.pauseAnimation()
            } else {
                binding.lottieCountDown.visibility = View.VISIBLE
                binding.lottieCountDown.playAnimation()
            }
            setLottieSwipeState(true)
        }

        binding.swipeButton.onSwipedOffListener = {
            binding.viewPager.setCurrentItem(0, true)
            setLottieSwipeState(false)
            binding.lottieCountDown.visibility = View.GONE
            binding.lottieCountDown.pauseAnimation()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.unlockDrawer()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        viewModel.onResume()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableUserAccount.observe(viewLifecycleOwner, Observer {
            it?.let {
                (activity as? MainActivity)?.setUpAchievementsLayout(it)
            }
        })
    }

    private fun setLottieSwipeState(active: Boolean) {
        if (active) {
            binding.lottieSwipeEnd.visibility = View.VISIBLE
            binding.lottieSwipeEnd.playAnimation()
            binding.lottieSwipeStart.visibility = View.GONE
            binding.lottieSwipeStart.pauseAnimation()
        } else {
            binding.lottieSwipeStart.visibility = View.VISIBLE
            binding.lottieSwipeStart.playAnimation()
            binding.lottieSwipeEnd.visibility = View.GONE
            binding.lottieSwipeEnd.pauseAnimation()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.lottieCountDown.visibility = View.GONE
        binding.lottieCountDown.pauseAnimation()
    }
}