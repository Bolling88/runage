package xevenition.com.runage.fragment.xp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentXpBinding

class XpFragment : BaseFragment<XpViewModel>() {

    private lateinit var binding: FragmentXpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = XpViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(XpViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_xp, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

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
