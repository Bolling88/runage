package xevenition.com.runage.fragment.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentPlayerBinding
import xevenition.com.runage.util.GlideApp
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class PlayerFragment : BaseFragment<PlayerViewModel>() {

    private lateinit var binding: FragmentPlayerBinding
    private var storageRef = Firebase.storage.reference

    @Inject
    lateinit var resourceUtil: ResourceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)
        val factory = PlayerViewModelFactory(getApplication(), PlayerFragmentArgs.fromBundle(requireArguments()))
        viewModel = ViewModelProvider(this, factory).get(PlayerViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

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

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableImage.observe(viewLifecycleOwner, {
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
    }
}