package elena.altair.note.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
import elena.altair.note.databinding.ActivityMainBinding
import elena.altair.note.databinding.HelpDialogBinding
import elena.altair.note.dialoghelper.DialogConst
import elena.altair.note.dialoghelper.DialogHelper
import elena.altair.note.fragments.ads.AllAdsFragment
import elena.altair.note.fragments.books.BackPressed
import elena.altair.note.fragments.books.ChapterListFragment
import elena.altair.note.fragments.books.FragmentManager
import elena.altair.note.fragments.books.FragmentManager.currentFlag
import elena.altair.note.fragments.books.HeroListFragment
import elena.altair.note.fragments.books.LocationListFragment
import elena.altair.note.fragments.books.MainListFragment
import elena.altair.note.fragments.books.PeopleListFragment
import elena.altair.note.fragments.books.PlotFragment
import elena.altair.note.fragments.books.TermListFragment
import elena.altair.note.fragments.books.ThemeFragment
import elena.altair.note.settings.SettingsActivity
import elena.altair.note.utils.font.TypefaceUtils.setTextBottomNav
import elena.altair.note.utils.font.TypefaceUtils.setTitleToolBar
import elena.altair.note.utils.font.TypefaceUtils.typeface
import elena.altair.note.utils.font.setTypeface
import elena.altair.note.utils.theme.ThemeUtils.getSelectedTheme
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var tvAccount: TextView
    private lateinit var binding: ActivityMainBinding
    private var currentMenuItemId = R.id.mainlist
    private lateinit var defPref: SharedPreferences
    private var pref: SharedPreferences? = null
    private var currentTheme = ""
    private val dialogHelper = DialogHelper(this)
    var backPressedTime: Long = 0

    //private val firebaseViewModel: FirebaseViewModel by viewModels()

    // обработаем нажатие на кнопку назад во фрагментах сюжета и темы
    val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            // выход при двойном нажатии
            if (backPressedTime + 3000 > System.currentTimeMillis()) {
                finish()
            } else { // инача обработаем нажатие на кнопку назад во фрагментах сюжета и темы
                val fragment = currentFlag
                (fragment as? BackPressed)?.handleOnBackPressed()?.let {
                    onBackPressedDispatcher.onBackPressed()
                }
                Toast.makeText(
                    this@MainActivity,
                    resources.getString(R.string.press_back_again),
                    Toast.LENGTH_LONG
                ).show()
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
            //uiUpdate(mAuth.currentUser)
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

        //Log.d("MyLog", "currentUser $currentUser")
        //Log.d("MyLog", "mAuth.currentUser?.isAnonymous ${mAuth.currentUser?.isAnonymous}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        defPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = defPref.getString("theme_key", "blue").toString()
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
                //Toast.makeText(this@MainActivity, "Tab selected: ${tab?.position} ${tab?.text}", Toast.LENGTH_SHORT).show()
                when (tab?.position) {
                    0 -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(0)!!.select()
                        //mainViewModel.bookTr.value = book
                        FragmentManager.setFragment(
                            PlotFragment.newInstance(),
                            this@MainActivity
                        )
                    }

                    1 -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(1)!!.select()
                        //mainViewModel.bookTr.value = book
                        FragmentManager.setFragment(
                            ThemeFragment.newInstance(),
                            this@MainActivity
                        )
                    }

                    2 -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(2)!!.select()
                        //mainViewModel.bookTr.value = book
                        FragmentManager.setFragment(
                            ChapterListFragment.newInstance(),
                            this@MainActivity
                        )
                    }

                    3 -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(3)!!.select()
                        //mainViewModel.bookTr.value = book
                        FragmentManager.setFragment(
                            HeroListFragment.newInstance(),
                            this@MainActivity
                        )
                    }

                    4 -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(4)!!.select()
                        //mainViewModel.bookTr.value = book
                        FragmentManager.setFragment(
                            LocationListFragment.newInstance(),
                            this@MainActivity
                        )
                    }

                    5 -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(5)!!.select()
                        //mainViewModel.bookTr.value = book
                        FragmentManager.setFragment(
                            PeopleListFragment.newInstance(),
                            this@MainActivity
                        )
                    }

                    6 -> {
                        binding.mainContent.tb.scrollX = binding.mainContent.tb.width
                        binding.mainContent.tb.getTabAt(6)!!.select()
                        //mainViewModel.bookTr.value = book
                        FragmentManager.setFragment(
                            TermListFragment.newInstance(),
                            this@MainActivity
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //TODO("Not yet implemented")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //TODO("Not yet implemented")
            }

        })

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onDestroy() {
        val fragment = currentFlag
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
                    startActivity(Intent(this, SettingsActivity::class.java))
                }

                R.id.mainlist -> {
                    if (currentUser != USER_ANONYMOUS)
                        currentUser = mAuth.currentUser?.email.toString()
                    currentMenuItemId = R.id.mainlist
                    if (currentFlag != null) {
                        if (currentFlag?.toString()!!
                                .contains("ChapterListFragment")
                        ) {
                            FragmentManager.setFragment(ChapterListFragment.newInstance(), this)
                        } else if (currentFlag?.toString()!!
                                .contains("PlotFragment")
                        ) {
                            FragmentManager.setFragment(PlotFragment.newInstance(), this)
                        } else if (currentFlag?.toString()!!
                                .contains("ThemeFragment")
                        ) {
                            FragmentManager.setFragment(ThemeFragment.newInstance(), this)
                        } else if (currentFlag?.toString()!!
                                .contains("HeroListFragment")
                        ) {
                            FragmentManager.setFragment(HeroListFragment.newInstance(), this)
                        } else if (currentFlag?.toString()!!
                                .contains("LocationListFragment")
                        ) {
                            FragmentManager.setFragment(LocationListFragment.newInstance(), this)
                        } else if (currentFlag?.toString()!!
                                .contains("PeopleListFragment")
                        ) {
                            FragmentManager.setFragment(PeopleListFragment.newInstance(), this)
                        } else if (currentFlag?.toString()!!
                                .contains("TermListFragment")
                        ) {
                            FragmentManager.setFragment(TermListFragment.newInstance(), this)
                        } else if (currentFlag?.toString()!!
                                .contains("AllAdsFragment")
                        ) {
                            FragmentManager.setFragment(AllAdsFragment.newInstance(), this)
                        } else {
                            FragmentManager.setFragment(MainListFragment.newInstance(), this)
                        }
                    } else {
                        FragmentManager.setFragment(MainListFragment.newInstance(), this)
                    }


                }

                R.id.add -> {

                    if (currentUser != USER_ANONYMOUS)
                        currentUser = mAuth.currentUser?.email.toString()
                    if (currentUser == "" || currentUser == "null") {
                        createDialog(resources.getString(R.string.only_registered_users))
                    } else {
                        currentFlag?.onClickNew()
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
                "theme_key",
                "blue"
            ) != currentTheme
        ) recreate() // эта функция пересоздает активити
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (currentUser != USER_ANONYMOUS)
            currentUser = mAuth.currentUser?.email.toString()
        when (item.itemId) {
            R.id.id_my_books -> {
                if (currentUser == "" || currentUser == "null") {
                    createDialog(resources.getString(R.string.ad_continue))
                } else {
                    FragmentManager.setFragment(MainListFragment.newInstance(), this)
                }
            }


            R.id.id_favourites -> {
                if (currentUser == "" || currentUser == "null") {
                    createDialog(resources.getString(R.string.ad_continue))
                } else {
                    catPublic = 3
                    FragmentManager.setFragment(AllAdsFragment.newInstance(), this)
                }
            }

            R.id.id_my_ads -> {
                if (mAuth.currentUser == null && currentUser != USER_ANONYMOUS) {
                    createDialog(resources.getString(R.string.only_registered_users))
                } else {
                    catPublic = 1
                    FragmentManager.setFragment(AllAdsFragment.newInstance(), this)
                }
            }

            R.id.id_my_new_ads -> {
                if (mAuth.currentUser == null && currentUser != USER_ANONYMOUS) {
                    createDialog(resources.getString(R.string.only_registered_users))
                } else {
                    val i = Intent(this, EditAdsActivity::class.java)
                    startActivity(i)
                }
            }

            R.id.profile -> {
                if (mAuth.currentUser!!.isAnonymous || currentUser == USER_ANONYMOUS || currentUser == "" || currentUser == "null") {
                    createDialog(resources.getString(R.string.can_create_profile))
                } else {
                    val i = Intent(this, ProfileActivity::class.java)
                    startActivity(i)
                }
            }

            R.id.id_other_ads -> {
                catPublic = 2
                FragmentManager.setFragment(AllAdsFragment.newInstance(), this)
            }

            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
                currentUser = mAuth.currentUser?.email.toString()
                tvAccount.text = mAuth.currentUser?.email
                FragmentManager.setFragment(MainListFragment.newInstance(), this)
                //Toast.makeText(this, "Presed id_sign_up", Toast.LENGTH_LONG).show()
            }

            R.id.id_sign_in -> {

                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
                currentUser = mAuth.currentUser?.email.toString()
                tvAccount.text = mAuth.currentUser?.email
                FragmentManager.setFragment(MainListFragment.newInstance(), this)
                //Toast.makeText(this, "Presed id_sign_in", Toast.LENGTH_LONG).show()
            }

            R.id.id_sign_in_anonymous -> {
                tvAccount.text = resources.getString(R.string.guest)
                currentUser = USER_ANONYMOUS
                uiUpdate(null)
                FragmentManager.setFragment(MainListFragment.newInstance(), this)
                createDialog(resources.getString(R.string.attention_work_offline))
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
                FragmentManager.setFragment(MainListFragment.newInstance(), this)
                //Toast.makeText(this, "Presed id_sign_out", Toast.LENGTH_LONG).show()
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
            } catch (e: Exception) {
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

    //функция изменения fontFamily
    private fun setFontFamily() = with(binding) {
        val tv: TextView = mainContent.toolbar.getChildAt(0) as TextView

        tv.setTypeface(
            pref?.getString("font_family_title_key", "sans-serif"),
            this@MainActivity
        )

        val font: Typeface? = typeface(
            pref?.getString("font_family_button_key", "sans-serif"),
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

    companion object {
        var currentUser = ""

        var flag = 0
        const val EDIT_STATE_AD = "edit_state"
        const val ADS_DATA = "ads_data"
        const val USER_ANONYMOUS = "Anonymous"

        // catPublic = 1 - мои опубликованные книги
        // catPublic = 2 - все опубликованные книги
        // catPublic = 3 - избранные опубликованные книги
        var catPublic = 0
    }


}