package xevenition.com.runage.fragment.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.subjects.BehaviorSubject
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_EVENT_BROADCAST_ID
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.databinding.FragmentMapBinding
import javax.inject.Inject


class MapFragment : BaseFragment<MapViewModel>() {

    private var userMarker: Marker? = null
    private var googleMap: GoogleMap? = null
    private lateinit var binding: FragmentMapBinding

    @Inject
    lateinit var factory: MapViewModelFactory

    private val currentActivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(KEY_EVENT_BROADCAST_ID)) {
                viewModel.onUserEventChanged(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as MainApplication).appComponent.inject(this)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            currentActivityReceiver, IntentFilter(KEY_EVENT_BROADCAST_ID)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        binding.mapView.onCreate(arguments)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(MapViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.mapView.getMapAsync {
            googleMap = it
            googleMap?.uiSettings?.setAllGesturesEnabled(false)
        }

        viewModel.observableAnimateMapPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                googleMap?.animateCamera(it)
            }
        })

        viewModel.observableUserMarkerPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (userMarker != null) {
                    userMarker?.position = it
                } else {
                    val options = MarkerOptions().position(it)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location))
                    userMarker = googleMap?.addMarker(options);
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(currentActivityReceiver)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    fun passLocationUpdatesObservable(observable: BehaviorSubject<LocationResult>) {
        viewModel.passLocationUpdatesObservable(observable)
    }

    companion object {
        fun newInstance() = MapFragment()
    }

}
