package elena.altair.note.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import elena.altair.note.databinding.DialogProgressLayoutBinding
import elena.altair.note.databinding.DialogProgressPdfBinding

object ProgressDialog {

    fun createProgressDialog(act: Activity): AlertDialog {

        val builder = AlertDialog.Builder(act)
        val rootDialogElement = DialogProgressLayoutBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

    fun createProgressDialogExtPdf(act: Activity): AlertDialog {

        val builder = AlertDialog.Builder(act)
        val rootDialogElement = DialogProgressPdfBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }


}