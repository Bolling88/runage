package xevenition.com.runage.fragment.gifted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentGiftedBinding
import xevenition.com.runage.databinding.FragmentRuleBinding
import xevenition.com.runage.fragment.rule.RuleViewModel
import xevenition.com.runage.fragment.rule.RuleViewModelFactory

class GiftedFragment : BaseFragment<GiftedViewModel>() {

    private lateinit var binding: FragmentGiftedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = GiftedViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(GiftedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gifted, container, false)
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
