package xevenition.com.runage.architecture


import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import xevenition.com.runage.MainApplication
import xevenition.com.runage.fragment.permission.PermissionViewModelFactory

open abstract class BaseFragment<T: BaseViewModel> : Fragment() {

     lateinit var viewModel: T

     @CallSuper
     open fun setUpObservables(){
          viewModel.observableNavigateTo.observe(viewLifecycleOwner, Observer {
               it?.let {
                    findNavController().navigate(it)
               }
          })

          viewModel.observableBackNavigation.observe(viewLifecycleOwner, Observer {
               activity?.onBackPressed()
          })

          viewModel.observableToast.observe(viewLifecycleOwner, Observer {
               it?.let {toast ->
                    context?.let {context ->
                         Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
                    }
               }
          })
     }

}
