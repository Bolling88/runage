package xevenition.com.runage.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import xevenition.com.runage.R


class RateDialogFragment : DialogFragment() {

    private lateinit  var callback: RateDialogCallback

    interface RateDialogCallback{
        fun onLikeClicked()
        fun onDislikeClicked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            callback = parentFragment as RateDialogCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override  fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        return builder
            .setTitle(getString(R.string.runage_hi_there))
            .setMessage(getString(R.string.runage_are_you_happy))
            .setPositiveButton(R.string.runage_yes
            ) { arg0, arg1 ->
                callback.onLikeClicked()
            }
            .setNegativeButton(R.string.runage_no
            ) { arg0, arg1 ->
                callback.onDislikeClicked()
            }
            .create()
    }

    companion object {
        fun newInstance(): RateDialogFragment {
            return RateDialogFragment()
        }
    }
}