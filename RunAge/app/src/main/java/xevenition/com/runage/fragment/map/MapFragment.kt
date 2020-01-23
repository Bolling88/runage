package xevenition.com.runage.fragment.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.TypedValueUtil
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.databinding.FragmentMapBinding
import javax.inject.Inject


class MapFragment : BaseFragment<MapViewModel>() {

    private var currentQuestId: Int = -1
    private var polyLine: Polyline? = null
    private var userMarker: Marker? = null
    private var googleMap: GoogleMap? = null
    private lateinit var binding: FragmentMapBinding

    @Inject
    lateinit var factory: MapViewModelFactory
    @Inject
    lateinit var typedValueUtil: TypedValueUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as MainApplication).appComponent.inject(this)
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
        if(currentQuestId != -1) viewModel.onNewQuestCreated(currentQuestId)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.mapView.getMapAsync {
            googleMap = it
            viewModel.onMapCreated()
            //googleMap?.uiSettings?.setAllGesturesEnabled(false)
        }
        setUpObservables()
    }

    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableAnimateMapPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                googleMap?.animateCamera(it)
            }
        })

        viewModel.observableUserMarkerPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                drawUserMarker(it)
            }
        })

        viewModel.observableClearMap.observe(viewLifecycleOwner, Observer {
            googleMap?.clear()
        })

        viewModel.observableRunningPath.observe(viewLifecycleOwner, Observer {
            it?.let {
                setUpPolyline(it)
            }
        })
    }

    private fun drawUserMarker(it: LatLng) {
        if (userMarker != null) {
            userMarker?.position = it
        } else {
            val options = MarkerOptions().position(it)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location))
            userMarker = googleMap?.addMarker(options)
        }
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
    }


    private fun setUpPolyline(positions: List<LatLng>) {
        if (positions.isEmpty() || googleMap == null) return

        moveCamera(positions)
        val positionPointsArray = ArrayList(positions)

        if (polyLine == null) { // Instantiates a new Polyline object and adds points to define a rectangle
            val rectOptions = PolylineOptions().addAll(positionPointsArray)
                .jointType(JointType.ROUND)
            // Get back the mutable Polyline
            polyLine = googleMap?.addPolyline(rectOptions)
            polyLine?.color = ContextCompat.getColor(context!!, R.color.colorAccent)
            polyLine?.width = typedValueUtil.dipToPixels(5f).toFloat()
        }
        polyLine?.points = positionPointsArray
        Timber.d("Polyline has ${polyLine?.points?.size} points")
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
        try {
            viewModel.onNewQuestCreated(id)
        }catch(e: Exception){
            //viewModel not initialised
            currentQuestId = id
        }
    }

    fun onQuestFinished() {
        viewModel.onQuestFinished()
        googleMap?.clear()
        userMarker = null
        polyLine = null
        currentQuestId = -1
    }

    companion object {
        fun newInstance() = MapFragment()
    }

}
