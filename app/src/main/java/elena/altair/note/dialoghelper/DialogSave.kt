package elena.altair.note.dialoghelper

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import elena.altair.note.R
import elena.altair.note.databinding.SaveDialogBinding
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.etities.BookEntity7
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.etities.PeopleEntity2
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.etities.TermEntity2
import elena.altair.note.etities.ThemeEntity2
import elena.altair.note.viewmodel.MainViewModel

object DialogSave {

    fun dialogSaveBook(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempBook: BookEntity7
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }

        bindingDialog.bSave.setOnClickListener {
            if (new) {
                mainViewModel.insertBook(tempBook)
            } else {
                mainViewModel.updateBook(tempBook)
            }
            if (!new)
                createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)
            dialog?.dismiss()
        }
        dialog.show()
    }

    fun dialogSaveChapter(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempChapter: ChapterEntity2
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {
            if (new) {
                mainViewModel.insertChapter(tempChapter)
            } else {
                mainViewModel.updateChapter(tempChapter)
            }
            if (!new)
                createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)
            dialog?.dismiss()
        }
        dialog.show()
    }

    fun dialogSaveHero(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempHero: HeroEntity2
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {
            if (new) {
                mainViewModel.insertHero(tempHero)
            } else {
                mainViewModel.updateHero(tempHero)
            }
            if (!new)
                createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)
            dialog?.dismiss()
        }
        dialog.show()
    }

    fun dialogSaveLocation(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempLocation: LocationEntity2
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {
            if (new) {
                mainViewModel.insertLocation(tempLocation)
            } else {
                mainViewModel.updateLocation(tempLocation)
            }
            if (!new)
                createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)
            dialog?.dismiss()
        }
        dialog.show()
    }

    fun dialogSavePeople(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempPeople: PeopleEntity2
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {
            if (new) {
                mainViewModel.insertPeoples(tempPeople)
            } else {
                mainViewModel.updatePeople(tempPeople)
            }
            if (!new)
                createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)
            dialog?.dismiss()
        }
        dialog.show()
    }

    fun dialogSaveTerm(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempTerm: TermEntity2
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {
            if (new) {
                mainViewModel.insertTerm(tempTerm)
            } else {
                mainViewModel.updateTerm(tempTerm)
            }
            if (!new)
                createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)
            dialog?.dismiss()
        }
        dialog.show()
    }

    fun dialogSavePlot(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempPlot: PlotEntity2
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {

            mainViewModel.insertPlot(tempPlot)

            createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)

            dialog?.dismiss()
        }
        dialog.show()
    }

    fun dialogSaveTheme(
        message: String,
        activity: AppCompatActivity,
        new: Boolean,
        mainViewModel: MainViewModel,
        tempTheme: ThemeEntity2
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {

            mainViewModel.insertTheme(tempTheme)

            createDialogInfo(activity.resources.getString(R.string.changes_saved), activity)
            dialog?.dismiss()
        }
        dialog.show()
    }

    fun DialogSaveAndGetOut(
        activity: AppCompatActivity,
        i: Intent,
    ) {
        val builder = AlertDialog.Builder(activity)
        val bindingDialog = SaveDialogBinding.inflate(activity.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = activity.resources.getString(R.string.sure_save)
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            activity.finish()
            dialog?.dismiss()
        }
        bindingDialog.bSave.setOnClickListener {

            activity.setResult(RESULT_OK, i)
            activity.finish()
            dialog?.dismiss()
        }
        dialog.show()
    }

}