package elena.altair.note.dialoghelper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import elena.altair.note.R
import elena.altair.note.databinding.DeleteDialogBinding
import elena.altair.note.viewmodel.MainViewModel

object DialogDelete {
    fun createDialogDelete(
        message: String,
        activity: AppCompatActivity,
        id: Long,
        mainViewModel: MainViewModel
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = DeleteDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()

        object : CountDownTimer(10000, 1000) {

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                bindingDialog.tvCounter.text = "" + (millisUntilFinished / 1000)
            }

            override fun onFinish() {
                bindingDialog.bDelete.visibility = View.VISIBLE
            }
        }.start()


        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bDelete.setOnClickListener {
            if (message == activity.resources.getString(R.string.sure_delete_book))
                mainViewModel.deleteBook(id)
            if (message == activity.resources.getString(R.string.sure_delete_chapter))
                mainViewModel.deleteChapter(id)
            if (message == activity.resources.getString(R.string.sure_delete_hero))
                mainViewModel.deleteHero(id)
            if (message == activity.resources.getString(R.string.sure_delete_location))
                mainViewModel.deleteLocation(id)
            if (message == activity.resources.getString(R.string.sure_delete_people))
                mainViewModel.deletePeople(id)
            if (message == activity.resources.getString(R.string.sure_delete_term))
                mainViewModel.deleteTerm(id)
            dialog?.dismiss()
        }
        dialog.show()
    }


}