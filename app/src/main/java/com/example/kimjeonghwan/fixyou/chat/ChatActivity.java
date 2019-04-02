package com.example.kimjeonghwan.fixyou.chat;

import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kimjeonghwan.fixyou.live.bases.Constants.MESSAGE_TYPE_RECEIVE;
import static com.example.kimjeonghwan.fixyou.live.bases.Constants.MESSAGE_TYPE_SELF;

public class ChatActivity extends AppCompatActivity {

    Handler handler;
    SocketChannel socketChannel;
    private static final String HOST_URL = "52.79.228.68";
    private static final int PORT = 9812;
    private boolean isSender;

    EditText EditText_Message;
    Button Button_Send;

    RecyclerView recyclerView;
    ArrayList<ChatMessageItem> chatMessageItems;
    ChatMessageItem item;
    private String user_email;
    private String user_name;
    private String user_profile;
    private String user_message;

    // 저장된 채팅 내역 정보
    private String receive_profile;
    private String receive_name;
    String receive_message;
    String party_email;
    String party_name;
    String party_profile;
    String group;
    String roomId;
    JSONObject message_obj;

    //메시지 보낼때 JSON 객체
    JSONObject json_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chat_recyclerview);
        Button_Send = findViewById(R.id.Button_Send);
        EditText_Message = findViewById(R.id.EditText_Message);

        chatMessageItems = new ArrayList<>();

        // SharedPreference 를 통해 유저정보를 가져온다.
        user_email = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");
        user_name = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("name","none");
        user_profile = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("profile","none");

        // 채팅 할 상대방 정보
        Intent intent = getIntent();
        party_email = intent.getStringExtra("email");
        party_name = intent.getStringExtra("name");
        party_profile = intent.getStringExtra("profile");
        group = intent.getStringExtra("group");
        roomId = intent.getStringExtra("roomId");

        // 타이틀바 이름 변경
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(party_name);

        // 룸아이디를 통해 입장 할때
        if(roomId != null){
            // 기존 채팅 메세지 불러오기
            chatMessage(roomId);
        }

        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST_URL, PORT));

                    //방 참여할때 정보 전송
                    if(roomId == null){
                        // 방생성
                        JSONObject room_enter = new JSONObject();
                        try {
                            room_enter.put("method", "create_room");
                            room_enter.put("userEmail", user_email);
                            room_enter.put("receiveEmail", party_email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String send_room_user = room_enter.toString();

                        socketChannel
                                .socket()
                                .getOutputStream()
                                .write(send_room_user.getBytes("UTF-8"));
                    } else {
                        // 방 입장
                        JSONObject room_enter = new JSONObject();
                        try {
                            room_enter.put("method", "enter_room");
                            room_enter.put("userEmail", user_email);
                            room_enter.put("roomId", roomId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String send_room_user = room_enter.toString();

                        socketChannel
                                .socket()
                                .getOutputStream()
                                .write(send_room_user.getBytes("UTF-8"));
                    }

                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + "a");
                    ioe.printStackTrace();
                }
                checkUpdate.start();
            }
        }).start();

        // 메세지 전송 버튼
        Button_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String return_msg = EditText_Message.getText().toString();
                    if (!TextUtils.isEmpty(return_msg)) {
                        new SendmsgTask().execute(return_msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 레이아웃 초기화
     */
    private void initLayout(){
        recyclerView.setAdapter(new ChatMessageAdapter(chatMessageItems, getApplicationContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.scrollToPosition(new ChatMessageAdapter(chatMessageItems, getApplicationContext()).getItemCount()-1);
    }

    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                user_message = strings[0];
                isSender = true;  //메세지를 보내는 경우
                handler.post(showUpdate);
                json_message = new JSONObject();
                try {
                    json_message.put("method", "send_room");
                    json_message.put("name", user_name);
                    json_message.put("profile", user_profile);
                    json_message.put("content", user_message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String message = json_message.toString();

                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(message.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EditText_Message.setText("");
                }
            });
        }
    }

    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                // 서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); // 데이터받기
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                user_message = charset.decode(byteBuffer).toString();
                Log.e("user_message", user_message);
                isSender = false;  // 메세지를 받을 경우
                handler.post(showUpdate);
            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private Thread checkUpdate = new Thread() {
        public void run() {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable showUpdate = new Runnable() {
        public void run() {
            Log.e("showUpdate", String.valueOf(isSender));
            item = new ChatMessageItem();
            // 보낸 메세지와 받은 메세지를 구분한다.
            if(isSender) {
                JSONObject message = null;

                Log.e("getdata", String.valueOf(user_message));
                try {
                    message = new JSONObject(String.valueOf(json_message));
                    item.setContents(message.getString("content"));
                    item.setMessageType(MESSAGE_TYPE_SELF);
                    //item.setName(message.getString("userId"));
                    chatMessageItems.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //item.setContents(receive_message.getString("message"));
                //item.setName(receive_message.getString("sender"));

            } else {
                JSONObject message = null;
                try {
                    message = new JSONObject(user_message);
                    Log.e("getdata", String.valueOf(message));
                    //item.setContents(receive_message.getString("message"));
                    //item.setName(receive_message.getString("sender"));
                    item.setMessageType(MESSAGE_TYPE_RECEIVE);
                    item.setName(message.getString("name"));
                    item.setProfile(message.getString("profile"));
                    item.setContents(message.getString("content"));
                    //item.setName(message.getString("userId"));
                    //item.setProfile(party_profile);

                    chatMessageItems.add(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            initLayout();  // 리사이클러뷰를 초기화한다.
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chatMessage(String roomId) {
        Call<ResponseBody> call = RetrofitClient.getInstance().getService().chatMessage(roomId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    //프로필정보 받아옴
                    String res = response.body().string();
                    Log.e("chatMessage", res);
                    JSONObject json_profile = new JSONObject(res);
                    String profile_info = json_profile.getString("message");
                    JSONArray json_info = new JSONArray(profile_info);
                    Log.e("json_info", json_info.toString());
                    for (int i = 0; i < json_info.length(); i++) {
                        item = new ChatMessageItem();
                        //JSONObject jsonObject = json_info.getJSONObject(i);
                        JSONObject jsonObject = new JSONObject(json_info.get(i).toString());
                        String userEmail = jsonObject.getString("userEmail");
                        String profile = jsonObject.getString("profile");
                        String name = jsonObject.getString("name");
                        String content = jsonObject.getString("content");
                        if(user_email.equals(userEmail)){
                            item.setMessageType(MESSAGE_TYPE_SELF);
                            item.setContents(content);
                        } else {
                            item.setMessageType(MESSAGE_TYPE_RECEIVE);
                            item.setProfile(profile);
                            item.setName(name);
                            item.setContents(content);
                        }
                        chatMessageItems.add(item);
                    }
                    initLayout();  // 리사이클러뷰를 초기화한다.
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
