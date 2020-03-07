package xevenition.com.runage.fragment.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.images.ImageManager
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentStartBinding

class StartFragment : BaseFragment<StartViewModel>() {

    private lateinit var binding: FragmentStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)
        val factory = StartViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(StartViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    override fun setUpObservables() {
        super.setUpObservables()
        viewModel.observableProfileImage.observe(viewLifecycleOwner, Observer {
            it?.let {
                val manager = ImageManager.create(requireContext())
                manager.loadImage(binding.imgProfile, it)
            }
        })
    }

    companion object {
        fun newInstance() = StartFragment()
    }

}
