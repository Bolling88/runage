package xevenition.com.runage.fragment.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.startForegroundService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentMainBinding
import xevenition.com.runage.fragment.map.MapFragment
import xevenition.com.runage.fragment.start.StartFragment
import xevenition.com.runage.service.EventService


class MainFragment : BaseFragment<MainViewModel>() {

    private lateinit var adapter: MainPagerAdapter
    private lateinit var binding: FragmentMainBinding
    private lateinit var mService: EventService
    private var mBound: Boolean = false
    private var currentQuestId = 0

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as EventService.LocalBinder
            mService = binder.getService()
            mBound = true
            mService.registerCallback(object : EventService.EventCallback {
                override fun onQuestCreated(id: Int) {
                    currentQuestId = id
                    Timber.d("onQuestCreated: $id")
                    (adapter.getItem(1) as MapFragment).onNewQuestCreated(id)
                }
            })
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        (activity?.applicationContext as MainApplication).appComponent.inject(this)

        val factory = MainViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = MainPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
        Timber.d("onViewCreated")
        if (EventService.serviceIsRunning) {
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
            if(EventService.serviceIsRunning){
                //don't run the count down if a quest is active
                binding.lottieCountDown.visibility = View.GONE
                binding.lottieCountDown.pauseAnimation()
            }else{
                binding.lottieCountDown.visibility = View.VISIBLE
                binding.lottieCountDown.playAnimation()
            }
            startEventService()
            setLottieSwipeState(true)
        }

        binding.swipeButton.onSwipedOffListener = {
            binding.viewPager.setCurrentItem(0, true)
            stopEventService()
            (adapter.getItem(1) as MapFragment).onQuestFinished()
            setLottieSwipeState(false)
            binding.lottieCountDown.visibility = View.GONE
            binding.lottieCountDown.pauseAnimation()
            viewModel.onQuestFinished(currentQuestId)
        }
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()
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

    override fun onStart() {
        super.onStart()
        if (EventService.serviceIsRunning) {
            bindToService()
        }
    }

    private fun bindToService() {
        Intent(activity, EventService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    private fun unbindService() {
        if (mBound) {
            activity?.unbindService(connection)
            mBound = false
        }
    }

    private fun startEventService() {
        val myService = Intent(activity, EventService::class.java)
        startForegroundService(requireContext(), myService)
        bindToService()
    }

    private fun stopEventService() {
        unbindService()
        val myService = Intent(activity, EventService::class.java)
        activity?.stopService(myService)
    }

    companion object {
        fun newInstance() = MainFragment()
    }

    private class MainPagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private var fragments: Array<Fragment> =
            arrayOf(StartFragment.newInstance(), MapFragment.newInstance())

        override fun getCount(): Int =
            NUM_PAGES

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        //We always want to recreate the fragments if they are destroyed ourselves
        override fun saveState(): Parcelable? {
            return null
        }

        companion object {
            const val NUM_PAGES = 2
        }
    }

}
