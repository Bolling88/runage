package xevenition.com.runage.fragment.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import xevenition.com.runage.ActivityBroadcastReceiver.Companion.KEY_EVENT_BROADCAST_ID
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.TypedValueUtil
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.databinding.FragmentMapBinding
import javax.inject.Inject


class MapFragment : BaseFragment<MapViewModel>() {

    private var polyLine: Polyline? = null
    private var userMarker: Marker? = null
    private var googleMap: GoogleMap? = null
    private lateinit var binding: FragmentMapBinding

    @Inject
    lateinit var factory: MapViewModelFactory
    @Inject
    lateinit var typedValueUtil: TypedValueUtil

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
                    userMarker = googleMap?.addMarker(options)
                }
            }
        })

        viewModel.observableRunningPath.observe(viewLifecycleOwner, Observer {
            it?.let {
                setUpPolyline(it)
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


    private fun setUpPolyline(positions: List<LatLng>) {
        if (positions.isEmpty() || googleMap == null) return

        moveCamera(positions)
        val positionPointsArray = ArrayList(positions)

        if (polyLine == null) { // Instantiates a new Polyline object and adds points to define a rectangle
            val rectOptions = PolylineOptions().addAll(positionPointsArray)
            // Get back the mutable Polyline
            polyLine = googleMap?.addPolyline(rectOptions)
            polyLine?.color = ContextCompat.getColor(context!!, R.color.colorAccent)
            polyLine?.width = typedValueUtil.dipToPixels(5f).toFloat()
        } else {
            polyLine?.points = positionPointsArray
        }
    }


    private fun moveCamera(coordinated: List<LatLng>) {
        if (googleMap == null) return
        try {
            if (coordinated.isNotEmpty()) {
                val builder = LatLngBounds.Builder()
                for (latLng in coordinated) {
                    builder.include(latLng)
                }
                val bounds = builder.build()
                val padding =
                    typedValueUtil.dipToPixels(50f) // offset from edges of the map in pixels
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                googleMap?.animateCamera(cu, 500, null)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
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

    fun onNewQuestCreated(id: Int) {
        viewModel.onNewQuestCreated(id)
    }

    companion object {
        fun newInstance() = MapFragment()
    }

}
