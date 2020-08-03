package xevenition.com.runage.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentHistoryBinding
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
        val factory = HistoryViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        historyRecyclerAdapter = HistoryRecyclerAdapter(resourceUtil, runningUtil, object: HistoryRecyclerAdapter.OnClickListener{
            override fun onClick(quest: SavedQuest) {
                viewModel.onQuestClicked(quest)
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
        fun getInstance(position: Int): HistoryFragment{
            return HistoryFragment()
        }
    }
}