package xevenition.com.runage.fragment.appsettings

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

class AppSettingsFragment : BaseFragment<AppSettingsViewModel>() {

    private lateinit var binding: FragmentAppSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.mainNavigation)
        }
        val factory = AppSettingsViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(AppSettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_settings, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openMenu()
        }

        binding.textWeightField.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               viewModel.onWeightTextChanged(s.toString())
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableWeight.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.textWeightField.setText(it.toString())
            }
        })

        viewModel.observableUnitType.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(it) {
                    binding.radioMetric.isChecked = true
                    binding.radioImperial.isChecked = false
                }else{
                    binding.radioMetric.isChecked = false
                    binding.radioImperial.isChecked = true
                }
            }
        })
    }
}
