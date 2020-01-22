package xevenition.com.runage.fragment.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.databinding.FragmentSplashBinding
import javax.inject.Inject


class SplashFragment : BaseFragment<SplashViewModel>() {

    private lateinit var binding: FragmentSplashBinding

    @Inject
    lateinit var factory: SplashViewModelFactory

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
        viewModel = ViewModelProviders.of(this, factory).get(SplashViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setUpObservables()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()
    }

    companion object {
        fun newInstance() = SplashFragment()
    }
}
