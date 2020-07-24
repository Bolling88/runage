package xevenition.com.runage.fragment.map

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.util.TypedValueUtil
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentMapBinding
import xevenition.com.runage.fragment.main.MainFragment
import xevenition.com.runage.service.EventService
import xevenition.com.runage.util.BitmapUtil
import java.util.logging.Logger
import javax.inject.Inject


class MapFragment : BaseFragment<MapViewModel>() {

    private var backPressCallback: OnBackPressedCallback? = null
    private var currentQuestId: Int = -1
    private var polyLine: Polyline? = null
    private var userMarker: Marker? = null
    private var startMarker: Marker? = null
    private var googleMap: GoogleMap? = null
    private lateinit var binding: FragmentMapBinding
    private lateinit var mService: EventService
    private var mBound: Boolean = false

    @Inject
    lateinit var typedValueUtil: TypedValueUtil

    @Inject
    lateinit var bitmapUtil: BitmapUtil

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as EventService.LocalBinder
            mService = binder.getService()
            mBound = true
            mService.registerCallback(object : EventService.EventCallback {
                override fun onQuestCreated(id: Int) {
                    currentQuestId = id
                    backPressCallback?.isEnabled = true
                    Timber.d("onQuestCreated: $id")
                    onNewQuestCreated(id)
                }
            })
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        backPressCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
                requireActivity().finish()
        }
        backPressCallback?.isEnabled = false
        (requireActivity().applicationContext as MainApplication).appComponent.inject(this)
        val factory = MapViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(MapViewModel::class.java)
        Timber.d("Viewmodel created!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        binding.mapView.onCreate(arguments)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        if (currentQuestId != -1) viewModel.onNewQuestCreated(currentQuestId)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.mapView.getMapAsync {
            googleMap = it
            viewModel.onMapCreated()
            //googleMap?.uiSettings?.setAllGesturesEnabled(false)
        }
        setUpObservables()
        if (MainApplication.serviceIsRunning) {
            binding.lottieCountDown.visibility = View.GONE
            binding.lottieCountDown.pauseAnimation()
        } else {
            binding.lottieCountDown.visibility = View.VISIBLE
            binding.lottieCountDown.playAnimation()
        }
        startEventService()
    }

    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableAnimateMapPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                //only move camera for user position if we don't have a polyline to focus on
                if (polyLine == null || polyLine?.points?.isEmpty() == true)
                    googleMap?.animateCamera(it)
            }
        })

        viewModel.observableUserMarkerPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                drawUserMarker(it)
            }
        })

        viewModel.observableStartMarkerPosition.observe(viewLifecycleOwner, Observer {
            it?.let {
                drawStartMarker(it)
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

        viewModel.observableStopRun.observe(viewLifecycleOwner, Observer {
            stopEventService()
        })
    }

    private fun drawStartMarker(it: LatLng) {
        if (startMarker != null) {
            startMarker?.position = it
        } else {
            val options = MarkerOptions().position(it)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapUtil.getBitmapFromVectorDrawable(R.drawable.ic_dot_red)))
            startMarker = googleMap?.addMarker(options)
        }
    }

    private fun drawUserMarker(it: LatLng) {
        if (userMarker != null) {
            userMarker?.position = it
        } else {
            val options = MarkerOptions().position(it)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapUtil.getBitmapFromVectorDrawable(R.drawable.ic_dot)))
            userMarker = googleMap?.addMarker(options)
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        binding.mapView.onStart()
        if (MainApplication.serviceIsRunning) {
            bindToService()
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        binding.mapView.onStop()
        unbindService()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        binding.mapView.onDestroy()
    }

    private fun bindToService() {
        Intent(activity, EventService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        if (mBound) {
            activity?.unbindService(connection)
            mBound = false
        }
    }

    private fun startEventService() {
        val myService = Intent(activity, EventService::class.java)
        ContextCompat.startForegroundService(requireContext(), myService)
        bindToService()
    }

    private fun stopEventService() {
        unbindService()
        val myService = Intent(activity, EventService::class.java)
        activity?.stopService(myService)
    }

    private fun setUpPolyline(positions: List<LatLng>) {
        if (positions.isEmpty() || googleMap == null) return

        moveCamera(positions)
        val positionPointsArray = ArrayList(positions)

        if (polyLine == null) {
            val polylineOptions = PolylineOptions().addAll(positionPointsArray)
                .jointType(JointType.ROUND)
                .color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                .width(typedValueUtil.dipToPixels(5f).toFloat())
            polyLine = googleMap?.addPolyline(polylineOptions)
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
                val padding = typedValueUtil.dipToPixels(50f)
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
        try {
            binding.mapView.onSaveInstanceState(outState)
        } catch (exception: Exception) {
            Timber.d(exception)
        }
    }

    fun onNewQuestCreated(id: Int) {
        try {
            viewModel.onNewQuestCreated(id)
        } catch (e: Exception) {
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
        fun newInstance(): MapFragment {
            Timber.d("New instance of map fragment created")
            return MapFragment()
        }
    }

}
