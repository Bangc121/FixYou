package com.example.kimjeonghwan.fixyou.live

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.example.kimjeonghwan.fixyou.R
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.fragment_broadcast.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import android.view.MenuInflater


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BroadcastFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BroadcastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BroadCastFragment : Fragment() {

    private var broadCastListAdapter: BroadCastListAdapter? = null
    private var broadCastItems: ArrayList<BroadCastItem> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_broadcast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 세션 로그아웃
/*        logout.setOnClickListener(View.OnClickListener {
            onClickLogout()
            mOAuthLoginModule.logout(activity)
        })*/
    }

    override fun onResume() {
        super.onResume()
        refreshBroadcast.visibility = View.VISIBLE
        broadcastList()
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
            refreshBroadcast.visibility = View.VISIBLE
            broadcastList()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 데이터 초기화
     */
    private fun InitRecyclerview() {
        broadCastListAdapter = BroadCastListAdapter(broadCastItems, context)
        broadcast_room_recyclerview.layoutManager = LinearLayoutManager(context) // room_recyclerview 에 레이아웃 매니저를 설정한다
        broadcast_room_recyclerview.adapter = broadCastListAdapter
        broadcast_room_recyclerview.itemAnimator

        refreshBroadcast.visibility = View.GONE
    }

    /**
     * 방송리스트 출력
     */
    private fun broadcastList() {
        // Retrofit을 사용해 DB에 저장된 방송리스트를 불러온다.
        val call = RetrofitClient.getInstance().service.BroadCastList("bangc")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    broadCastItems.clear()  // 방송목록을 초기화한다.
                    val result = response.body()!!.string()  // broadcast 정보를 받아온다.
                    val dataObject = JSONObject(result)
                    val broadcast = dataObject.getString("broadcast")
                    val dataArray = JSONArray(broadcast)
                    for (i in 0 until dataArray.length()) {
                        val jsonObject = dataArray.getJSONObject(i)
                        val item = BroadCastItem()
                        item.broadcast_name = jsonObject.getString("broadcast_name")
                        item.broadcast_sessionid = jsonObject.getString("broadcast_sessionid")
                        item.broadcast_creator = jsonObject.getString("broadcast_creator")
                        item.broadcast_viewer = jsonObject.getString("broadcast_viewer")
                        item.broadcast_thumbnail = jsonObject.getString("broadcast_thumbnail")
                        Log.e("jsonObject",jsonObject.getString("broadcast_thumbnail"))
                        broadCastItems.add(item)
                    }

                    InitRecyclerview()  // 리사이클러뷰를 초기화한다.
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
