package xevenition.com.runage.fragment.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startForegroundService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
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

        binding.swipeButton.onSwipedOnListener = {
            binding.viewPager.setCurrentItem(1, true)
            startEventService()
        }

        binding.swipeButton.onSwipedOffListener = {
            binding.viewPager.setCurrentItem(0, true)
            stopEventService()
        }
    }

    private fun startEventService(){
        val myService = Intent(activity, EventService::class.java)
        startForegroundService(requireContext(), myService)
    }

    private fun stopEventService(){
        val myService = Intent(activity, EventService::class.java)
        activity?.stopService(myService)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
    }

    companion object {
        const val CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE"
        const val ONGOING_NOTIFICATION_ID = 532632

        fun newInstance() = MainFragment()
    }

    private class MainPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int =
            NUM_PAGES

        override fun getItem(position: Int): Fragment {
            return if (position == 0) StartFragment.newInstance() else MapFragment.newInstance()
        }

        companion object {
            const val NUM_PAGES = 2
        }
    }

}
