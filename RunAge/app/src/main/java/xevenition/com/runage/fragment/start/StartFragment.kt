package xevenition.com.runage.fragment.start

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.images.ImageManager
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentStartBinding
import xevenition.com.runage.dialog.CommenceRateDialogFragment
import xevenition.com.runage.dialog.FeedbackDialogFragment
import xevenition.com.runage.dialog.RateDialogFragment
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject


class StartFragment : BaseFragment<StartViewModel>(), RateDialogFragment.RateDialogCallback,
    FeedbackDialogFragment.FeedbackDialogCallback,
    CommenceRateDialogFragment.CommenceRateDialogCallback {

    private lateinit var binding: FragmentStartBinding

    @Inject
    lateinit var resourceUtil: ResourceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)
        val factory = StartViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(StartViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openMenu()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.unlockDrawer()
    }

    override fun setUpObservables() {
        super.setUpObservables()
        viewModel.observableProfileImage.observe(viewLifecycleOwner, Observer {
            it?.let {
                val manager = ImageManager.create(requireContext())
                manager.loadImage(binding.imgProfile, it)
            }
        })

        viewModel.observableOpenMenu.observe(viewLifecycleOwner, Observer {
            (activity as? MainActivity)?.openMenu()
        })

        viewModel.observableShowAchievements.observe(viewLifecycleOwner, Observer {
                startActivityForResult(it, PROFILE_REQUEST_CODE)
        })

        viewModel.observableShowRateDialog.observe(viewLifecycleOwner, Observer {
            RateDialogFragment.newInstance().show(childFragmentManager, "dialog_fragment")
        })
    }

    override fun onLikeClicked() {
        CommenceRateDialogFragment.newInstance().show(childFragmentManager, "dialog_fragment")
    }

    override fun onDislikeClicked() {
        viewModel.onDislikeClicked()
        FeedbackDialogFragment.newInstance().show(childFragmentManager, "dialog_fragment")
    }

    override fun onRateClicked() {
        viewModel.onRateClicked()
        val uri: Uri = Uri.parse("market://details?id=" + requireContext().packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + requireContext().packageName)
                )
            )
        }
    }

    override fun onLaterClicked() {
        viewModel.onRateLaterClicked()
    }

    override fun onFeedbackClicked() {
        Timber.d("Sending email...")
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "xevenition@gmail.com", null
            )
        )
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RunAge Support")
        startActivity(Intent.createChooser(emailIntent, resourceUtil.getString(R.string.runage_send_email)))
    }

    companion object {
        fun newInstance() = StartFragment()
        const val PROFILE_REQUEST_CODE = 23423
    }

}
