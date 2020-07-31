package xevenition.com.runage.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import xevenition.com.runage.R


class FeedbackDialogFragment : DialogFragment() {

    private lateinit  var callback: FeedbackDialogCallback

    interface FeedbackDialogCallback{
        fun onFeedbackClicked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            callback = parentFragment as FeedbackDialogCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override  fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        return builder
            .setTitle(getString(R.string.runage_sorry_to_hear_that))
            .setMessage(getString(R.string.runage_would_you_like_feedback))
            .setPositiveButton(R.string.runage_yes
            ) { arg0, arg1 ->
                callback.onFeedbackClicked()
            }
            .setNegativeButton(R.string.runage_no
            ) { arg0, arg1 ->
                dismiss()
            }
            .create()
    }

    companion object {
        fun newInstance(): FeedbackDialogFragment {
            return FeedbackDialogFragment()
        }
    }
}