package xevenition.com.runage.fragment.challengelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentChallengeListBinding
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject


class ChallengeListFragment : BaseFragment<ChallengeListViewModel>() {

    private lateinit var binding: FragmentChallengeListBinding
    private lateinit var challengeListRecyclerAdapter: ChallengeListRecyclerAdapter

    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var runningUtil: RunningUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getApplication().appComponent.inject(this)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.mainNavigation)
        }
        val factory = ChallengeListViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(ChallengeListViewModel::class.java)

        challengeListRecyclerAdapter = ChallengeListRecyclerAdapter(resourceUtil, runningUtil, object: ChallengeListRecyclerAdapter.OnClickListener{
            override fun onClick(challenge: Challenge) {
                viewModel.onChallengeClicked(challenge)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_challenge_list, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = challengeListRecyclerAdapter

        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openMenu()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableChallenges.observe(viewLifecycleOwner, Observer {
            it?.let {
                challengeListRecyclerAdapter.submitList(it)
            }
        })
    }
}