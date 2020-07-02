package xevenition.com.runage.architecture


import android.content.DialogInterface
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController


abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    lateinit var viewModel: T

    @CallSuper
    open fun setUpObservables() {
        viewModel.observableNavigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(it)
            }
        })

        viewModel.observableBackNavigation.observe(viewLifecycleOwner, Observer {
            activity?.onBackPressed()
        })

        viewModel.observableToast.observe(viewLifecycleOwner, Observer {
            it?.let { toast ->
                context?.let { context ->
                    Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.observableInfoDialog.observe(viewLifecycleOwner, Observer {
            it?.let { triple ->
                context?.let { context ->
                    val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                    alertDialog.setTitle(triple.first)
                    alertDialog.setMessage(triple.second)
                    alertDialog.setButton(
                        AlertDialog.BUTTON_POSITIVE, triple.third
                    ) { dialog, _ -> dialog.dismiss() }
                    alertDialog.show()
                }
            }
        })
    }

}
