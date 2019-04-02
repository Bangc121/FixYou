package com.example.kimjeonghwan.fixyou.vod

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import com.example.kimjeonghwan.fixyou.R
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.fragment_meetup.*
import okhttp3.ResponseBody

import kotlinx.android.synthetic.main.fragment_vod.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [VodFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [VodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VodFragment : Fragment() {

    private var vodListAdapter: VodListAdapter? = null
    private var vodItems: ArrayList<VodItem> = ArrayList()

    private var recentVodListAdapter: VodRecentListAdapter? = null
    private var recentVodItems: ArrayList<VodItem> = ArrayList()
    private var user_email: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onResume() {
        super.onResume()
        scrollviewVod.scrollTo(0, 0)
        refreshVod.visibility = View.VISIBLE
        getData()
        vodList()
        recentVodList()
        InitRecyclerview()  // 리사이클러뷰를 초기화한다.
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
            refreshVod.visibility = View.VISIBLE
            vodList()
            recentVodList()
            InitRecyclerview()  // 리사이클러뷰를 초기화한다.
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getData() {
        user_email = activity!!.getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none") // SharedPreference 에서 이메일 정보를 가져온다.
    }
    /**
     * 데이터 초기화
     */
    private fun InitRecyclerview() {
        vodListAdapter = VodListAdapter(vodItems, context)
        vod_list_recyclerview.layoutManager = LinearLayoutManager(context) // room_recyclerview 에 레이아웃 매니저를 설정한다
        vod_list_recyclerview.adapter = vodListAdapter
        vod_list_recyclerview.itemAnimator

        //가로 레이아웃
        recentVodListAdapter = VodRecentListAdapter(recentVodItems, context)
        vod_recent_recyclerview.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false) // room_recyclerview 에 레이아웃 매니저를 설정한다
        vod_recent_recyclerview.adapter = recentVodListAdapter
        vod_recent_recyclerview.itemAnimator

        refreshVod.visibility = View.GONE
    }

    /**
     * VOD 리스트 출력
     */
    private fun vodList() {
        // Retrofit을 사용해 DB에 저장된 VOD 리스트를 불러온다.
        val call = RetrofitClient.getInstance().service.VodList("bangc")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    vodItems.clear()  // 방송목록을 초기화한다.
                    val result = response.body()!!.string()  // broadcast 정보를 받아온다.
                    val dataObject = JSONObject(result)
                    val broadcast = dataObject.getString("vod")
                    val dataArray = JSONArray(broadcast)
                    for (i in 0 until dataArray.length()) {
                        val jsonObject = dataArray.getJSONObject(i)
                        val item = VodItem()
                        item.vod_id = jsonObject.getString("vod_id")
                        item.vod_name = jsonObject.getString("vod_name")
                        item.vod_creator = jsonObject.getString("vod_creator")
                        item.vod_viewer = jsonObject.getString("vod_viewer")
                        item.vod_url = jsonObject.getString("vod_url")
                        item.vod_thumbnail = jsonObject.getString("vod_thumbnail")
                        Log.e("thumbnail", jsonObject.getString("vod_thumbnail"))
                        vodItems.add(item)
                    }
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
     * VOD 리스트 출력
     */
    private fun recentVodList() {
        // Retrofit을 사용해 DB에 저장된 VOD 리스트를 불러온다.
        val call = RetrofitClient.getInstance().service.recentVodList(user_email)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    recentVodItems.clear()  // 방송목록을 초기화한다.
                    val result = response.body()!!.string()  // broadcast 정보를 받아온다.
                    val dataObject = JSONObject(result)
                    val broadcast = dataObject.getString("recent")
                    val dataArray = JSONArray(broadcast)
                    for (i in 0 until dataArray.length()) {
                        val jsonObject = dataArray.getJSONObject(i)
                        val item = VodItem()
                        item.vod_id = jsonObject.getString("vod_id")
                        item.vod_position = jsonObject.getString("vod_position")
                        item.vod_name = jsonObject.getString("vod_name")
                        item.vod_creator = jsonObject.getString("vod_creator")
                        item.vod_url = jsonObject.getString("vod_url")
                        item.vod_thumbnail = jsonObject.getString("vod_thumbnail")
                        recentVodItems.add(item)
                    }
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
