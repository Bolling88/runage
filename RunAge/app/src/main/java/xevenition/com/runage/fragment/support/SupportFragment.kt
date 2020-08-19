package xevenition.com.runage.fragment.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import timber.log.Timber
import xevenition.com.runage.MainApplication
import xevenition.com.runage.R
import xevenition.com.runage.activity.MainActivity
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentSupportBinding
import xevenition.com.runage.util.ResourceUtil
import javax.inject.Inject


class SupportFragment : BaseFragment<SupportViewModel>() {

    private lateinit var binding: FragmentSupportBinding

    @Inject
    lateinit var resourceUtil: ResourceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as MainApplication).appComponent.inject(this)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.mainNavigation)
        }
        val factory = SupportViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(SupportViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_support, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openMenu()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableSendEmail.observe(viewLifecycleOwner, {
            sendEmail()
        })

        viewModel.observableOpenBrowser.observe(viewLifecycleOwner, {
            openBrowser()
        })
    }

    private fun openBrowser() {
        val url = "https://dontkillmyapp.com/"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun sendEmail(){
        Timber.d("Sending email...")
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "xevenition@gmail.com", null
            )
        )
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RunAge Support")
        startActivity(Intent.createChooser(emailIntent, resourceUtil.getString(R.string.runage_send_email)))
    }
}
