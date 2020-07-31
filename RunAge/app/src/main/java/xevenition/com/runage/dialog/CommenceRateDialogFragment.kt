package xevenition.com.runage.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import xevenition.com.runage.R


class CommenceRateDialogFragment : DialogFragment() {

    private lateinit  var callback: CommenceRateDialogCallback

    interface CommenceRateDialogCallback{
        fun onRateClicked()
        fun onLaterClicked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            callback = parentFragment as CommenceRateDialogCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override  fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        return builder
            .setTitle(getString(R.string.runage_happy_to_hear_it))
            .setMessage(getString(R.string.runage_rate_us))
            .setPositiveButton(R.string.runage_sure
            ) { arg0, arg1 ->
                callback.onRateClicked()
            }
            .setNegativeButton(R.string.runage_later
            ) { arg0, arg1 ->
                callback.onLaterClicked()
            }
            .create()
    }

    companion object {
        fun newInstance(): CommenceRateDialogFragment {
            return CommenceRateDialogFragment()
        }
    }
}