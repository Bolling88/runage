package xevenition.com.runage.fragment.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import timber.log.Timber
import xevenition.com.runage.BuildConfig
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentSummaryBinding
import xevenition.com.runage.util.BitmapUtil
import xevenition.com.runage.util.TypedValueUtil
import javax.inject.Inject


class SummaryFragment : BaseFragment<SummaryViewModel>() {

    private lateinit var binding: FragmentSummaryBinding
    private var googleMap: GoogleMap? = null
    private lateinit var mInterstitialAd: InterstitialAd

    @Inject
    lateinit var typedValueUtil: TypedValueUtil

    @Inject
    lateinit var bitmapUtil: BitmapUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as MainApplication).appComponent.inject(this)
        val args = SummaryFragmentArgs.fromBundle(requireArguments())
        val factory = SummaryViewModelFactory(getApplication(), args)
        viewModel = ViewModelProvider(this, factory).get(SummaryViewModel::class.java)

        mInterstitialAd = InterstitialAd(requireContext())
        if (BuildConfig.DEBUG) {
            mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        }else{
            mInterstitialAd.adUnitId = "ca-app-pub-5287847424239288/7381115318"
        }
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_summary, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
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
            googleMap?.uiSettings?.setAllGesturesEnabled(false)
        }

        setUpObservables()
    }

    @Override
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

        viewModel.observablePlaySuccessAnimation.observe(viewLifecycleOwner, Observer {
            binding.animationSuccess.visibility = View.VISIBLE
            binding.animationSuccess.playAnimation()
            binding.animation2.playAnimation()
        })

        viewModel.observablePlayFailAnimation.observe(viewLifecycleOwner, Observer {
            binding.animationFail.visibility = View.VISIBLE
            binding.animationFail.playAnimation()
        })


        viewModel.observableShowAd.observe(viewLifecycleOwner, Observer {
            showAdAndReturn()
        })

        viewModel.observableYesNoDialog.observe(viewLifecycleOwner, Observer {
            it?.let { pair ->
                context?.let { context ->
                    val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                    alertDialog.setTitle(pair.first)
                    alertDialog.setMessage(pair.second)
                    alertDialog.setButton(
                        AlertDialog.BUTTON_NEGATIVE, resources.getString(R.string.runage_no)
                    ) { dialog, _ -> dialog.dismiss() }
                    alertDialog.setButton(
                        AlertDialog.BUTTON_POSITIVE, resources.getString(R.string.runage_yes)
                    ) { _, _ -> viewModel.onDeleteConfirmed() }
                    alertDialog.show()
                }
            }
        })
    }

    private fun showAdAndReturn(){
        try {
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        activity?.onBackPressed()
                    }

                    override fun onAdFailedToLoad(errorCode: Int) {
                        activity?.onBackPressed()
                    }

                    override fun onAdOpened() {
                        activity?.onBackPressed()
                    }

                    override fun onAdClicked() {
                        activity?.onBackPressed()
                    }

                    override fun onAdLeftApplication() {

                    }

                    override fun onAdClosed() {
                        activity?.onBackPressed()
                    }
                }
                mInterstitialAd.show()
            } else {
                activity?.onBackPressed()
            }
        }catch (exception: Exception){
            Timber.e(exception)
            activity?.onBackPressed()
        }
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

    private fun setUpPolyline(positions: List<LatLng>) {
        if (positions.isEmpty() || googleMap == null) return

        moveCamera(positions)
        val positionPointsArray = ArrayList(positions)
        val polylineOptions = PolylineOptions().addAll(positionPointsArray)
            .jointType(JointType.ROUND)
            .color(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            .width(typedValueUtil.dipToPixels(5f).toFloat())
        val polyLine = googleMap?.addPolyline(polylineOptions)
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
                googleMap?.moveCamera(cu)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}