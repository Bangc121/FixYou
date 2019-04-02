package com.example.kimjeonghwan.fixyou.meetup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.example.kimjeonghwan.fixyou.R
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.fragment_meetup.*

import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import android.widget.LinearLayout


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MeetupFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MeetupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MeetupFragment : Fragment() {
    private var myMeetupListAdapter: MyMeetupListAdapter? = null
    private var myMeetupListItem: ArrayList<MeetupListItem> = ArrayList()

    private var meetupListAdapter: MeetupListAdapter? = null
    private var meetupListItem: ArrayList<MeetupListItem> = ArrayList()
    private var user_email: String = ""
    private var meetup_id: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meetup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 모임 생성하는 액티비티로 이동
        meetup_create.setOnClickListener({
            Log.e("meetup_id", meetup_id)
            // 이미 그룹을 생성한 경우 생성 불가
            if (meetup_id == "null"){
                val intent = Intent(context, MeetupCreateActivity::class.java)
                startActivity(intent)
            } else{
                val msgDialog = AlertDialog.Builder(context)
                msgDialog.setTitle("알림")
                msgDialog.setMessage("이미 생성한 그룹이 있습니다.")
                msgDialog.setPositiveButton("확인", null)
                msgDialog.show()
            }
        })
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
            refreshMeetup.visibility = View.VISIBLE
            getData()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        refreshMeetup.visibility = View.VISIBLE
        getData()
    }

    private fun getData() {
        user_email = activity!!.getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none") // SharedPreference 에서 이메일 정보를 가져온다.
        meetup_id = activity!!.getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("meetupId","none") // SharedPreference 에서 이메일 정보를 가져온다.
        MeetupList()
        myMeetupList()
        refreshMeetup.visibility = View.GONE
    }

    /**
     * 추천 그룹 초기화
     */
    private fun InitRecommend() {
        meetupListAdapter = MeetupListAdapter(meetupListItem,context)
        meetup_list_recyclerview.layoutManager = LinearLayoutManager(context) // room_recyclerview 에 레이아웃 매니저를 설정한다
        meetup_list_recyclerview.adapter = meetupListAdapter
        meetup_list_recyclerview.itemAnimator
    }

    /**
     * 추천 그룹리스트 출력
     */
    private fun MeetupList() {
        // Retrofit을 사용해 DB에 저장된 밋업 리스트를 불러온다.
        val call = RetrofitClient.getInstance().service.MeetupList(user_email)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    meetupListItem.clear()  // 모임목록을 초기화한다.
                    val result = response.body()!!.string()  // broadcast 정보를 받아온다.
                    Log.d("result", result)
                    val dataObject = JSONObject(result)
                    Log.d("dataObject", dataObject.toString())
                    val meetup = dataObject.getString("meetup")
                    Log.d("meetup", meetup)
                    val dataArray = JSONArray(meetup)
                    for (i in 0 until dataArray.length()) {
                        val jsonObject = dataArray.getJSONObject(i)
                        val item = MeetupListItem()

                        item.meetup_id = jsonObject.getString("meetup_id")
                        item.meetup_title = jsonObject.getString("meetup_title")
                        item.meetup_content = jsonObject.getString("meetup_content")
                        item.meetup_picture = jsonObject.getString("meetup_picture")
                        item.meetup_creater = jsonObject.getString("meetup_creater")
                        meetupListItem.add(item)
                    }

                    InitRecommend()  // 리사이클러뷰를 초기화한다.
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }

    /**
     * 내 그룹 초기화
     */
    private fun InitMyMeetup() {
        //가로 레이아웃
        myMeetupListAdapter = MyMeetupListAdapter(myMeetupListItem,context)
        my_meetup_list_recyclerview.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false) // room_recyclerview 에 레이아웃 매니저를 설정한다
        my_meetup_list_recyclerview.adapter = myMeetupListAdapter
        my_meetup_list_recyclerview.itemAnimator
    }

    /**
     * 내 그룹 리스트 출력
     */
    private fun myMeetupList() {
        val user_email = activity!!.getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none")  // SharedPreference 에서 이메일 정보를 가져온다.
        // Retrofit을 사용해 DB에 저장된 밋업 리스트를 불러온다.
        val call = RetrofitClient.getInstance().service.myMeetupList(user_email)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    myMeetupListItem.clear()  // 방송목록을 초기화한다.
                    val result = response.body()!!.string()  // meetuplist 정보를 받아온다.
                    Log.e("result", result)
                    val dataObject = JSONObject(result)
                    val meetup = dataObject.getString("meetup")
                    val dataArray = JSONArray(meetup)
                    for (i in 0 until dataArray.length()) {
                        val jsonObject = dataArray.getJSONObject(i)
                        val item = MeetupListItem()

                        item.meetup_id = jsonObject.getString("meetup_id")
                        item.meetup_title = jsonObject.getString("meetup_title")
                        item.meetup_content = jsonObject.getString("meetup_content")
                        item.meetup_picture = jsonObject.getString("meetup_picture")
                        item.meetup_creater = jsonObject.getString("meetup_creater")

                        myMeetupListItem.add(item)
                        Log.d("meetup_title", myMeetupListItem.get(i).meetup_title)
                    }

                    InitMyMeetup()  // 리사이클러뷰를 초기화한다.
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }
}
