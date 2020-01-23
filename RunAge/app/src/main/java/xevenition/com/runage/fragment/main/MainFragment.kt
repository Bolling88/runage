package xevenition.com.runage.fragment.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.databinding.FragmentMainBinding
import xevenition.com.runage.fragment.map.MapFragment
import xevenition.com.runage.fragment.start.StartFragment
import xevenition.com.runage.service.EventService
import javax.inject.Inject


class MainFragment : BaseFragment<MainViewModel>() {

    private lateinit var adapter: MainPagerAdapter
    private lateinit var binding: FragmentMainBinding
    private lateinit var mService: EventService
    private var mBound: Boolean = false
    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as EventService.LocalBinder
            mService = binder.getService()
            mBound = true
            mService.registerCallback(object: EventService.EventCallback{
                override fun onQuestCreated(id: Int) {
                    Timber.d("onQuestCreated: $id")
                    (adapter.getItem(1) as MapFragment).onNewQuestCreated(id)
                }
            })
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    @Inject
    lateinit var factory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)

        adapter =
            MainPagerAdapter(
                childFragmentManager
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.viewPager.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (EventService.serviceIsRunning) {
            binding.viewPager.currentItem = 1
            binding.swipeButton.isChecked = true
        }

        binding.swipeButton.onSwipedOnListener = {
            binding.viewPager.setCurrentItem(1, true)
            startEventService()
        }

        binding.swipeButton.onSwipedOffListener = {
            binding.viewPager.setCurrentItem(0, true)
            stopEventService()
            (adapter.getItem(1) as MapFragment).onQuestFinished()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onStart() {
        super.onStart()
        if(EventService.serviceIsRunning) {
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
        if(mBound) {
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
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private var fragments: Array<Fragment> = arrayOf(StartFragment.newInstance(), MapFragment.newInstance())

        override fun getCount(): Int =
            NUM_PAGES

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        companion object {
            const val NUM_PAGES = 2
        }
    }

}
