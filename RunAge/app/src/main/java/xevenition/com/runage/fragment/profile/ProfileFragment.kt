package xevenition.com.runage.fragment.profile

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionInflater
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentProfileBinding
import xevenition.com.runage.fragment.start.StartFragment
import xevenition.com.runage.util.GlideApp
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    private lateinit var args: ProfileFragmentArgs
    private lateinit var binding: FragmentProfileBinding
    private var storageRef = Firebase.storage.reference

    @Inject
    lateinit var resourceUtil: ResourceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        args = ProfileFragmentArgs.fromBundle(requireArguments())
        val factory = ProfileViewModelFactory(getApplication(), args)
        viewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.textLevel.text = args.keyUserLevel
        binding.textName.text = args.keyUserName

        val navController = (activity as MainActivity).findNavController(R.id.nav_host_tab_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar
            .setupWithNavController(navController, appBarConfiguration)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    // General activity result contract
    private val openPostActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
            }
        }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableProfileImage.observe(viewLifecycleOwner, {
            it?.let {
                val profileImageRef = storageRef.child("images/${it}.jpg")
                Timber.d("Loading player image")
                GlideApp.with(requireContext())
                    .load(profileImageRef)
                    .placeholder(resourceUtil.getDrawable(R.drawable.ic_profile))
                    .fallback(resourceUtil.getDrawable(R.drawable.ic_profile))
                    .into(binding.imgProfile)
            }
        })

        viewModel.observableLevel.observe(viewLifecycleOwner, {
            it?.let {
                binding.textLevel.text = it
            }
        })

        viewModel.observableEditProfile.observe(viewLifecycleOwner, {
            it?.let {
//                openPostActivity.launch(
//                  it
//                )
            }
        })
    }
}
