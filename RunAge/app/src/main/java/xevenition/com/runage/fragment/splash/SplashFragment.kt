package xevenition.com.runage.fragment.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentSplashBinding
import javax.inject.Inject


class SplashFragment : BaseFragment<SplashViewModel>() {

    private lateinit var binding: FragmentSplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = SplashViewModelFactory(getApplication())
        viewModel = ViewModelProviders.of(this, factory).get(SplashViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setUpObservables()

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.permissionsGranted(false)
        } else {
            viewModel.permissionsGranted(true)
        }
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()
    }

    companion object {
        fun newInstance() = SplashFragment()
    }
}
