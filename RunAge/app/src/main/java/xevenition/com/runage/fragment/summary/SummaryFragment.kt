package xevenition.com.runage.fragment.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentSummaryBinding

class SummaryFragment : BaseFragment<SummaryViewModel>() {

    private lateinit var binding: FragmentSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = SummaryFragmentArgs.fromBundle(requireArguments())
        val factory = SummaryViewModelFactory(getApplication(), args)
        viewModel = ViewModelProvider(this, factory).get(SummaryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_summary, container, false)
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

    companion object {
        const val KEY_QUEST_ID = "KEY_QUEST_ID"

        fun newInstance(questId: Int): SummaryFragment {
            val fragment = SummaryFragment()
            val arguments = Bundle()
            arguments.putInt(KEY_QUEST_ID, questId)
            fragment.arguments = arguments
            return fragment
        }
    }
}