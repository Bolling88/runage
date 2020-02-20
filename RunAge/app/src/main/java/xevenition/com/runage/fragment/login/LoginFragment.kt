package xevenition.com.runage.fragment.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentLoginBinding
import xevenition.com.runage.databinding.FragmentSettingsBinding
import xevenition.com.runage.fragment.settings.SettingsFragment
import xevenition.com.runage.fragment.settings.SettingsViewModel
import xevenition.com.runage.fragment.settings.SettingsViewModelFactory

class LoginFragment : BaseFragment<LoginViewModel>() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = LoginViewModelFactory(getApplication())
        viewModel = ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setUpObservables()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }

}