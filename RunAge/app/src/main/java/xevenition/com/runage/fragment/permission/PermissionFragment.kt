package xevenition.com.runage.fragment.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.databinding.FragmentPermissionBinding
import xevenition.com.runage.fragment.settings.SettingsFragment
import xevenition.com.runage.fragment.settings.SettingsViewModel
import xevenition.com.runage.fragment.settings.SettingsViewModelFactory
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
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableCheckPermissionActivity.observe(viewLifecycleOwner, Observer {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                binding.switchActivity.isChecked = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            PERMISSIONS_REQUEST_ACTIVITY
                    )
                } else {
                    //earlier version is enough with manifest permission
                    binding.switchActivity.isChecked = true
                }
            } else {
                binding.switchActivity.isChecked = true
            }
        })

        viewModel.observableCheckPermissionLocation.observe(viewLifecycleOwner, Observer {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                binding.switchLocation.isChecked = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            PERMISSIONS_REQUEST_LOCATION
                    )
                } else {
                    //earlier version is enough with manifest permission
                    requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSIONS_REQUEST_LOCATION
                    )
                }
            } else {
                binding.switchLocation.isChecked = true
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
        }
    }

    private fun permissionGranted(grantResults: IntArray) =
            (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)

    companion object {
        fun newInstance() = SettingsFragment()
        const val PERMISSIONS_REQUEST_ACTIVITY = 1001
        const val PERMISSIONS_REQUEST_LOCATION = 1002
    }
}
