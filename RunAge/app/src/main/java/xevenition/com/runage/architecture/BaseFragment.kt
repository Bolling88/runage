package xevenition.com.runage.architecture


import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import xevenition.com.runage.MainApplication
import xevenition.com.runage.fragment.permission.PermissionViewModelFactory

open abstract class BaseFragment<T: BaseViewModel> : Fragment() {

     lateinit var viewModel: T

     override fun onActivityCreated(savedInstanceState: Bundle?) {
          super.onActivityCreated(savedInstanceState)
     }

     @CallSuper
     open fun setUpObservables(){
          viewModel.observableNavigateTo.observe(viewLifecycleOwner, Observer {
               it?.let {
                    findNavController().navigate(it)
               }
          })
     }

}
