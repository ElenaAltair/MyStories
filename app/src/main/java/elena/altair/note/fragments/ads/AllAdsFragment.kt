package elena.altair.note.fragments.ads

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.activities.MainActivity.Companion.ADS_DATA
import elena.altair.note.activities.MainActivity.Companion.ALL_ADS_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.ALL_PUBLISHED_BOOKS
import elena.altair.note.activities.MainActivity.Companion.EDIT_STATE_AD
import elena.altair.note.activities.MainActivity.Companion.MAIN_LIST_FRAGMENT
import elena.altair.note.activities.MainActivity.Companion.MY_PUBLISHED_BOOKS
import elena.altair.note.activities.MainActivity.Companion.SELECTED_PUBLISHED_BOOKS
import elena.altair.note.activities.MainActivity.Companion.USER_ANONYMOUS
import elena.altair.note.activities.MainActivity.Companion.catPublic
import elena.altair.note.activities.MainActivity.Companion.currentFrag
import elena.altair.note.activities.MainActivity.Companion.currentUser
import elena.altair.note.activities.ads.DescriptionActivity
import elena.altair.note.activities.ads.EditAdsActivity
import elena.altair.note.adapters.ads.AllAdsFragmentBookRsAdapter
import elena.altair.note.databinding.ContinueDialogBinding
import elena.altair.note.databinding.DeleteDialogBinding
import elena.altair.note.databinding.FragmentAllAdsBinding
import elena.altair.note.dialoghelper.DialogSpinnerHelper
import elena.altair.note.fragments.books.BackPressed
import elena.altair.note.fragments.books.BaseFragment
import elena.altair.note.fragments.books.DialogsAndOtherFunctions
import elena.altair.note.fragments.books.MainListFragment
import elena.altair.note.fragments.books.MainListFragment.Companion.SCROLL_DOWN
import elena.altair.note.model.Ad
import elena.altair.note.utils.InternetConnection.isOnline
import elena.altair.note.viewmodel.FirebaseViewModel
import elena.altair.note.viewmodel.MainViewModel


@AndroidEntryPoint
class AllAdsFragment : BaseFragment(), AllAdsFragmentBookRsAdapter.AdListener, BackPressed {

    private lateinit var binding: FragmentAllAdsBinding
    private var pref: SharedPreferences? = null
    private val mAuth = Firebase.auth
    private val dialog = DialogSpinnerHelper()
    private val adapter = AllAdsFragmentBookRsAdapter(mAuth, this@AllAdsFragment)

    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var clearUpdate: Boolean = true
    private var filter: Int = 0
    private var currentCategory = ""
    private var tempTime = "0"
    private lateinit var dialogProgres: AlertDialog

    override fun onClickNew() {

        if (mAuth.currentUser!!.isAnonymous || currentUser == USER_ANONYMOUS || currentUser == "" || currentUser == "null") {
            (activity as? DialogsAndOtherFunctions)?.createDialogI(
                resources.getString(R.string.only_registered_users)
            )
            return
        }
        val i = Intent(activity, EditAdsActivity::class.java)
        startActivity(i)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAllAdsBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        initFirebaseViewModal()

        pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initRecyclerView()
        scrollListener()





        binding.edCatLiter2.setOnClickListener {

            val listGenresLiter =
                resources.getStringArray(R.array.genres_of_literature).toMutableList() as ArrayList
            dialog.showSpinnerDialog(
                activity as AppCompatActivity,
                listGenresLiter,
                binding.edCatLiter2
            )


        }

        // отслеживаем момент, когда пользователь выберет книгу для публикации
        binding.edCatLiter2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

                filter = 1
                tempTime = "0"
                clearUpdate = true
                currentCategory = binding.edCatLiter2.text.toString()
                firebaseViewModel.loadAllAdsFromCatLiterFirstPage(currentCategory, dialogProgres)

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })

    }


    private fun initFirebaseViewModal() {

        (activity as? DialogsAndOtherFunctions)?.viewButtons(ALL_ADS_FRAGMENT)

        tempTime = "0"
        dialogProgres = (activity as? DialogsAndOtherFunctions)!!.progressDialog()
        if (!isOnline(activity as AppCompatActivity)) {
            dialogProgres.dismiss()
            (activity as? DialogsAndOtherFunctions)?.createDialogI(resources.getString(R.string.network_exception))
        }

        if (catPublic == MY_PUBLISHED_BOOKS) {
            clearUpdate = true

            if (currentCategory == "")
                firebaseViewModel.loadMyAdsFirstPage(dialogProgres)
            else
                firebaseViewModel.loadMyAdsFirstPageCatLiterTime(
                    currentCategory, dialogProgres
                )


        }
        if (catPublic == ALL_PUBLISHED_BOOKS) {
            clearUpdate = true

            if (currentCategory == "")
                firebaseViewModel.loadAllAdsFirstPage(dialogProgres)
            else
                firebaseViewModel.loadAllAdsFromCatLiterFirstPage(
                    currentCategory, dialogProgres
                )

        }
        if (catPublic == SELECTED_PUBLISHED_BOOKS) {
            binding.edCatLiter2.visibility = View.GONE
            binding.tvSelCatLit.visibility = View.GONE
            clearUpdate = true

            firebaseViewModel.loadMyFavs(dialogProgres)
        }

    }


    private fun initViewModel() {

        firebaseViewModel.liveAdsData.observe(activity as AppCompatActivity) {
            val list = getAdsByCategory(it)

            if (!clearUpdate) {
                adapter.updateAdapter(list)
            } else {
                adapter.updateAdapterWithClear(list)
            }
            binding.tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }

    }

    // если выбран фильтр по жанру литературы, создать список книг только с выбранным жанром
    // если фильтр не применен, то пусть в списке будут книги всех жанров
    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad> {
        val tempList = ArrayList<Ad>()

        if (catPublic == MY_PUBLISHED_BOOKS) {
            list.forEach {
                if (mAuth.uid == it.uidOwner)
                    tempList.add(it)
            }
            if (currentCategory != "") {
                tempList.clear()
                list.forEach {
                    if (currentCategory == it.categoryLiter && mAuth.uid == it.uidOwner)
                        tempList.add(it)
                }
            }
        }

        if (catPublic == ALL_PUBLISHED_BOOKS) {
            tempList.addAll(list)
            if (currentCategory != "") {
                tempList.clear()
                list.forEach {
                    if (currentCategory == it.categoryLiter)
                        tempList.add(it)
                }
            }
        }

        if (catPublic == SELECTED_PUBLISHED_BOOKS) {
            tempList.addAll(list)

        }

        tempList.reverse()
        if (tempList.size > 2 && tempList[tempList.size - 1] == tempList[tempList.size - 2])
            tempList.remove(tempList[tempList.size - 1])


        return tempList
    }


    private fun initRecyclerView() {
        binding.apply {
            rcViewAds.layoutManager = LinearLayoutManager(activity as AppCompatActivity)
            rcViewAds.adapter = adapter
        }

    }


    override fun onDeleteItem(ad: Ad) {

        if (!isOnline(activity as AppCompatActivity)) {
            (activity as? DialogsAndOtherFunctions)?.createDialogI(
                (activity as AppCompatActivity).resources.getString(R.string.network_exception)
            )
            return
        }


        deleteBookFromPublic(ad)

    }

    @SuppressLint("SuspiciousIndentation")
    private fun deleteBookFromPublic(ad: Ad) {
        val id = ad.idBookLocal!!.toLong()

        mainViewModel.getBook(id, ad.key.toString()).observe(viewLifecycleOwner) {
            if (it == null) {
                if (flagDel == 0)
                    createDialogContinue(
                        (activity as AppCompatActivity).resources.getString(R.string.published_from_another_device),
                        ad
                    )
            } else {
                createDialogDeletePublic(
                    resources.getString(R.string.sure_delete_book_published),
                    ad,
                )
            }
        }
    }

    private fun createDialogContinue(
        message: String,
        ad: Ad
    ) {
        val builder = AlertDialog.Builder(activity as AppCompatActivity)
        val bindingDialog =
            ContinueDialogBinding.inflate((activity as AppCompatActivity).layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvMess.text = message
        val dialog = builder.create()
        bindingDialog.bNo.setOnClickListener {
            dialog?.dismiss()
        }

        bindingDialog.bContinue.setOnClickListener {
            createDialogDeletePublic(
                resources.getString(R.string.sure_delete_book_published),
                ad,
            )

            dialog?.dismiss()
        }
        dialog.show()
    }


    private fun createDialogDeletePublic(
        message: String,
        ad: Ad,
    ) {
        val builder = AlertDialog.Builder(activity as AppCompatActivity)
        val bindingDialog =
            DeleteDialogBinding.inflate((activity as AppCompatActivity).layoutInflater)
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
            flagDel = 1
            firebaseViewModel.deleteItem(ad)
            dialog?.dismiss()


            //val id = ad.idBookLocal!!.toLong()
            //Log.d("MyLog", "key ${ad.key}")
            mainViewModel.getBookByKeyFirebase(ad.key.toString()).observe(viewLifecycleOwner) {
                val book = it
                if (book != null) {
                    book.public = "0"
                    book.uidAd = null
                    mainViewModel.updateBook(book)
                }
            }
            initFirebaseViewModal()
        }
        dialog.show()

    }

    override fun onAdViewed(ad: Ad) { // увеличиваем счетчик просмотров
        if (!isOnline(activity as AppCompatActivity)) {
            (activity as? DialogsAndOtherFunctions)?.createDialogI(
                (activity as AppCompatActivity).resources.getString(R.string.network_exception)
            )
            return
        }
        firebaseViewModel.adViewed(ad)

        val i = Intent(context, DescriptionActivity::class.java)
        i.putExtra(AD_KEY, ad)
        (activity as AppCompatActivity).startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        if (!isOnline(activity as AppCompatActivity)) {
            (activity as? DialogsAndOtherFunctions)?.createDialogI(
                (activity as AppCompatActivity).resources.getString(R.string.network_exception)
            )
            return
        }
        firebaseViewModel.onFavClick(ad) // нажали на сердечко
        initFirebaseViewModal() // временная мера
    }


    override fun onClickEdit(ad: Ad) {
        if (!isOnline(activity as AppCompatActivity)) {
            (activity as? DialogsAndOtherFunctions)?.createDialogI(
                (activity as AppCompatActivity).resources.getString(R.string.network_exception)
            )
            return
        }
        val editIntent =
            Intent((activity as AppCompatActivity), EditAdsActivity::class.java).apply {
                // true - открыли объявление для редактирования
                // false - создаём новое объявление
                putExtra(EDIT_STATE_AD, true)
                putExtra(ADS_DATA, ad)
            }
        (activity as AppCompatActivity).startActivity(editIntent)
    }


    // тест
    private fun scrollListener() {

        binding.rcViewAds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recView, newState)
                // не может скролиться вниз и новое состояние это состояние покоя
                if (!recView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //Log.d("MyLog", "Can't scroll down")
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!

                    if (adsList.isNotEmpty()) {
                        adsList[0]
                            .let {
                                if (tempTime == "0" || tempTime > it.time) {

                                    tempTime = it.time

                                    if (catPublic == MY_PUBLISHED_BOOKS) {
                                        if (filter == 0)
                                            firebaseViewModel.loadMyAdsNextPage(it.time)
                                        else {
                                            val catLiterTime = "${currentCategory}_${it.time}"
                                            firebaseViewModel.loadMyAdsNextPageCatLiterTime(
                                                catLiterTime
                                            )
                                        }
                                    }
                                    if (catPublic == ALL_PUBLISHED_BOOKS) {
                                        if (filter == 0)
                                            firebaseViewModel.loadAllAdsNextPage(it.time)
                                        else {
                                            val catLiterTime = "${currentCategory}_${it.time}"
                                            firebaseViewModel.loadAllAdsFromCatLiterNextPage(
                                                catLiterTime
                                            )
                                        }
                                    }
                                    if (catPublic == SELECTED_PUBLISHED_BOOKS) {
                                        firebaseViewModel.loadMyFavs(dialogProgres)
                                    }

                                }
                            }
                    }
                }
            }
        })
    }

    override fun handleOnBackPressed() {
        currentFrag = MAIN_LIST_FRAGMENT
        requireActivity().supportFragmentManager.commit {
            replace(R.id.placeHolder, MainListFragment.newInstance())
        }
    }

    companion object {

        const val AD_KEY = "AD"
        var flagDel = 0

        @JvmStatic
        fun newInstance() = AllAdsFragment()
    }
}


