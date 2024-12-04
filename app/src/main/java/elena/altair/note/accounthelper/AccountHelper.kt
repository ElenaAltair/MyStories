package elena.altair.note.accounthelper

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import elena.altair.note.R
import elena.altair.note.activities.MainActivity
import elena.altair.note.constants.FirebaseAuthConstants

class AccountHelper(private val act: MainActivity) {

    fun signUpWithEmail(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {
            if (act.mAuth.currentUser?.isAnonymous == true) {
                act.mAuth.currentUser?.delete()?.addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        signUpWithEmailTemp(email, password)
                    } else {
                        act.createDialog(act.resources.getString(R.string.network_exception))
                    }
                }
            } else {
                signUpWithEmailTemp(email, password)
            }
        } else {
            act.createDialog(act.resources.getString(R.string.bad_fill_fields))
            //Toast.makeText(act, act.resources.getString(R.string.bad_fill_fields), Toast.LENGTH_LONG).show()

        }
    }

    private fun signUpWithEmailTemp(email: String, password: String) {
        act.mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // если регистация прошла успешно:
                if (task.isSuccessful) {
                    signUpWithEmailSuccessful()
                } else { // иначе
                    task.exception?.let { signUpWithEmailException(it) }
                }
            }
    }

    private fun signUpWithEmailSuccessful() {
        // отправим пользователю ссылку для подтверждения его email адреса
        sendEmailVerification() //task.result?.user!!
        act.uiUpdate(act.mAuth.currentUser) //task.result?.user
    }

    private fun signUpWithEmailException(e: Exception) {
        // Log.d("MyLog", "Exception: " + task.exception)
        // val exception = task.exception as FirebaseAuthWeakPasswordException
        // Log.d("MyLog", "Exception:  ${exception.errorCode}" )
        when (e) {

            is FirebaseAuthWeakPasswordException -> {
                if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                    act.createDialog(act.resources.getString(R.string.password_invalid))
                }
            }

            is FirebaseAuthUserCollisionException -> {
                if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                    act.createDialog(act.resources.getString(R.string.email_in_use))
                }
            }

            is FirebaseAuthInvalidCredentialsException -> {
                if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    act.createDialog(act.resources.getString(R.string.email_badly_formatted))
                }
            }

            is FirebaseNetworkException -> {
                act.createDialog(act.resources.getString(R.string.network_exception))
            }

            else -> {
                act.createDialog("Exception: " + e)
            }
        }
    }


    fun signInWithEmail(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {
            if (act.mAuth.currentUser?.isAnonymous == true) {
                act.mAuth.currentUser?.delete()?.addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        signInWithEmailTemp(email, password)
                    } else {
                        act.createDialog(act.resources.getString(R.string.network_exception))
                    }
                }
            } else {
                signInWithEmailTemp(email, password)
            }

        } else {
            act.createDialog(act.resources.getString(R.string.bad_fill_fields))
        }
    }

    private fun signInWithEmailTemp(email: String, password: String) {
        act.mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // если регистация прошла успешно:
                if (task.isSuccessful) {
                    act.createDialog(act.resources.getString(R.string.successfully_logged))
                    act.uiUpdate(act.mAuth.currentUser) //task.result?.user
                } else {
                    // иначе
                    task.exception?.let { signInWithEmailException(it) }
                }

            }
    }

    private fun signInWithEmailException(e: Exception) {
        //Log.d("MyLog", "Exception: " + task.exception)
        //val exception = task.exception as FirebaseAuthInvalidCredentialsException
        //Log.d("MyLog", "Exception:  ${exception.errorCode}" )
        when (e) {
            is FirebaseAuthInvalidCredentialsException -> {
                when (e.errorCode) {
                    FirebaseAuthConstants.ERROR_INVALID_EMAIL -> {
                        act.createDialog(act.resources.getString(R.string.email_badly_formatted))
                        //Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
                    }

                    FirebaseAuthConstants.ERROR_INVALID_CREDENTIAL -> {
                        act.createDialog(act.resources.getString(R.string.bad_login_or_password))
                        //Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_CREDENTIAL, Toast.LENGTH_LONG).show()
                    }

                    FirebaseAuthConstants.ERROR_WRONG_PASSWORD -> {
                        act.createDialog(act.resources.getString(R.string.bad_login_or_password))
                        //Toast.makeText(act, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG).show()
                    }
                }
            }

            is FirebaseNetworkException -> {
                act.createDialog(act.resources.getString(R.string.network_exception))
            }

            else -> {
                act.createDialog("Exception: " + e)
            }
        }
    }


    private fun sendEmailVerification() {

        act.mAuth.currentUser?.sendEmailVerification()!!.addOnCompleteListener { task ->
            // если сообщение о проверке имейла было успешно отправлено:
            if (task.isSuccessful) {
                act.createDialog(act.resources.getString(R.string.send_email))
            } else {
                act.createDialog(act.resources.getString(R.string.failed_send_email))
            }
        }
    }

    fun signInAnonymously(listener: Listener) { //
        act.mAuth.signInAnonymously().addOnCompleteListener { task ->
            //Log.d("MyLog", "signInAnonymously")
            if (task.isSuccessful) {
                listener.onComplete()
                val mess =
                    act.resources.getString(R.string.entered_guest) + "\n" + act.getString(R.string.attention_work_offline)
                act.createDialog(mess)
            } //else {
            //act.createDialog(act.resources.getString(R.string.not_entered_guest))
            //}
        }
    }

    interface Listener {
        fun onComplete()
    }
}