package com.example.kimjeonghwan.fixyou.chat

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kimjeonghwan.fixyou.R
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.fragment_chat.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.ContextUtils.getApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException




/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ChatFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    private var chatListAdapter: ChatListAdapter? = null
    private var chatListItem: ArrayList<ChatListItem> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onResume() {
        super.onResume()
        chatList()
    }

    /**
     * 리사이클러뷰 초기화
     */
    private fun InitRecyclerview() {
        chatListAdapter = ChatListAdapter(chatListItem, context)
        chat_list_recyclerview.layoutManager = LinearLayoutManager(context) // chat_list_recyclerview 에 레이아웃 매니저를 설정한다
        chat_list_recyclerview.adapter = chatListAdapter
        chat_list_recyclerview.itemAnimator
    }

    /**
     * 친구추천 리스트 출력
     */
    private fun chatList() {
        val user_email = activity!!.getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none")  // SharedPreference 에서 이메일 정보를 가져온다.

        val call = RetrofitClient.getInstance().service.chatList(user_email)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) =
                    try {
                        chatListItem.clear()  // 채팅리스트 초기화
                        val res = response.body()!!.string()
                        Log.e("chat_room", res)
                        val json_freind = JSONObject(res)
                        val freind_info = json_freind.getString("chat_room")
                        val json_info = JSONArray(freind_info)
                        for (i in 0 until json_info.length()) {
                            val jsonObject = json_info.getJSONObject(i)
                            val item = ChatListItem()
                            val json_room = JSONObject(jsonObject.getString("room_message"))
                            Log.e("json_room", json_room.getString("name"))
                            if(user_email != jsonObject.getString("send_email")){
                                item.email = jsonObject.getString("send_email")  //상대방이메일
                                item.roomId = jsonObject.getString("room_id")  //룸아이디
                                item.content = json_room.getString("content")  //룸메세지
                                chatListItem.add(item)
                            } else if(user_email != jsonObject.getString("receive_email")){
                                item.email = jsonObject.getString("receive_email")  //상대방이메일
                                item.roomId = jsonObject.getString("room_id")  //룸아이디
                                item.content = json_room.getString("content")  //룸메세지
                                chatListItem.add(item)
                            }
                        }
                        InitRecyclerview()  //친구리스트를 초기화 한다.
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(getApplicationContext(), "Error" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
