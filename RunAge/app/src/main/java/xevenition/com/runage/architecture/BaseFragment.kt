package xevenition.com.runage.architecture


import androidx.fragment.app.Fragment

open abstract class BaseFragment<T: BaseViewModel> : Fragment() {

     lateinit var viewModel: T

}
