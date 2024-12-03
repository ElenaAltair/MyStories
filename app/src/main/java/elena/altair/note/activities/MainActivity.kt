package elena.altair.note.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import elena.altair.note.R
import elena.altair.note.accounthelper.AccountHelper
import elena.altair.note.activities.ads.EditAdsActivity
import elena.altair.note.activities.books.ProfileActivity
import elena.altair.note.constants.MyConstants.FONT_FAMILY_BUTTON_KEY
import elena.altair.note.constants.MyConstants.FONT_FAMILY_DEFAULT
import elena.altair.note.constants.MyConstants.FONT_FAMILY_TITLE_KEY
import elena.altair.note.constants.MyConstants.THEME_DEFAULT
import elena.altair.note.constants.MyConstants.THEME_KEY
import elena.altair.note.databinding.ActivityMainBinding
import elena.altair.note.databinding.DeleteDialogBinding
import elena.altair.note.databinding.HelpDialogBinding
import elena.altair.note.dialoghelper.DialogConst
import elena.altair.note.dialoghelper.DialogHelper
import elena.altair.note.dialoghelper.DialogInfo.createDialogInfo
import elena.altair.note.dialoghelper.DialogSave.dialogSavePlot
import elena.altair.note.dialoghelper.DialogSave.dialogSaveTheme
import elena.altair.note.dialoghelper.ProgressDialog.createProgressDialog
import elena.altair.note.etities.ChapterEntity2
import elena.altair.note.etities.HeroEntity2
import elena.altair.note.etities.LocationEntity2
import elena.altair.note.etities.PeopleEntity2
import elena.altair.note.etities.PlotEntity2
import elena.altair.note.etities.TermEntity2
import elena.altair.note.etities.ThemeEntity2
import elena.altair.note.fragments.ads.AllAdsFragment
import elena.altair.note.fragments.books.BackPressed
import elena.altair.note.fragments.books.BaseFragment
import elena.altair.note.fragments.books.ChapterListFragment
import elena.altair.note.fragments.books.DialogsAndOtherFunctions
import elena.altair.note.fragments.books.HeroListFragment
import elena.altair.note.fragments.books.LocationListFragment
import elena.altair.note.fragments.books.MainListFragment
import elena.altair.note.fragments.books.PeopleListFragment
import elena.altair.note.fragments.books.PlotFragment
import elena.altair.note.fragments.books.TermListFragment
import elena.altair.note.fragments.books.ThemeFragment
import elena.altair.note.settings.SettingsActivity
import elena.altair.note.utils.file.DOCXUtils.saveDocx
import elena.altair.note.utils.file.PDFUtils.savePdf
import elena.altair.note.utils.file.PdfTxtChapterListUtils.saveDocx
import elena.altair.note.utils.file.PdfTxtChapterListUtils.savePdf
import elena.altair.note.utils.file.PdfTxtChapterListUtils.saveTxt
import elena.altair.note.utils.file.PdfTxtHeroListUtils.saveDocx
import elena.altair.note.utils.file.PdfTxtHeroListUtils.savePdf
import elena.altair.note.utils.file.PdfTxtHeroListUtils.saveTxt
import elena.altair.note.utils.file.PdfTxtLocationListUtils.saveDocx
import elena.altair.note.utils.file.PdfTxtLocationListUtils.savePdf
import elena.altair.note.utils.file.PdfTxtLocationListUtils.saveTxt
import elena.altair.note.utils.file.PdfTxtPeopleListUtils.saveDocx
import elena.altair.note.utils.file.PdfTxtPeopleListUtils.savePdf
import elena.altair.note.utils.file.PdfTxtPeopleListUtils.saveTxt
import elena.altair.note.utils.file.PdfTxtTermListUtils.saveDocx
import elena.altair.note.utils.file.PdfTxtTermListUtils.savePdf
import elena.altair.note.utils.file.PdfTxtTermListUtils.saveTxt
import elena.altair.note.utils.file.TXTUtils.saveTxt
import elena.altair.note.utils.font.TypefaceUtils.setTextBottomNav
import elena.altair.note.utils.font.TypefaceUtils.setTitleToolBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.share.ShareHelperPlot.makeShareText
import elena.altair.note.utils.share.ShareHelperTheme.makeShareText
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import elena.altair.note.viewmodel.MainViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    DialogsAndOtherFunctions {

    private lateinit var tvAccount: TextView
    private lateinit var binding: ActivityMainBinding
    private var currentMenuItemId = R.id.mainlist
    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private var currentTheme = ""
    private val dialogHelper = DialogHelper(this)
    private var backPressedTime: Long = 0
    private val mainViewModel: MainViewModel by viewModels()
    var back = BACK_IS_NOT_PRESSED
    //private val firebaseViewModel: FirebaseViewModel by viewModels()

    // обработаем нажатие на кнопку назад во фрагментах сюжета и темы
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                // выход при двойном нажатии
                if (backPressedTime + 3000 > System.currentTimeMillis()) {
                    finish()
                } else { // инача обработаем нажатие на кнопку назад во фрагментах сюжета и темы
                    if (back == BACK_IS_NOT_PRESSED) {
                        back = BACK_WAS_PRESSED_ONCE
                        val fragment = getCurrentFragment()
                        //Log.d("MyLog", "fragment $fragment")
                        (fragment as? BackPressed)?.handleOnBackPressed().let {
                            onBackPressedDispatcher.onBackPressed()
                        }
                        Toast.makeText(
                            this@MainActivity,
                            resources.getString(R.string.press_back_again),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                backPressedTime = System.currentTimeMillis()

            }
        }

    @Inject
    lateinit var mAuth: FirebaseAuth


    @SuppressLint("SetTextI18n")
    override fun onStart() {
        setFontFamily()
        super.onStart()

        if (currentUser != USER_ANONYMOUS) {
            currentUser = mAuth.currentUser?.email.toString()
            if (currentUser != "null")
                tvAccount.text = currentUser

        } else {
            tvAccount.text = resources.getString(R.string.guest)
        }
        if (currentUser == "" || currentUser == "null") {
            binding.navView.menu.findItem(R.id.profile).isVisible = false
            binding.navView.menu.findItem(R.id.id_my_books).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_in).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_up).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_in_anonymous).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_out).isVisible = false
            binding.navView.menu.findItem(R.id.id_my_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_my_new_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_other_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_favourites).isVisible = false

        } else {
            binding.navView.menu.findItem(R.id.id_my_books).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_in).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_up).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_in_anonymous).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_out).isVisible = true

        }

        if (currentUser == USER_ANONYMOUS && mAuth.currentUser?.isAnonymous == true) {
            binding.navView.menu.findItem(R.id.profile).isVisible = false
            binding.navView.menu.findItem(R.id.id_my_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_my_new_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_other_ads).isVisible = true
            binding.navView.menu.findItem(R.id.id_favourites).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_in).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_up).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_out).isVisible = false
        } else if (currentUser == USER_ANONYMOUS && mAuth.currentUser?.isAnonymous == null) {
            binding.navView.menu.findItem(R.id.profile).isVisible = false
            binding.navView.menu.findItem(R.id.id_my_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_my_new_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_other_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_favourites).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_in).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_up).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_out).isVisible = true
            val str = tvAccount.text.toString()
            tvAccount.text = "$str (offline)"
            if (flag == 0) {
                createDialog(resources.getString(R.string.network_exception))
                flag = 1
            }
        } else if (currentUser != "" && currentUser != "null" && currentUser != USER_ANONYMOUS) {
            binding.navView.menu.findItem(R.id.id_my_ads).isVisible = true
            binding.navView.menu.findItem(R.id.id_my_new_ads).isVisible = true
            binding.navView.menu.findItem(R.id.id_other_ads).isVisible = true
            binding.navView.menu.findItem(R.id.id_favourites).isVisible = true
            binding.navView.menu.findItem(R.id.profile).isVisible = true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = defPref.getString(THEME_KEY, THEME_DEFAULT).toString()
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(getSelectedTheme(defPref))

        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        init()

        binding.mainContent.bottomNav.setSelectedItemId(R.id.mainlist)
        setBottomNavListener()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.mainContent.tb.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    TAB_POSITION_BUTTON_PLOT -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(TAB_POSITION_BUTTON_PLOT)!!.select()

                        currentFrag = PLOT_FRAGMENT
                        back = BACK_IS_NOT_PRESSED
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, PlotFragment.newInstance())
                        }

                    }

                    TAB_POSITION_BUTTON_THEME -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(TAB_POSITION_BUTTON_THEME)!!.select()
                        currentFrag = THEME_FRAGMENT
                        back = BACK_IS_NOT_PRESSED
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, ThemeFragment.newInstance())
                        }

                    }

                    TAB_POSITION_BUTTON_CHAPTER -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(TAB_POSITION_BUTTON_CHAPTER)!!.select()
                        currentFrag = CHAPTER_LIST_FRAGMENT
                        back = BACK_IS_NOT_PRESSED
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, ChapterListFragment.newInstance())
                        }
                    }

                    TAB_POSITION_BUTTON_HERO -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(TAB_POSITION_BUTTON_HERO)!!.select()
                        currentFrag = HERO_LIST_FRAGMENT
                        back = BACK_IS_NOT_PRESSED
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, HeroListFragment.newInstance())
                        }
                    }

                    TAB_POSITION_BUTTON_LOCATION -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(TAB_POSITION_BUTTON_LOCATION)!!.select()
                        currentFrag = LOCATION_LIST_FRAGMENT
                        back = BACK_IS_NOT_PRESSED
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, LocationListFragment.newInstance())
                        }
                    }

                    TAB_POSITION_BUTTON_PEOPLE -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(TAB_POSITION_BUTTON_PEOPLE)!!.select()
                        currentFrag = PEOPLE_LIST_FRAGMENT
                        back = BACK_IS_NOT_PRESSED
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, PeopleListFragment.newInstance())
                        }
                    }

                    TAB_POSITION_BUTTON_TERM -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(TAB_POSITION_BUTTON_TERM)!!.select()
                        currentFrag = TERM_LIST_FRAGMENT
                        back = BACK_IS_NOT_PRESSED
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, TermListFragment.newInstance())
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit

        })

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onDestroy() {
        val fragment = getCurrentFragment()
        (fragment as? BackPressed)?.onDestroy()?.let {
            super.onDestroy()
        }
        super.onDestroy()
    }

    private fun init() {
        // подключим наш собсвенный Action Bar к нашему активити
        setSupportActionBar(binding.mainContent.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.mainContent.toolbar,
            R.string.open, R.string.close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
    }

    private fun setBottomNavListener() {
        binding.mainContent.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.settings -> {
                    back = BACK_IS_NOT_PRESSED
                    startActivity(Intent(this, SettingsActivity::class.java))
                }

                R.id.mainlist -> {
                    back = BACK_IS_NOT_PRESSED
                    if (currentUser != USER_ANONYMOUS)
                        currentUser = mAuth.currentUser?.email.toString()
                    currentMenuItemId = R.id.mainlist
                    val currentFragment = getCurrentFragment()
                    if (currentFragment != null) {
                        if (currentFragment.toString()
                                .contains(CHAPTER_LIST_FRAGMENT)
                        ) {
                            currentFrag = CHAPTER_LIST_FRAGMENT

                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, ChapterListFragment.newInstance())
                            }
                        } else if (currentFragment.toString()
                                .contains(PLOT_FRAGMENT)
                        ) {
                            currentFrag = PLOT_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, PlotFragment.newInstance())
                            }
                        } else if (currentFragment.toString()
                                .contains(THEME_FRAGMENT)
                        ) {
                            currentFrag = THEME_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, ThemeFragment.newInstance())
                            }
                        } else if (currentFragment.toString()
                                .contains(HERO_LIST_FRAGMENT)
                        ) {
                            currentFrag = HERO_LIST_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, HeroListFragment.newInstance())
                            }
                        } else if (currentFragment.toString()
                                .contains(LOCATION_LIST_FRAGMENT)
                        ) {
                            currentFrag = LOCATION_LIST_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, LocationListFragment.newInstance())
                            }
                        } else if (currentFragment.toString()
                                .contains(PEOPLE_LIST_FRAGMENT)
                        ) {
                            currentFrag = PEOPLE_LIST_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, PeopleListFragment.newInstance())
                            }
                        } else if (currentFragment.toString()
                                .contains(TERM_LIST_FRAGMENT)
                        ) {
                            currentFrag = TERM_LIST_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, TermListFragment.newInstance())
                            }
                        } else if (currentFragment.toString()
                                .contains(ALL_ADS_FRAGMENT)
                        ) {
                            currentFrag = ALL_ADS_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, AllAdsFragment.newInstance())
                            }
                        } else {
                            currentFrag = MAIN_LIST_FRAGMENT
                            supportFragmentManager.commit {
                                replace(R.id.placeHolder, MainListFragment.newInstance())
                            }
                        }
                    } else {
                        currentFrag = MAIN_LIST_FRAGMENT
                        supportFragmentManager.commit {
                            replace(R.id.placeHolder, MainListFragment.newInstance())
                        }
                    }


                }

                R.id.add -> {

                    if (currentUser != USER_ANONYMOUS)
                        currentUser = mAuth.currentUser?.email.toString()
                    if (currentUser == "" || currentUser == "null") {
                        createDialogI(resources.getString(R.string.only_registered_users))
                    } else {
                        getCurrentFragment()?.onClickNew()
                    }
                }
            }
            true
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mainContent.bottomNav.selectedItemId = currentMenuItemId
        if (defPref.getString(
                THEME_KEY,
                THEME_DEFAULT
            ) != currentTheme
        ) recreate() // эта функция пересоздает активити
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (currentUser != USER_ANONYMOUS)
            currentUser = mAuth.currentUser?.email.toString()
        when (item.itemId) {
            R.id.id_my_books -> {
                if (currentUser == "" || currentUser == "null") {
                    createDialogI(resources.getString(R.string.ad_continue))
                } else {
                    currentFrag = MAIN_LIST_FRAGMENT
                    back = BACK_IS_NOT_PRESSED
                    supportFragmentManager.commit {
                        replace(R.id.placeHolder, MainListFragment.newInstance())
                    }
                }
            }


            R.id.id_favourites -> {
                if (currentUser == "" || currentUser == "null") {
                    createDialogI(resources.getString(R.string.ad_continue))
                } else {
                    currentFrag = ALL_ADS_FRAGMENT
                    catPublic = SELECTED_PUBLISHED_BOOKS
                    back = BACK_IS_NOT_PRESSED
                    supportFragmentManager.commit {
                        replace(R.id.placeHolder, AllAdsFragment.newInstance())
                    }
                }
            }

            R.id.id_my_ads -> {
                if (mAuth.currentUser == null && currentUser != USER_ANONYMOUS) {
                    createDialogI(resources.getString(R.string.only_registered_users))
                } else {
                    currentFrag = ALL_ADS_FRAGMENT
                    catPublic = MY_PUBLISHED_BOOKS
                    back = BACK_IS_NOT_PRESSED
                    supportFragmentManager.commit {
                        replace(R.id.placeHolder, AllAdsFragment.newInstance())
                    }
                }
            }

            R.id.id_my_new_ads -> {
                if (mAuth.currentUser == null && currentUser != USER_ANONYMOUS) {
                    createDialogI(resources.getString(R.string.only_registered_users))
                } else {
                    back = BACK_IS_NOT_PRESSED
                    val i = Intent(this, EditAdsActivity::class.java)
                    startActivity(i)
                }
            }

            R.id.profile -> {
                if (mAuth.currentUser!!.isAnonymous || currentUser == USER_ANONYMOUS || currentUser == "" || currentUser == "null") {
                    createDialogI(resources.getString(R.string.can_create_profile))
                } else {
                    back = BACK_IS_NOT_PRESSED
                    val i = Intent(this, ProfileActivity::class.java)
                    startActivity(i)
                }
            }

            R.id.id_other_ads -> {
                catPublic = ALL_PUBLISHED_BOOKS
                currentFrag = ALL_ADS_FRAGMENT
                back = BACK_IS_NOT_PRESSED
                supportFragmentManager.commit {
                    replace(R.id.placeHolder, AllAdsFragment.newInstance())
                }
            }

            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
                currentUser = mAuth.currentUser?.email.toString()
                tvAccount.text = mAuth.currentUser?.email
                currentFrag = MAIN_LIST_FRAGMENT
                back = BACK_IS_NOT_PRESSED
                supportFragmentManager.commit {
                    replace(R.id.placeHolder, MainListFragment.newInstance())
                }
            }

            R.id.id_sign_in -> {

                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
                currentUser = mAuth.currentUser?.email.toString()
                tvAccount.text = mAuth.currentUser?.email
                currentFrag = MAIN_LIST_FRAGMENT
                back = BACK_IS_NOT_PRESSED
                supportFragmentManager.commit {
                    replace(R.id.placeHolder, MainListFragment.newInstance())
                }
            }

            R.id.id_sign_in_anonymous -> {
                tvAccount.text = resources.getString(R.string.guest)
                currentUser = USER_ANONYMOUS
                uiUpdate(null)
                currentFrag = MAIN_LIST_FRAGMENT
                back = BACK_IS_NOT_PRESSED
                supportFragmentManager.commit {
                    replace(R.id.placeHolder, MainListFragment.newInstance())
                }
                createDialogI(resources.getString(R.string.attention_work_offline))
            }

            R.id.id_sign_out -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    currentUser = ""
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                currentUser = ""
                currentFrag = MAIN_LIST_FRAGMENT
                back = BACK_IS_NOT_PRESSED
                supportFragmentManager.commit {
                    replace(R.id.placeHolder, MainListFragment.newInstance())
                }
                createDialog(resources.getString(R.string.you_logged_out))
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?) {

        if (user == null) {
            try {
                dialogHelper.accHelper.signInAnonymously(object : AccountHelper.Listener {
                    override fun onComplete() {
                        tvAccount.text = resources.getString(R.string.guest)
                        currentUser = USER_ANONYMOUS
                    }
                })
            } catch (_: Exception) {
            }
            tvAccount.text = resources.getString(R.string.guest)
            currentUser = USER_ANONYMOUS
        } else if (user.isAnonymous) {
            currentUser = USER_ANONYMOUS
            tvAccount.text = resources.getString(R.string.guest)
        } else {
            tvAccount.text = user.email
        }
    }


    fun createDialog(message: String) { // 0 - регистрация, 1 - вход
        val builder = AlertDialog.Builder(this)
        val bindingDialog = HelpDialogBinding.inflate(this.layoutInflater)
        val view = bindingDialog.root
        builder.setView(view)
        bindingDialog.tvInfo.text = message

        val dialog = builder.create()

        bindingDialog.bOk.setOnClickListener {
            recreate() // эта функция пересоздает наше активити
            dialog?.dismiss()
        }

        dialog.show()
    }

    override fun createDialogI(message: String) {
        createDialogInfo(message, this)
    }


    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        val tv: TextView = mainContent.toolbar.getChildAt(0) as TextView

        tv.setTypeface(
            pref?.getString(FONT_FAMILY_TITLE_KEY, FONT_FAMILY_DEFAULT),
            this@MainActivity
        )

        val font: Typeface? = typeface(
            pref?.getString(FONT_FAMILY_BUTTON_KEY, FONT_FAMILY_DEFAULT),
            this@MainActivity
        )

        val bottomBar = mainContent.bottomNav.menu
        for (i in 0 until bottomBar.size()) {
            val menuItem: MenuItem = bottomBar.getItem(i)
            val spannableTitle = setTextBottomNav(menuItem.title, font)
            menuItem.setTitle(spannableTitle)
        }


        for (i in 0..6) {
            val mNewTitle = setTitleToolBar(binding.mainContent.tb.getTabAt(i)?.text, font)
            binding.mainContent.tb.getTabAt(i)?.setText(mNewTitle)
        }

    }


    override fun createDialogDelete(
        message: String,
        id: Long,
    ) {
        val builder = AlertDialog.Builder(this)
        val bindingDialog = DeleteDialogBinding.inflate(layoutInflater)
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
            if (message == resources.getString(R.string.sure_delete_book))
                mainViewModel.deleteBook(id)
            if (message == resources.getString(R.string.sure_delete_chapter))
                mainViewModel.deleteChapter(id)
            if (message == resources.getString(R.string.sure_delete_hero))
                mainViewModel.deleteHero(id)
            if (message == resources.getString(R.string.sure_delete_location))
                mainViewModel.deleteLocation(id)
            if (message == resources.getString(R.string.sure_delete_people))
                mainViewModel.deletePeople(id)
            if (message == resources.getString(R.string.sure_delete_term))
                mainViewModel.deleteTerm(id)
            dialog?.dismiss()
        }
        dialog.show()
    }

    override fun viewButtons(currentFragment: String) {
        if (currentFragment == MAIN_LIST_FRAGMENT) {
            findViewById<View>(R.id.add).visibility = View.VISIBLE
            findViewById<View>(R.id.mainlist).visibility = View.VISIBLE
            findViewById<View>(R.id.tb).visibility = View.GONE
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            val tv: TextView = toolbar.getChildAt(0) as TextView
            tv.text = resources.getString(R.string.app_name)

        } else if (currentFragment == CHAPTER_LIST_FRAGMENT) {
            findViewById<View>(R.id.add).visibility = View.VISIBLE
            findViewById<View>(R.id.mainlist).visibility = View.VISIBLE
            val tab = findViewById<TabLayout>(R.id.tb)
            tab.visibility = View.VISIBLE
            tab.getTabAt(TAB_POSITION_BUTTON_CHAPTER)!!.select()

        } else if (currentFragment == HERO_LIST_FRAGMENT ||
            currentFragment == LOCATION_LIST_FRAGMENT ||
            currentFragment == PEOPLE_LIST_FRAGMENT ||
            currentFragment == TERM_LIST_FRAGMENT
        ) {
            findViewById<View>(R.id.add).visibility = View.VISIBLE
            findViewById<View>(R.id.mainlist).visibility = View.VISIBLE
            findViewById<View>(R.id.tb).visibility = View.VISIBLE

        } else if (currentFragment == PLOT_FRAGMENT || currentFragment == THEME_FRAGMENT) {
            findViewById<View>(R.id.add).visibility = View.GONE
            findViewById<View>(R.id.mainlist).visibility = View.GONE
            findViewById<View>(R.id.tb).visibility = View.VISIBLE

        } else if (currentFragment == ALL_ADS_FRAGMENT) {
            findViewById<View>(R.id.add).visibility = View.VISIBLE
            findViewById<View>(R.id.tb).visibility = View.GONE
            val toolbar =
                findViewById<Toolbar>(R.id.toolbar)
            val tv: TextView = toolbar.getChildAt(0) as TextView
            if (catPublic == MY_PUBLISHED_BOOKS)
                tv.text = resources.getString(R.string.ad_my_ads)
            if (catPublic == ALL_PUBLISHED_BOOKS)
                tv.text = resources.getString(R.string.ad_other_ads)
            if (catPublic == SELECTED_PUBLISHED_BOOKS)
                tv.text = resources.getString(R.string.ad_favourites)
        }

    }

    override fun progressDialog(): AlertDialog {
        return createProgressDialog(this)
    }

    override fun textViewSetTypeface(fontFamily: String?, textView: TextView) {
        textView.setTypeface(fontFamily, this)
    }

    override fun editTextSetTypeface(fontFamily: String?, editText: EditText) {
        editText.setTypeface(fontFamily, this)
    }

    override suspend fun saveDocxChapters(
        titleBook: String,
        nameAuthor: String,
        list: List<ChapterEntity2>
    ): String {
        return saveDocx(titleBook, nameAuthor, list, this@MainActivity)
    }

    override suspend fun saveDocxHero(
        titleBook: String,
        nameAuthor: String,
        list: List<HeroEntity2>
    ): String {
        return saveDocx(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveDocxLocation(
        titleBook: String,
        nameAuthor: String,
        list: List<LocationEntity2>
    ): String {
        return saveDocx(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveDocxPeople(
        titleBook: String,
        nameAuthor: String,
        list: List<PeopleEntity2>
    ): String {
        return saveDocx(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveDocxTerm(
        titleBook: String,
        nameAuthor: String,
        list: List<TermEntity2>
    ): String {
        return saveDocx(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveDocx(
        title: String,
        string: String
    ): String {
        return saveDocx(title, string, this)
    }

    override suspend fun savePdfChapters(
        titleBook: String,
        nameAuthor: String,
        list: List<ChapterEntity2>
    ): String {
        return savePdf(titleBook, nameAuthor, list, this@MainActivity)
    }

    override suspend fun savePdfHero(
        titleBook: String,
        nameAuthor: String,
        list: List<HeroEntity2>
    ): String {
        return savePdf(titleBook, nameAuthor, list, this)
    }

    override suspend fun savePdfLocation(
        titleBook: String,
        nameAuthor: String,
        list: List<LocationEntity2>
    ): String {
        return savePdf(titleBook, nameAuthor, list, this)
    }

    override suspend fun savePdfPeople(
        titleBook: String,
        nameAuthor: String,
        list: List<PeopleEntity2>
    ): String {
        return savePdf(titleBook, nameAuthor, list, this)
    }

    override suspend fun savePdfTerm(
        titleBook: String,
        nameAuthor: String,
        list: List<TermEntity2>
    ): String {
        return savePdf(titleBook, nameAuthor, list, this)
    }

    override suspend fun savePdf(
        title: String,
        string: String
    ): String {
        return savePdf(title, string, this)
    }

    override suspend fun saveTxtChapters(
        titleBook: String,
        nameAuthor: String,
        list: List<ChapterEntity2>
    ): String {
        return saveTxt(titleBook, nameAuthor, list, this@MainActivity)
    }

    override suspend fun saveTxtHero(
        titleBook: String,
        nameAuthor: String,
        list: List<HeroEntity2>
    ): String {
        return saveTxt(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveTxtLocation(
        titleBook: String,
        nameAuthor: String,
        list: List<LocationEntity2>
    ): String {
        return saveTxt(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveTxtPeople(
        titleBook: String,
        nameAuthor: String,
        list: List<PeopleEntity2>
    ): String {
        return saveTxt(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveTxtTerm(
        titleBook: String,
        nameAuthor: String,
        list: List<TermEntity2>
    ): String {
        return saveTxt(titleBook, nameAuthor, list, this)
    }

    override suspend fun saveTxt(
        title: String,
        string: String
    ): String {
        return saveTxt(title, string, this)
    }

    override fun makeShareTextTheme(theme: ThemeEntity2, listName: String, nameA: String): String {
        return makeShareText(theme, listName, nameA, this)
    }

    override fun makeShareTextPlot(plot: PlotEntity2, listName: String, nameA: String): String {
        return makeShareText(plot, listName, nameA, this)
    }

    override fun dialogSaveTheme(message: String, new: Boolean, tempTheme: ThemeEntity2) {
        dialogSaveTheme(message, this, new, mainViewModel, tempTheme)
    }

    override fun dialogSavePlot(message: String, new: Boolean, tempPlot: PlotEntity2) {
        dialogSavePlot(message, this, new, mainViewModel, tempPlot)
    }


    companion object {
        var currentUser = ""

        const val BACK_WAS_PRESSED_ONCE = 1
        const val BACK_IS_NOT_PRESSED = 0

        var flag = 0
        const val EDIT_STATE_AD = "edit_state"
        const val ADS_DATA = "ads_data"
        const val USER_ANONYMOUS = "Anonymous"

        const val MY_PUBLISHED_BOOKS = 1 // catPublic = 1 - мои опубликованные книги
        const val ALL_PUBLISHED_BOOKS = 2 // catPublic = 2 - все опубликованные книги
        const val SELECTED_PUBLISHED_BOOKS = 3 // catPublic = 3 - избранные опубликованные книги
        var catPublic = 0

        const val TAB_POSITION_BUTTON_PLOT = 0
        const val TAB_POSITION_BUTTON_THEME = 1
        const val TAB_POSITION_BUTTON_CHAPTER = 2
        const val TAB_POSITION_BUTTON_HERO = 3
        const val TAB_POSITION_BUTTON_LOCATION = 4
        const val TAB_POSITION_BUTTON_PEOPLE = 5
        const val TAB_POSITION_BUTTON_TERM = 6

        const val CHAPTER_LIST_FRAGMENT = "ChapterListFragment"
        const val PLOT_FRAGMENT = "PlotFragment"
        const val THEME_FRAGMENT = "ThemeFragment"
        const val HERO_LIST_FRAGMENT = "HeroListFragment"
        const val LOCATION_LIST_FRAGMENT = "LocationListFragment"
        const val PEOPLE_LIST_FRAGMENT = "PeopleListFragment"
        const val TERM_LIST_FRAGMENT = "TermListFragment"
        const val ALL_ADS_FRAGMENT = "AllAdsFragment"
        const val MAIN_LIST_FRAGMENT = "MainListFragment"
        var currentFrag = ""

        fun FragmentActivity.getCurrentFragment(): BaseFragment? =
            supportFragmentManager.findFragmentById(R.id.placeHolder) as? BaseFragment

    }
}