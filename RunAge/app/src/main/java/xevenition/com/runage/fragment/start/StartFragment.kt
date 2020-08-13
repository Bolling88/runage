package xevenition.com.runage.fragment.start

import android.animation.Animator
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
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
        viewModel = ViewModelProvider(requireActivity(), factory).get(StartViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setOnMenuItemClickListener {
            launchMusicIntent()
            false
        }

        if (!isPackageInstalled("com.spotify.music", requireContext().packageManager))
            binding.toolbar.menu.findItem(R.id.action_music).isVisible = false

        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openMenu()
        }

        viewModel.onViewResumed()

        return binding.root
    }

    private fun launchMusicIntent() {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            if (isPackageInstalled("com.spotify.music", requireContext().packageManager)) {
                intent.component =
                    ComponentName("com.spotify.music", "com.spotify.music.MainActivity")
            } else {
                intent.setDataAndType(Uri.parse("http://"), "audio/*")
            }
            startActivity(intent)
        } catch (error: Exception) {
            Timber.e(error)
        }
    }

    private fun isPackageInstalled(
        packageName: String,
        packageManager: PackageManager
    ): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
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
                displayProfileImageAndRetrieveIt(it)
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

        viewModel.observableShowProfile.observe(viewLifecycleOwner, Observer {
            it?.let {
                showProfile(it)
            }
        })

        viewModel.observableShowLevelUp.observe(viewLifecycleOwner, Observer {
            binding.lottieLevelUp.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    binding.lottieLevelUp.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator?) {
                    binding.lottieLevelUp.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
            binding.lottieLevelUp.playAnimation()
        })

        viewModel.observableLevelProgress.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.circularProgressBar.setProgressWithAnimation(it, 1000)
            }
        })
    }

    private fun showProfile(args: Bundle) {
        val extras = FragmentNavigatorExtras(
            binding.cardProfile to "imageView",
            binding.textName to "name",
            binding.textLevel to "level"
        )
        findNavController().navigate(
            R.id.action_startFragment_to_profileFragment,
            args,
            null,
            extras
        )
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

    private fun displayProfileImageAndRetrieveIt(uri: Uri) {
        val manager = ImageManager.create(requireContext())
        val listener =
            ImageManager.OnImageLoadedListener { uri: Uri, drawable: Drawable, b: Boolean ->
                binding.imgProfile.setImageDrawable(drawable)
                val bitmap = drawable.toBitmap()
                //val bitmap = (binding.imgProfile.drawable as BitmapDrawable).bitmap
                viewModel.onProfileImageRetrieved(bitmap)
            }
        manager.loadImage(listener, uri, 0)
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
        startActivity(
            Intent.createChooser(
                emailIntent,
                resourceUtil.getString(R.string.runage_send_email)
            )
        )
    }

    companion object {
        fun newInstance() = StartFragment()
        const val PROFILE_REQUEST_CODE = 23423
    }

}
