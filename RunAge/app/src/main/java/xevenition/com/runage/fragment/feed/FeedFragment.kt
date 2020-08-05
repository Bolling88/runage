package xevenition.com.runage.fragment.feed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentFeedBinding
import xevenition.com.runage.fragment.history.HistoryFragment
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_ALL
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_FOLLOWING
import xevenition.com.runage.fragment.history.HistoryFragment.Companion.PAGE_MINE
import xevenition.com.runage.model.SavedQuest

class FeedFragment : BaseFragment<FeedViewModel>() {

    private lateinit var binding: FragmentFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = FeedViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(FeedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openMenu()
        }

        val pagerAdapter = ScreenSlidePagerAdapter(childFragmentManager, requireContext())
        binding.viewPager.adapter = pagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.getTabAt(PAGE_MINE)?.setIcon(R.drawable.ic_profile)
        binding.tabLayout.getTabAt(PAGE_FOLLOWING)?.setIcon(R.drawable.ic_following)
        binding.tabLayout.getTabAt(PAGE_ALL)?.setIcon(R.drawable.ic_earth)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab select
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()
    }

    fun onQuestClicked(quest: SavedQuest) {
        viewModel.onQuestClicked(quest)
    }
}

private class ScreenSlidePagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment = HistoryFragment.getInstance(position)

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            PAGE_MINE -> context.getString(R.string.runage_mine)
            PAGE_FOLLOWING -> context.getString(R.string.runage_following)
            PAGE_ALL -> context.getString(R.string.runage_all)
            else -> ""
        }
    }
}
