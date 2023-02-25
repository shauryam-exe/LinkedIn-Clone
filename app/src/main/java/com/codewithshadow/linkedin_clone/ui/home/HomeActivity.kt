package com.codewithshadow.linkedin_clone.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.codewithshadow.linkedin_clone.R
import com.codewithshadow.linkedin_clone.base.BaseActivity
import com.codewithshadow.linkedin_clone.constants.Constants
import com.codewithshadow.linkedin_clone.models.UserModel
import com.codewithshadow.linkedin_clone.ui.fragments.HomeFragment
import com.codewithshadow.linkedin_clone.ui.fragments.JobsFragment
import com.codewithshadow.linkedin_clone.ui.fragments.NetworkFragment
import com.codewithshadow.linkedin_clone.ui.fragments.NotificationFragment
import com.codewithshadow.linkedin_clone.ui.login.LoginActivity
import com.codewithshadow.linkedin_clone.ui.message_user.MessageUsersActivity
import com.codewithshadow.linkedin_clone.ui.profile.ProfileActivity
import com.codewithshadow.linkedin_clone.ui.share_post.SharePostActivity
import com.codewithshadow.linkedin_clone.utils.AppSharedPreferences
import com.codewithshadow.linkedin_clone.utils.UniversalImageLoderClass
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nostra13.universalimageloader.core.ImageLoader

class HomeActivity : BaseActivity() {
    lateinit var drawerLayout: DrawerLayout
    lateinit var profileImg: ImageView

    lateinit var messageBtn: ImageView
    lateinit var nav_img: ImageView
    lateinit var nav_close_img: ImageView
    lateinit var mNavigationView: NavigationView
    lateinit var tt: TextView
    lateinit var nav_name: TextView
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var selectedFragment: Fragment
    lateinit var appSharedPreferences: AppSharedPreferences
    lateinit var userRef: DatabaseReference
    lateinit var userReference: DocumentReference
    lateinit var user: FirebaseUser
    var model: UserModel? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        appSharedPreferences = AppSharedPreferences(this)

        user = FirebaseAuth.getInstance().currentUser!!
        userRef = FirebaseDatabase.getInstance().reference.child(Constants.USER_CONSTANT).child(
            user.uid
        )

        userReference = FirebaseFirestore.getInstance().collection(Constants.USER_CONSTANT).document(user.uid)

        drawerLayout = findViewById(R.id.drawerLayout)
        profileImg = findViewById(R.id.img)
        messageBtn = findViewById(R.id.messageBtn)
        mNavigationView = findViewById(R.id.nav_view)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                model = snapshot.child(Constants.INFO).getValue(
                    UserModel::class.java
                )
                appSharedPreferences.setUsername(model!!.username)
                appSharedPreferences.imgUrl = model!!.imageUrl

                nav_name.setText(model!!.username)
                Glide.with(this@HomeActivity).load(model!!.imageUrl).into(profileImg)
                Glide.with(this@HomeActivity).load(model!!.imageUrl).into(nav_img)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //UniversalImageLoaderClass
        val universalImageLoaderClass = UniversalImageLoderClass(this)
        ImageLoader.getInstance().init(universalImageLoaderClass.config)


        // Header
        val header = mNavigationView.getHeaderView(0)
        nav_name = header.findViewById(R.id.user_name)
        nav_img = header.findViewById(R.id.img)
        nav_close_img = header.findViewById(R.id.close_img)
        tt = header.findViewById(R.id.tt)

        header.findViewById<TextView>(R.id.logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,LoginActivity::class.java))
        }

        //Open Profile Activity
        tt.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(
                    this@HomeActivity,
                    ProfileActivity::class.java
                )
            )
        })

        // Set Header Data
        Glide.with(this).load(appSharedPreferences.imgUrl).into(profileImg)
        Glide.with(this).load(appSharedPreferences.imgUrl).into(nav_img)
        nav_name.setText(appSharedPreferences.userName)


        //NavBar Close
        nav_close_img.setOnClickListener(View.OnClickListener { v: View? ->
            if (drawerLayout.isDrawerOpen(
                    GravityCompat.START
                )
            ) drawerLayout.closeDrawer(GravityCompat.START)
        })


        // Open Drawer Layout
        profileImg.setOnClickListener(View.OnClickListener { v: View? ->
            if (!drawerLayout.isDrawerOpen(
                    GravityCompat.START
                )
            ) drawerLayout.openDrawer(Gravity.START) else drawerLayout.closeDrawer(Gravity.END)
        })

        // Open Message Activity
        messageBtn.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@HomeActivity, MessageUsersActivity::class.java)
            startActivity(intent)
        })


        //BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar)
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationSelectedListener)
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomeFragment())
            .commit()

    }

    private val navigationSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_network -> selectedFragment = NetworkFragment()
                R.id.nav_uplod -> {
                    startActivity(Intent(this@HomeActivity, SharePostActivity::class.java))
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
                }
                R.id.nav_notification -> selectedFragment = NotificationFragment()
                R.id.nav_jobs -> selectedFragment = JobsFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, selectedFragment).commit()
            true
        }

    override fun onBackPressed() {
        val mBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
        if (mBottomNavigationView.selectedItemId == R.id.nav_home) {
            super.onBackPressed()
            finish()
        } else {
            mBottomNavigationView.selectedItemId = R.id.nav_home
        }
    }
}