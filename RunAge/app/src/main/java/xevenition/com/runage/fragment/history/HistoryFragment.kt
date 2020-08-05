package xevenition.com.runage.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentHistoryBinding
import xevenition.com.runage.fragment.feed.FeedFragment
import xevenition.com.runage.model.SavedQuest
import xevenition.com.runage.util.ResourceUtil
import xevenition.com.runage.util.RunningUtil
import javax.inject.Inject


class HistoryFragment : BaseFragment<HistoryViewModel>() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyRecyclerAdapter: HistoryRecyclerAdapter
    private var loadMore = false

    @Inject
    lateinit var resourceUtil: ResourceUtil
    @Inject
    lateinit var runningUtil: RunningUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getApplication().appComponent.inject(this)
        val factory = HistoryViewModelFactory(getApplication(), requireArguments())
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        historyRecyclerAdapter = HistoryRecyclerAdapter(userId, resourceUtil, runningUtil, object: HistoryRecyclerAdapter.OnClickListener{
            override fun onClick(quest: SavedQuest) {
                (parentFragment as FeedFragment).onQuestClicked(quest)
            }

            override fun onReachedItem(position: Int) {
                viewModel.onReachedItem(position)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = historyRecyclerAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableQuest.observe(viewLifecycleOwner, Observer {
            it?.let {
                loadMore = false
                historyRecyclerAdapter.submitList(it)
            }
        })
    }

    companion object{

        const val PAGE_MINE = 0
        const val PAGE_FOLLOWING = 1
        const val PAGE_ALL = 2

        const val KEY_PAGE = "key_page"

        fun getInstance(position: Int): HistoryFragment{
            val args = Bundle()
            args.putInt(KEY_PAGE, position)
            val fragment = HistoryFragment()
            fragment.arguments = args
            return fragment
        }
    }
}