package xevenition.com.runage.fragment.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.fragment.permission.PermissionViewModelFactory
import javax.inject.Inject

class StartFragment : BaseFragment<StartViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = StartViewModelFactory(getApplication())
        viewModel = ViewModelProviders.of(this, factory).get(StartViewModel::class.java)
    }

    companion object {
        fun newInstance() = StartFragment()
    }

}
