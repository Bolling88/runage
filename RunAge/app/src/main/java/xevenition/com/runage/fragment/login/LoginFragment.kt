package xevenition.com.runage.fragment.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.SignInButton.COLOR_DARK
import com.google.firebase.auth.FirebaseAuth
import xevenition.com.runage.R
import xevenition.com.runage.architecture.BaseFragment
import xevenition.com.runage.architecture.getApplication
import xevenition.com.runage.databinding.FragmentLoginBinding
import xevenition.com.runage.fragment.settings.SettingsFragment


class LoginFragment : BaseFragment<LoginViewModel>() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }

        val factory = LoginViewModelFactory(getApplication())
        viewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.button.setColorScheme(COLOR_DARK)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservables()

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.permissionsGranted(false)
        } else {
            viewModel.permissionsGranted(true)
        }
    }

    @Override
    override fun setUpObservables() {
        super.setUpObservables()

        viewModel.observableLoginClicked.observe(viewLifecycleOwner, {
            startSignInIntent()
        })
    }

    private fun startSignInIntent() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(getString(R.string.web_client_id))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result =
                Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result?.isSuccess == true) {
                val account = result.signInAccount
                if(account != null) {
                    viewModel.firebaseAuthWithPlayGames(account)
                }else{
                    handleError(result)
                }
            } else {
                handleError(result!!)
            }
        }
    }

    private fun handleError(result: GoogleSignInResult) {
        var message = result.status.statusMessage
        if (message == null || message.isEmpty()) {
            message = getString(R.string.signin_other_error)
        }
        AlertDialog.Builder(requireContext()).setMessage(message)
            .setNeutralButton(android.R.string.ok, null).show()
    }

    companion object {
        const val RC_SIGN_IN = 33442
        fun newInstance() = SettingsFragment()
    }

}