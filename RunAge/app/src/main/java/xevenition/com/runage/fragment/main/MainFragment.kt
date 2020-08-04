package xevenition.com.runage.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentMainBinding


class MainFragment : BaseFragment<MainViewModel>() {

    private lateinit var binding: FragmentMainBinding

    private val navController by lazy {
        Navigation.findNavController(requireActivity(), R.id.nav_host_tab_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }

        (activity?.applicationContext as MainApplication).appComponent.inject(this)

        val factory = MainViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
        Timber.d("onViewCreated")

        binding.bottomNavigation.selectedItemId = R.id.startFragment
        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mapFragment -> hideBottomNav()
                R.id.requirementFragment -> hideBottomNav()
                R.id.startFragment -> showBottomNav()
                R.id.challengeFragment -> showBottomNav()
                R.id.feedFragment -> showBottomNav()
                R.id.historySummaryFragment -> hideBottomNav()
                else -> {}
            }
        }
    }

    private fun hideBottomNav() {
        binding.bottomNavigation.visibility = View.GONE
    }

    private fun showBottomNav() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.unlockDrawer()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        viewModel.onResume()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableUserAccount.observe(viewLifecycleOwner, Observer {
            it?.let {
                (activity as? MainActivity)?.setUpAchievementsLayout(it)
            }
        })
    }
}
