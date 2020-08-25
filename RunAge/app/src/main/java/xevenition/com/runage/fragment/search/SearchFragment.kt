package xevenition.com.runage.fragment.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import xevenition.com.runage.BuildConfig
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentSearchBinding
import xevenition.com.runage.fragment.feed.FeedFragment
import xevenition.com.runage.fragment.history.HistoryFragment
import xevenition.com.runage.fragment.history.HistoryRecyclerAdapter
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject

class SearchFragment : BaseFragment<SearchViewModel>() {

    private lateinit var binding: FragmentSearchBinding

    private lateinit var searchRecyclerAdapter: SearchRecyclerAdapter
    private var loadMore = false

    // The AdLoader used to load ads.§
    private var adLoader: AdLoader? = null

    // List of native ads that have been successfully loaded.
    private val mNativeAds: MutableList<UnifiedNativeAd> = ArrayList()

    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Inject
    lateinit var runningUtil: RunningUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getApplication().appComponent.inject(this)
        val factory = SearchViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(SearchViewModel::class.java)

        loadNativeAds()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        searchRecyclerAdapter = SearchRecyclerAdapter(
            userId,
            resourceUtil,
            runningUtil,
            object : SearchRecyclerAdapter.OnClickListener {
                override fun onClick(quest: SavedQuest) {
                    (parentFragment as FeedFragment).onQuestClicked(quest)
                }

                override fun onReachedItem(position: Int) {
                    viewModel.onReachedItem(position)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val navController = (activity as MainActivity).findNavController(R.id.nav_host_tab_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar
            .setupWithNavController(navController, appBarConfiguration)

        return binding.root
    }

    private fun loadNativeAds() {
        val builder: AdLoader.Builder =  if (BuildConfig.DEBUG) {
            AdLoader.Builder(requireContext(), "ca-app-pub-3940256099942544/2247696110")
        } else {
            AdLoader.Builder(requireContext(), "ca-app-pub-9607319032304025/8633628403")
        }
        adLoader =
            builder.forUnifiedNativeAd { unifiedNativeAd -> // A native ad loaded successfully, check if the ad loader has finished loading
                // and if so, insert the ads into the list.
                mNativeAds.add(unifiedNativeAd)
                if (adLoader?.isLoading == false && mNativeAds.size > 0) {
                    viewModel.insertAdsInMenuItems(mNativeAds)
                }
            }.withAdListener(
                object : AdListener() {
                    @SuppressLint("BinaryOperationInTimber")
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Timber.e(
                            "The previous native ad failed to load. Attempting to"
                                    + " load another."
                        )
                        if (adLoader?.isLoading == false && mNativeAds.size > 0) {
                            viewModel.insertAdsInMenuItems(mNativeAds)
                        }
                    }
                }).build()

        // Load the Native Express ad.
        adLoader?.loadAds(AdRequest.Builder().build(), HistoryFragment.NUMBER_OF_ADS)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()
    }
}