package com.example.kimjeonghwan.fixyou

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.ActionBarDrawerToggle

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kimjeonghwan.fixyou.chat.ChatFragment
import com.example.kimjeonghwan.fixyou.ethereum.CreateWalletActivity
import com.example.kimjeonghwan.fixyou.ethereum.GenerateWalletActivity
import com.example.kimjeonghwan.fixyou.ethereum.WalletActivity
import com.example.kimjeonghwan.fixyou.friend.FriendFragment
import com.example.kimjeonghwan.fixyou.live.BroadCastFragment
import com.example.kimjeonghwan.fixyou.live.BroadCasterActivity
import com.example.kimjeonghwan.fixyou.meetup.MeetupActivity
import com.example.kimjeonghwan.fixyou.meetup.MeetupFragment
import com.example.kimjeonghwan.fixyou.profile.ProfileActivity
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient
import com.example.kimjeonghwan.fixyou.vod.VodFragment

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_nvigation.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val PERMISSIONS_ACCESS_FINE_LOCATION = 1000
    private val PERMISSIONS_ACCESS_COARSE_LOCATION = 1001
    private var isAccessFineLocation = false
    private var isAccessCoarseLocation = false
    private var isPermission = false
    private var user_email = "null"
    private var user_profile = "null"
    private var user_name = "null"
    private var user_keystore = "null"
    private var meetup_id = "null"

    private val arrayList = ArrayList<String>()

    internal var locationService: LocationService? = null // 서비스 객체
    internal var isService = false // 서비스 중인 확인용

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val pref = getSharedPreferences("login_info", Context.MODE_PRIVATE)
        user_email = pref.getString("email", "none")
        profileInfo(user_email)

        nav_view.setNavigationItemSelectedListener(this)
        locationService()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment? {
            //Returning the current tabs
            return when (position) {
                0 -> {
                    val broadCastFragment = BroadCastFragment()
                    broadCastFragment
                }
                1 -> {
                    val vodFragment = VodFragment()
                    vodFragment
                }
                2 -> {
                    val meetupFragment = MeetupFragment()
                    meetupFragment
                }
                3 -> {
                    val friendFragment = FriendFragment()
                    friendFragment
                }
                4 -> {
                    val chatFragment = ChatFragment()
                    chatFragment
                }
                else -> null
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 5
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "라이브"
                1 -> return "VOD"
                2 -> return "모임"
                3 -> return "친구찾기"
                4 -> return "채팅"
            }
            return null
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // 네비게이션 헤더의 정보를 다룬다.
    private fun onNavigationHeader() {
        val nav_header_view = nav_view.inflateHeaderView(R.layout.nav_header_nvigation)


        Log.e("user_name", user_name)
        nav_header_view.userNickname.text = user_name
        nav_header_view.userEmail.text = user_email
        Glide.with(applicationContext)
                .load(user_profile)
                .apply(RequestOptions.circleCropTransform())
                .into(nav_header_view.userProfile)
        //nav_header_view.userEmail.setText("")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            // 프로필 버튼
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            //이더리움지갑 버튼
            R.id.nav_wallet -> {
                var keydir: File? = null
                keydir = this.getDir("wallet", Context.MODE_PRIVATE)
                val listfiles = keydir.listFiles()
                if (listfiles!!.isEmpty()) {
                    val intent = Intent(this, CreateWalletActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, WalletActivity::class.java)
                    startActivity(intent)
                }
            }
            //캐쉬충전 버튼
            R.id.nav_cash -> {
                val intent = Intent(this, CashActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_meetup -> {
                if(meetup_id != "null"){
                    val intent = Intent(this, MeetupActivity::class.java)
                    intent.putExtra("id", meetup_id)
                    startActivity(intent)
                }
            }
            //방송시작 버튼
            R.id.nav_broadcast -> {
                val intent = Intent(this, BroadCasterActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_upload -> {

            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // 현재 위치를 갱신하는 서비스 시작
    fun locationService() {
        //callPermission()  // 권한 요청을 해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_ACCESS_FINE_LOCATION)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSIONS_ACCESS_COARSE_LOCATION)
        } else {
            val intent = Intent(
                    applicationContext, //현재제어권자
                    LocationService::class.java) // 이동할 컴포넌트
            startService(intent) // 서비스 시작
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessCoarseLocation = true
        }
        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true
        }
    }

    private fun profileInfo(email : String) {
        val call = RetrofitClient.getInstance().service.profileCall(email)
        call.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("ApplySharedPref")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    //프로필정보 받아옴
                    val res = response.body()!!.string()
                    val json_profile = JSONObject(res)
                    val profile_info = json_profile.getString("profile")
                    val json_info = JSONArray(profile_info)
                    val jsonObject = json_info.getJSONObject(0)
                    user_name = jsonObject.getString("name")
                    user_profile = jsonObject.getString("profile")
                    user_keystore = jsonObject.getString("keystore")
                    meetup_id = jsonObject.getString("meetup_id")
                    val balloon = jsonObject.getString("balloon")
                    // SharedPreferences를 사용해 유저정보를 저장한다.
                    val pref = getSharedPreferences("login_info", Context.MODE_PRIVATE)
                    val editor = pref.edit()
                    editor.putString("meetupId", meetup_id)
                    editor.putString("name", user_name)
                    editor.putString("profile", user_profile)
                    editor.putString("keystore", user_keystore)
                    editor.putString("balloon", balloon)
                    editor.commit()
                    onNavigationHeader()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, "Error" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
