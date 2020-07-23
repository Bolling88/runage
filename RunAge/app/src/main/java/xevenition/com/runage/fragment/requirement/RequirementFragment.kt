package xevenition.com.runage.fragment.requirement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.images.ImageManager
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentRequirementBinding
import xevenition.com.runage.fragment.start.StartFragment
import xevenition.com.runage.fragment.summary.SummaryFragmentArgs
import xevenition.com.runage.model.Challenge

class RequirementFragment : BaseFragment<RequirementViewModel>() {

    private lateinit var binding: FragmentRequirementBinding

    private val navController by lazy {
        Navigation.findNavController(requireActivity(), R.id.nav_host_tab_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = RequirementFragmentArgs.fromBundle(requireArguments())
        val factory = RequirementViewModelFactory(getApplication(), args.keyChallenge!!)
        viewModel = ViewModelProvider(this, factory).get(RequirementViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_requirement, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    override fun setUpObservables() {
        super.setUpObservables()
    }
}