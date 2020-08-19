package xevenition.com.runage.fragment.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import xevenition.com.runage.BuildConfig
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentHistoryBinding
import xevenition.com.runage.fragment.feed.FeedFragment
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject


class HistoryFragment : BaseFragment<HistoryViewModel>() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyRecyclerAdapter: HistoryRecyclerAdapter
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
        val factory = HistoryViewModelFactory(getApplication(), requireArguments())
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        loadNativeAds()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        historyRecyclerAdapter = HistoryRecyclerAdapter(
            userId,
            resourceUtil,
            runningUtil,
            object : HistoryRecyclerAdapter.OnClickListener {
                override fun onClick(quest: SavedQuest) {
                    (parentFragment as FeedFragment).onQuestClicked(quest)
                }

                override fun onReachedItem(position: Int) {
                    viewModel.onReachedItem(position)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = historyRecyclerAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableQuest.observe(viewLifecycleOwner, Observer {
            it?.let {
                loadMore = false
                historyRecyclerAdapter.submitList(it)
            }
        })
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

        // Load the Native Express ad.
        adLoader?.loadAds(AdRequest.Builder().build(), NUMBER_OF_ADS)
    }

    companion object {

        const val PAGE_MINE = 0
        const val PAGE_FOLLOWING = 1
        const val PAGE_ALL = 2

        const val NUMBER_OF_ADS = 5

        const val KEY_PAGE = "key_page"

        fun getInstance(position: Int): HistoryFragment {
            val args = Bundle()
            args.putInt(KEY_PAGE, position)
            val fragment = HistoryFragment()
            fragment.arguments = args
            return fragment
        }
    }
}