package xevenition.com.runage.fragment.path

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentPathBinding
import xevenition.com.runage.util.BitmapUtil
import xevenition.com.runage.util.TypedValueUtil
import javax.inject.Inject


class PathFragment : BaseFragment<PathViewModel>() {

    private var polyLine: Polyline? = null
    private var googleMap: GoogleMap? = null
    private lateinit var binding: FragmentPathBinding

    @Inject
    lateinit var typedValueUtil: TypedValueUtil

    @Inject
    lateinit var bitmapUtil: BitmapUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        (requireActivity().applicationContext as MainApplication).appComponent.inject(this)
        val args = PathFragmentArgs.fromBundle(requireArguments())
        val factory = PathViewModelFactory(getApplication(), args)
        viewModel = ViewModelProvider(this, factory).get(PathViewModel::class.java)
        Timber.d("Viewmodel created!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_path, container, false)
        binding.mapView.onCreate(arguments)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.mapView.getMapAsync {
            googleMap = it
            viewModel.onMapCreated()
        }
        setUpObservables()
    }

    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableRunningPath.observe(viewLifecycleOwner, Observer {
            it?.let {
                setUpPolyline(it)
            }
        })

        viewModel.observableStartMarker.observe(viewLifecycleOwner, Observer {
            it?.let {
                drawStartMarker(it)
            }
        })

        viewModel.observableEndMarker.observe(viewLifecycleOwner, Observer {
            it?.let {
                drawEndMarker(it)
            }
        })
    }

    private fun drawStartMarker(it: LatLng) {
        val options = MarkerOptions().position(it)
            .anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmapUtil.getBitmapFromVectorDrawable(R.drawable.ic_dot_red)))
        googleMap?.addMarker(options)
    }

    private fun drawEndMarker(it: LatLng) {
        val options = MarkerOptions().position(it)
            .anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmapUtil.getBitmapFromVectorDrawable(R.drawable.ic_dot)))
        googleMap?.addMarker(options)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        binding.mapView.onStop()
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
        binding.mapView.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(): PathFragment {
            Timber.d("New instance of map fragment created")
            return PathFragment()
        }
    }

}
