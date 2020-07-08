package xevenition.com.runage.fragment.share

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import xevenition.com.runage.databinding.FragmentShareBinding
import xevenition.com.runage.fragment.path.PathFragment
import xevenition.com.runage.util.BitmapUtil
import xevenition.com.runage.util.TypedValueUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class ShareFragment : BaseFragment<ShareViewModel>() {

    private var polyLine: Polyline? = null
    private var userMarker: Marker? = null
    private var startMarker: Marker? = null
    private var googleMap: GoogleMap? = null
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var binding: FragmentShareBinding

    @Inject
    lateinit var typedValueUtil: TypedValueUtil

    @Inject
    lateinit var bitmapUtil: BitmapUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as MainApplication).appComponent.inject(this)
        val args = ShareFragmentArgs.fromBundle(requireArguments())
        val factory = ShareViewModelFactory(getApplication(), args)
        viewModel = ViewModelProvider(this, factory).get(ShareViewModel::class.java)

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_share, container, false)
        binding.mapView.onCreate(arguments)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.mapView.getMapAsync {
            googleMap = it
            viewModel.onMapCreated()
        }
        setUpObservables()
        return binding.root
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

        viewModel.observableShowAd.observe(viewLifecycleOwner, Observer {
            showAdAndReturn()
        })

        viewModel.observableShare.observe(viewLifecycleOwner, Observer {
            loadBitmapFromMaps()
        })

        viewModel.observableMapType.observe(viewLifecycleOwner, Observer {
            it?.let {
                googleMap?.mapType = it
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

    private fun loadBitmapFromMaps(){
        val callback = GoogleMap.SnapshotReadyCallback {
            val ob = BitmapDrawable(resources, it)
            binding.shareBackground.background = ob

            val bitmap = loadBitmapFromView(binding.shareView)
            if(bitmap != null) {
                saveImageToCache(bitmap)
                shareImage()
            }else{
                Toast.makeText(requireContext(), R.string.runage_unexpected_error, Toast.LENGTH_LONG).show()
            }
        }

        googleMap?.snapshot(callback)
    }

    private fun loadBitmapFromView(view: View): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable: Drawable? = view.background
        if (bgDrawable != null) //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas) else  //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private fun saveImageToCache(bitmap: Bitmap){
        // save bitmap to cache directory
        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun shareImage(){
        val imagePath = File(requireContext().cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        val contentUri: Uri =
            FileProvider.getUriForFile(requireContext(), "xevenition.com.runage.fileprovider", newFile)

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
        shareIntent.setDataAndType(contentUri, requireActivity().contentResolver.getType(contentUri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        startActivity(Intent.createChooser(shareIntent, "Choose an app"))
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
