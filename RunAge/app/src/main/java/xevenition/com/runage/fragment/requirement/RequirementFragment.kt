package xevenition.com.runage.fragment.requirement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.images.ImageManager
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentRequirementBinding
import xevenition.com.runage.fragment.start.StartFragment
import xevenition.com.runage.model.Challenge

class RequirementFragment : BaseFragment<RequirementViewModel>() {

    private lateinit var binding: FragmentRequirementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val challenge = arguments?.getParcelable<Challenge>(KEY_CHALLENGE)
        val factory = RequirementViewModelFactory(getApplication(), challenge!!)
        viewModel = ViewModelProvider(this, factory).get(RequirementViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_requirement, container, false)
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
    }

    companion object {
        const val KEY_CHALLENGE = "KEY_CHALLENGE"

        fun newInstance(challenge: Challenge): RequirementFragment {
            val bundle = Bundle()
            bundle.putParcelable(KEY_CHALLENGE, challenge)
            val fragment = RequirementFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}