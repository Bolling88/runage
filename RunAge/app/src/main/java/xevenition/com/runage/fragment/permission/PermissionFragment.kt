package xevenition.com.runage.fragment.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.databinding.FragmentPermissionBinding
import javax.inject.Inject


class PermissionFragment : BaseFragment<PermissionViewModel>() {

    private lateinit var binding: FragmentPermissionBinding

    @Inject
    lateinit var factory: PermissionViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.applicationContext as MainApplication).appComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_permission, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(PermissionViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setUpObservables()
        //checkPermissions()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableCheckPermissionActivity.observe(viewLifecycleOwner, Observer {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                        MainActivity.MY_PERMISSIONS_REQUEST
                )
            } else {
                binding.switchActivity.isChecked = true
            }
        })
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACTIVITY -> {
                binding.switchActivity.isChecked = permissionGranted(grantResults)
            }
            PERMISSIONS_REQUEST_LOCATION -> {
                binding.switchLocation.isChecked = permissionGranted(grantResults)
            }
            PERMISSIONS_REQUEST_BACKGROUND -> {
                //binding.switchLocation.isChecked = permissionGranted(grantResults)
            }
        }
    }

    private fun permissionGranted(grantResults: IntArray) =
            (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)

    companion object {
        fun newInstance() = PermissionFragment()
        const val PERMISSIONS_REQUEST_ACTIVITY = 1001
        const val PERMISSIONS_REQUEST_LOCATION = 1002
        const val PERMISSIONS_REQUEST_BACKGROUND = 1003
    }
}
