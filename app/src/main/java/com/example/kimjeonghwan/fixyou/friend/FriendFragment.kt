package com.example.kimjeonghwan.fixyou.friend

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.location.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.Toast
import com.example.kimjeonghwan.fixyou.R
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPOIItem
import com.skt.Tmap.TMapPoint
import kotlinx.android.synthetic.main.fragment_friend.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.ContextUtils.getApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import com.skt.Tmap.TMapView


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FriendFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendFragment : Fragment() {

    private var friendRecommandAdpater: FriendRecommandAdpater? = null
    private var friendRecommandItem: ArrayList<FriendRecommandItem> = ArrayList()
    private lateinit var tMapView: TMapView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //friendCall()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        scrollviewFriend.scrollTo(0, 0)
        refreshFriend.visibility = View.VISIBLE
        // 위치 정보를 받아온다.
        val lm = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        val location = lm!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        val longitude = location.longitude
        val latitude = location.latitude

        tMapView = TMapView(context)
        tMapView.setSKTMapApiKey( "5f2e55f6-29a0-43ca-a397-218cf46c7a80" )
        surroundings_view.addView( tMapView )

        val markerItem1 = TMapMarkerItem()

        val tMapPoint1 = TMapPoint(latitude, longitude) // SKT타워

        // 마커 아이콘
        val bitmap = BitmapFactory.decodeResource(context!!.resources, R.drawable.marker_current_pink)

        markerItem1.icon = bitmap // 마커 아이콘 지정
        markerItem1.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.tMapPoint = tMapPoint1 // 마커의 좌표 지정
        tMapView.addMarkerItem("markerItem1", markerItem1) // 지도에 마커 추가

        tMapView.setCenterPoint(longitude, latitude)
        tMapView.setUserScrollZoomEnable(true)

        // 클릭 이벤트 설정
        tMapView.setOnClickListenerCallBack(object : TMapView.OnClickListenerCallback {
            override fun onPressUpEvent(p0: java.util.ArrayList<TMapMarkerItem>?, p1: java.util.ArrayList<TMapPOIItem>?, p2: TMapPoint?, p3: PointF?): Boolean {
                val intent = Intent(context, FriendLocationActivity::class.java)
                startActivity(intent)
                return true
            }

            override fun onPressEvent(p0: java.util.ArrayList<TMapMarkerItem>?, p1: java.util.ArrayList<TMapPOIItem>?, p2: TMapPoint?, p3: PointF?): Boolean {
                return true
            }
        })
        recommendFriend()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_refresh, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("CommitTransaction")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_refresh) {
            Log.e("action_refresh","action_refresh")
            refreshFriend.visibility = View.VISIBLE
            recommendFriend()
            //friendCall()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 리사이클러뷰 초기화
     */
    private fun InitRecyclerview() {
        friendRecommandAdpater = FriendRecommandAdpater(friendRecommandItem, context)
        friend_recommand_recyclerview.layoutManager = LinearLayoutManager(context) // room_recyclerview 에 레이아웃 매니저를 설정한다
        friend_recommand_recyclerview.adapter = friendRecommandAdpater
        friend_recommand_recyclerview.itemAnimator

        friend_recommand_recyclerview.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean { // 세로스크롤 막기
                return false
            }

            override fun canScrollHorizontally(): Boolean { //가로 스크롤막기
                return false
            }
        }
        refreshFriend.visibility = View.GONE
    }

    /**
     * 친구추천 리스트 불러옴
     */
    private fun recommendFriend() {
        val user_email = context!!.getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none")  // SharedPreference 에서 이메일 정보를 가져온다.
        val call = RetrofitClient.getInstance().service.recommendFriend(user_email)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    friendRecommandItem.clear()  // 친구추천 초기화
                    //프로필정보 받아옴
                    val res = response.body()!!.string()
                    Log.e("res", res)
                    val json_freind = JSONObject(res)
                    Log.e("res", json_freind.toString())
                    val freind_info = json_freind.getString("recommend")
                    Log.e("res", freind_info)
                    val json_info = JSONArray(freind_info)
                    for (i in 0 until json_info.length()) {
                        val jsonObject = json_info.getJSONObject(i)
                        Log.e("res", jsonObject.getString("email"))
                        val item = FriendRecommandItem()
                        //상대방 위치 정보를 받아옴
                        val latitude = jsonObject.getString("latitude")
                        val longitude = jsonObject.getString("longitude")

                        val geocoder = Geocoder(context)

                        // 위도,경도 입력 후 변환 버튼 클릭
                        var list: List<Address>? = null
                        try {
                            val d1 = java.lang.Double.parseDouble(latitude)
                            val d2 = java.lang.Double.parseDouble(longitude)

                            list = geocoder.getFromLocation(
                                    d1, // 위도
                                    d2, // 경도
                                    10) // 얻어올 값의 개수
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생")
                        }

                        if (list != null) {
                            if (list.size == 0) {
                                Log.e("address", "해당되는 주소 정보는 없습니다")
                            } else {
                                Log.e("address", list[0].getAddressLine(0).toString())
                                item.friend_location = list[0].getAddressLine(0).toString()
                            }
                        }
                        item.friend_email = jsonObject.getString("email") //이메일
                        item.friend_name = jsonObject.getString("name")  //이름
                        item.friend_nation = jsonObject.getString("nation")  //나라
                        item.friend_profile = jsonObject.getString("profile")  //프로필사진

                        friendRecommandItem.add(item)
                    }
                    InitRecyclerview()  //친구리스트를 초기화 한다.
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(getApplicationContext(), "Error" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
