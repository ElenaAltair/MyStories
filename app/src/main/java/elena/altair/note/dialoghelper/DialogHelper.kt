package elena.altair.note.dialoghelper

import android.app.AlertDialog
import android.view.View
import elena.altair.note.R
import elena.altair.note.accounthelper.AccountHelper
import elena.altair.note.activities.MainActivity
import elena.altair.note.databinding.DialogSignBinding

class DialogHelper(private val act: MainActivity) {

    val accHelper = AccountHelper(act)

    fun createSignDialog(index: Int) { // 0 - регистрация, 1 - вход
        val builder = AlertDialog.Builder(act)
        val bindingDialog = DialogSignBinding.inflate(act.layoutInflater)
        val view = bindingDialog.root

        builder.setView(view)

        setDialogState(index, bindingDialog)

        val dialog = builder.create()

        bindingDialog.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, bindingDialog, dialog)
        }
        bindingDialog.btForgetP.setOnClickListener {
            setOnClickResetPassword(bindingDialog, dialog)
        }

        dialog.show()
    }

    // если пользователь забыл пароль
    private fun setOnClickResetPassword(bindingDialog: DialogSignBinding, dialog: AlertDialog?) {
        if (bindingDialog.edSignEmail.text.isNotEmpty()) {
            //Log.d("MyLog", bindingDialog.edSignEmail.text.toString().trim() ) // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            act.mAuth.sendPasswordResetEmail(bindingDialog.edSignEmail.text.toString().trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        act.createDialog(act.resources.getString(R.string.email_reset_password))
                        //Toast.makeText(act, act.resources.getString(R.string.email_reset_password), Toast.LENGTH_LONG).show()
                    } else {
                        act.createDialog(act.resources.getString(R.string.failed_email_reset_password))
                        //Toast.makeText(act, act.resources.getString(R.string.failed_email_reset_password), Toast.LENGTH_LONG).show()
                    }
                }
            dialog?.dismiss()
        } else {
            bindingDialog.tvDialogMessage.visibility = View.VISIBLE
            bindingDialog.tvDialogMessage.text = act.resources.getString(R.string.enter_email)
        }
    }

    private fun setOnClickSignUpIn(
        index: Int,
        bindingDialog: DialogSignBinding,
        dialog: AlertDialog?
    ) {
        dialog?.dismiss()
        if (index == DialogConst.SIGN_UP_STATE) {
            accHelper.signUpWithEmail(
                bindingDialog.edSignEmail.text.toString(),
                bindingDialog.edSignPassword.text.toString()
            )
        } else {
            accHelper.signInWithEmail(
                bindingDialog.edSignEmail.text.toString(),
                bindingDialog.edSignPassword.text.toString()
            )
        }
    }

    private fun setDialogState(index: Int, bindingDialog: DialogSignBinding) {
        if (index == DialogConst.SIGN_UP_STATE) {
            bindingDialog.tvSignTitle.text = act.resources.getString(R.string.ac_sign_up)
            bindingDialog.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
            bindingDialog.btForgetP.visibility = View.GONE
        } else {
            bindingDialog.tvSignTitle.text = act.resources.getString(R.string.ac_sign_in)
            bindingDialog.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            bindingDialog.btForgetP.visibility = View.VISIBLE
        }
    }


}