package elena.altair.note.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import elena.altair.note.databinding.ProgressDialogLayoutBinding
import elena.altair.note.databinding.ProgressDialogPdfBinding

object ProgressDialog {

    fun createProgressDialog(act: Activity): AlertDialog{

        val builder = AlertDialog.Builder(act)
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

    fun createProgressDialogExtPdf(act: Activity): AlertDialog{

        val builder = AlertDialog.Builder(act)
        val rootDialogElement = ProgressDialogPdfBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }


}