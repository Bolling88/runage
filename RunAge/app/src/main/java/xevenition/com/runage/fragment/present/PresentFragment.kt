package xevenition.com.runage.fragment.present

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import timber.log.Timber
import xevenition.com.runage.BuildConfig
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentPresentBinding

class PresentFragment : BaseFragment<PresentViewModel>() {

    private lateinit var binding: FragmentPresentBinding
    private lateinit var rewardedAd: RewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = PresentViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(PresentViewModel::class.java)

        createAndLoadRewardAd()
    }

    private fun createAndLoadRewardAd() {
        viewModel.onAdLoading()
        rewardedAd = if (BuildConfig.DEBUG) {
            RewardedAd(
                requireActivity(),
                "ca-app-pub-3940256099942544/5224354917"
            )
        } else {
            RewardedAd(
                requireActivity(),
                "ca-app-pub-9607319032304025/6199036750"
            )
        }

        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                Timber.d("Reward ad loaded")
                viewModel.onRewardedAdLoaded()
            }

            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                Timber.e(adError.message)
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_present, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableShowAdd.observe(viewLifecycleOwner, Observer {
            showAd()
        })
    }

    private fun showAd(){
        if (rewardedAd.isLoaded) {
            val adCallback = object: RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    Timber.d("onRewardedAdOpened")
                }
                override fun onRewardedAdClosed() {
                    Timber.d("onRewardedAdClosed")
                    createAndLoadRewardAd()
                }
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    Timber.d("onUserEarnedReward")
                    viewModel.onUserEarnedReward()
                }
                override fun onRewardedAdFailedToShow(adError: AdError) {
                    Timber.e(adError.message)
                }
            }
            rewardedAd.show(requireActivity(), adCallback)
        }
        else {
            Timber.d("The rewarded ad wasn't loaded yet.")
        }
    }
}