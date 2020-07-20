package xevenition.com.runage.fragment.support

import xevenition.com.runage.fragment.appsettings.AppSettingsViewModel
import xevenition.com.runage.fragment.appsettings.AppSettingsViewModelFactory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentAppSettingsBinding
import xevenition.com.runage.databinding.FragmentSettingsBinding
import xevenition.com.runage.databinding.FragmentSupportBinding
import xevenition.com.runage.fragment.start.StartFragment

class SupportFragment : BaseFragment<SupportViewModel>() {

    private lateinit var binding: FragmentSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.mainNavigation)
        }
        val factory = SupportViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(SupportViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_support, container, false)
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

    @Override
    override fun setUpObservables() {
        super.setUpObservables()
    }
}
