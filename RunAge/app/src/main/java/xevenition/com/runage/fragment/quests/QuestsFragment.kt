package xevenition.com.runage.fragment.quests

import android.graphics.Rect
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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import timber.log.Timber
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentQuestsBinding
import xevenition.com.runage.model.Challenge
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject


class QuestsFragment : BaseFragment<QuestsViewModel>() {

    private lateinit var binding: FragmentQuestsBinding
    private lateinit var challengeListRecyclerAdapter: QuestsRecyclerAdapter

    @Inject
    lateinit var resourceUtil: ResourceUtil

    @Inject
    lateinit var runningUtil: RunningUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getApplication().appComponent.inject(this)
//        requireActivity().onBackPressedDispatcher.addCallback(this) {
//            activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.mainNavigation)
//        }
        val factory = QuestsViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(QuestsViewModel::class.java)

        challengeListRecyclerAdapter =
            QuestsRecyclerAdapter(resourceUtil, object : QuestsRecyclerAdapter.OnClickListener {
                override fun onClick(challenge: Challenge, isLocked: Boolean) {
                    Timber.d("Click")
                    if (!isLocked) {
                        viewModel.onChallengeClicked(challenge)
                    }else{
                        Timber.d("Is locked!")
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quests, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = challengeListRecyclerAdapter

        binding.recyclerView.addItemDecoration(SpacesItemDecoration(0))

        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openMenu()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.unlockDrawer()
    }

    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableChallenges.observe(viewLifecycleOwner, Observer {
            it?.let {
                challengeListRecyclerAdapter.setItems(it)
            }
        })
    }
}

class SpacesItemDecoration(private val space: Int) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(space, space, space, space)
    }
}