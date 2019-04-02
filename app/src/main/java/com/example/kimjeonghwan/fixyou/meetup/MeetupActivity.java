package com.example.kimjeonghwan.fixyou.meetup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetupActivity extends AppCompatActivity {

    // 그룹맴버
    RecyclerView meetup_member_recyclerview;
    ArrayList<MeetupMemberItem> meetupMemberItems;
    MeetupMemberItem meetupMemberItem;

    ImageView meetup_image;
    TextView meetup_title;
    TextView meetup_creater;
    Button meetup_join;
    ProgressBar join_progress;
    private String meetupId;
    private Boolean meetupJoin;
    private TextView meetupNumber;
    private LinearLayout ButtonLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup);

        ButtonLayout = findViewById(R.id.ButtonLayout);
        meetup_member_recyclerview = findViewById(R.id.meetup_member_recyclerview);
        meetup_image = findViewById(R.id.meetup_image);
        meetup_title = findViewById(R.id.meetup_title);
        meetup_creater = findViewById(R.id.meetup_creater);
        meetup_join = findViewById(R.id.meetup_join);
        meetupNumber = findViewById(R.id.meetupNumber);
        join_progress = findViewById(R.id.join_progress);

        meetupMemberItems = new ArrayList<>();

        // 버튼 텍스트가 가입 및 참가신청일때 가입 할 수 있다.
        if(meetup_join.getText().equals("가입 및 참가신청")){
            meetup_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgress(true);
                    meetupJoin(meetupId);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress(true);
        // 그룹정보 가져오기
        Intent intent = getIntent();
        meetupJoin = intent.getBooleanExtra("join", false);
        meetupId = intent.getStringExtra("id");
        String picture = intent.getStringExtra("picture");
        String title = intent.getStringExtra("title");
        String creater = intent.getStringExtra("creater");

        if(meetupJoin){
            ButtonLayout.setVisibility(View.GONE);
        }
        // 그룹정보
        Glide.with(getApplicationContext())
                .load(picture)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(25)))
                .into(meetup_image);
        meetup_title.setText(title);
        meetup_creater.setText(creater);
        meetupInfo();
    }

    private void meetupJoin(String id){
        String email = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");  // SharedPreference 에서 이메일 정보를 가져온다.

        Call<ResponseBody> call = RetrofitClient.getInstance().getService().meetupJoin(id, email);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.e("res", res);
                    // success값을 받아오면 회원가입 성공
                    if(Objects.equals(res, "success")){
                        meetup_join.setText("가입완료");
                        showProgress(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MeetupActivity.this, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 그룹맴버 초기화
     */
    private void initMember() {
        meetup_member_recyclerview.setAdapter(new MeetupMemberAdapter(meetupMemberItems, getApplicationContext()));
        meetup_member_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL, false));
        meetup_member_recyclerview.setItemAnimator(new DefaultItemAnimator());

        showProgress(false);
    }

    private void meetupInfo() {
        Log.e("meetupinfo", meetupId);

        Call<ResponseBody> call = RetrofitClient.getInstance().getService().meetupInfo(meetupId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    //프로필정보 받아옴
                    String res = response.body().string();
                    Log.e("meetupinfo", res);
                    JSONObject json_profile = new JSONObject(res);
                    String profile_info = json_profile.getString("meetupinfo");
                    JSONArray json_info = new JSONArray(profile_info);
                    meetupNumber.setText(String.valueOf(json_info.length()));
                    for (int i = 0; i < json_info.length(); i++) {
                        meetupMemberItem = new MeetupMemberItem();

                        JSONObject jsonObject = new JSONObject(json_info.get(i).toString());
                        meetupMemberItem.setName(jsonObject.getString("name"));
                        meetupMemberItem.setEmail(jsonObject.getString("email"));
                        meetupMemberItem.setProfile(jsonObject.getString("profile"));

                        meetupMemberItems.add(meetupMemberItem);
                    }
                    initMember();
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

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            join_progress.setVisibility(show ? View.GONE : View.VISIBLE);
            join_progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    join_progress.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            join_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            join_progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    join_progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            join_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            join_progress.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
