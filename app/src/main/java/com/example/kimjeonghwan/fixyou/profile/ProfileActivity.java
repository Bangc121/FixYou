package com.example.kimjeonghwan.fixyou.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.chat.ChatActivity;
import com.example.kimjeonghwan.fixyou.meetup.MeetupListItem;
import com.example.kimjeonghwan.fixyou.meetup.MyMeetupListAdapter;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    // 내그룹
    RecyclerView myMeetup_recyclerview;
    ArrayList<MeetupListItem> meetupListItems;
    MeetupListItem meetupListItem;

    // 관심사
    RecyclerView myInterest_recyclerview;
    ArrayList<InterestItem> interestItems;
    InterestItem interestItem;

    SwipeRefreshLayout swipeRefreshLayout;

    // 유저정보
    TextView profileName;
    TextView profileEmail;
    TextView profileNation;
    ImageView profileImage;

    String user_email;
    String user_name;
    String user_profile;
    String user_interest;

    // 버튼
    LinearLayout btn_message_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        myMeetup_recyclerview = findViewById(R.id.myMeetup_recyclerview);
        myInterest_recyclerview = findViewById(R.id.myInterest_recyclerview);
        swipeRefreshLayout = findViewById(R.id.swipe_profile);

        profileEmail = findViewById(R.id.profileEmail);
        profileName = findViewById(R.id.profileName);
        profileNation = findViewById(R.id.profileNation);
        profileImage = findViewById(R.id.profileImage);
        btn_message_send = findViewById(R.id.btn_message_send);

        btn_message_send.setOnClickListener(this);
        profileImage.setOnClickListener(this);

        meetupListItems = new ArrayList<>();  // 밋업 리스트
        interestItems = new ArrayList<>();  //관심사 리스트

        // 인텐트를 통해 넘어온 이메일값을 받는다.
        Intent intent = getIntent();
        user_email = intent.getStringExtra("email");
        if(user_email == null){
            // SharedPreference 를 통해 유저정보를 가져온다.
            user_email = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");
            user_name = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("name","none");
            user_profile = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("profile","none");
        }

        Log.e("profile_email", user_email);
        profileEmail.setText(user_email);
        profileCall(user_email);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_message_send:
                Intent intentChat = new Intent(ProfileActivity.this, ChatActivity.class);
                intentChat.putExtra("email", user_email);
                intentChat.putExtra("name", user_name);
                startActivity(intentChat);
                break;
            case R.id.profileImage:
                Intent intentProfile = new Intent(ProfileActivity.this, ProfileCameraActivity.class);
                startActivity(intentProfile);
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        myMeetupList();
    }
    /**
     * 내 관심사 초기화
     */
    private void initInterestList(){
        myInterest_recyclerview.setAdapter(new ProfileInterestListAdapter(interestItems, getApplicationContext()));
        //불규칙 레이아
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getApplicationContext());
        // Set flex direction.
        layoutManager.setFlexDirection(FlexDirection.ROW);
        // Set JustifyContent.
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        myInterest_recyclerview.setLayoutManager(layoutManager);
        myInterest_recyclerview.setItemAnimator(new DefaultItemAnimator());
    }


    /**
     * 내 관심사 가져오기
     */
    public void interestList() {
        // split 를 통해 관심사 가져옴
        String[] array = user_interest.split(",");

        //출력
        for (String anArray : array) {
            interestItem = new InterestItem();
            System.out.println(anArray);
            interestItem.setInterest(anArray);
            interestItems.add(interestItem);
        }
        initInterestList();
    }

    private void profileCall(String email) {
        Call<ResponseBody> call = RetrofitClient.getInstance().getService().profileCall(email);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    //프로필정보 받아옴
                    String res = response.body().string();
                    Log.e("profile", res);
                    JSONObject json_profile = new JSONObject(res);
                    String profile_info = json_profile.getString("profile");
                    JSONArray json_info = new JSONArray(profile_info);
                    JSONObject jsonObject = json_info.getJSONObject(0);
                    user_name = jsonObject.getString("name");
                    user_profile = jsonObject.getString("profile");
                    String nation = jsonObject.getString("nation");
                    user_interest = jsonObject.getString("interest");

                    interestList();
                    // 프로필정보 적용
                    profileName.setText(user_name);
                    profileEmail.setText(user_email);
                    profileNation.setText(nation);
                    Glide.with(getApplicationContext())
                            .load(user_profile)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImage);

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

    /**
     * 내 그룹 초기화
     */
    private void InitMyMeetup() {
        myMeetup_recyclerview.setAdapter(new MyMeetupListAdapter(meetupListItems, getApplicationContext()));
        myMeetup_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL, false));
        myMeetup_recyclerview.setItemAnimator(new DefaultItemAnimator());
    }

    private void myMeetupList() {
        String user_email = getApplicationContext().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");  // SharedPreference 에서 이메일 정보를 가져온다.

        Call<ResponseBody> call = RetrofitClient.getInstance().getService().myMeetupList(user_email);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    meetupListItems.clear();  // 모임목록을 초기화한다.
                    //프로필정보 받아옴
                    String res = response.body().string();
                    Log.e("meetup", res);
                    JSONObject json_profile = new JSONObject(res);
                    String profile_info = json_profile.getString("meetup");
                    JSONArray json_info = new JSONArray(profile_info);
                    Log.e("json_info", json_info.toString());
                    for (int i = 0; i < json_info.length(); i++) {
                        meetupListItem = new MeetupListItem();

                        JSONObject jsonObject = new JSONObject(json_info.get(i).toString());
                        meetupListItem.setMeetup_title(jsonObject.getString("meetup_title"));
                        meetupListItem.setMeetup_content(jsonObject.getString("meetup_content"));
                        meetupListItem.setMeetup_picture(jsonObject.getString("meetup_picture"));
                        meetupListItem.setMeetup_creater(jsonObject.getString("meetup_creater"));

                        meetupListItems.add(meetupListItem);
                    }
                    InitMyMeetup();
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
