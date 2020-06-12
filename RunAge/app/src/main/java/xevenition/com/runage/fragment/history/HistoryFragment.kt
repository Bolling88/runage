package xevenition.com.runage.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentHistoryBinding
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject

class HistoryFragment : BaseFragment<HistoryViewModel>() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyRecyclerAdapter: HistoryRecyclerAdapter

    @Inject
    lateinit var resourceUtil: ResourceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getApplication().appComponent.inject(this)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
        val factory = HistoryViewModelFactory(getApplication())
        historyRecyclerAdapter = HistoryRecyclerAdapter(resourceUtil)
        viewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)
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

        viewModel.observableQuest.observe(viewLifecycleOwner, Observer {
            it?.let {
                historyRecyclerAdapter.submitList(it)
            }
        })
    }
}