package elena.altair.note.dialoghelper

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import elena.altair.note.databinding.DialogHelpBinding

object DialogInfo {
    fun createDialogInfo(message: String, activity: AppCompatActivity) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = DialogHelpBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvInfo.text = message
        val dialog = builder.create()
        bindingDialog.bOk.setOnClickListener {
            dialog?.dismiss()
        }
        dialog.show()
    }
}