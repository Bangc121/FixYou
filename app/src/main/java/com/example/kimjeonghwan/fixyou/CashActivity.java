package com.example.kimjeonghwan.fixyou;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CashActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout cash_1_button;
    LinearLayout cash_2_button;
    LinearLayout cash_3_button;
    LinearLayout cash_4_button;
    LinearLayout cash_5_button;
    TextView user_balloon;
    String user_email;

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);

        cash_1_button = findViewById(R.id.cash_1_button);
        cash_2_button = findViewById(R.id.cash_2_button);
        cash_3_button = findViewById(R.id.cash_3_button);
        cash_4_button = findViewById(R.id.cash_4_button);
        cash_5_button = findViewById(R.id.cash_5_button);
        user_balloon = findViewById(R.id.user_balloon);

        cash_1_button.setOnClickListener(this);
        cash_2_button.setOnClickListener(this);
        cash_3_button.setOnClickListener(this);
        cash_4_button.setOnClickListener(this);
        cash_5_button.setOnClickListener(this);

        user_email = getApplicationContext().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");  // SharedPreference 에서 이메일 정보를 가져온다.

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cash_1_button:
                intent = new Intent(CashActivity.this, CashPurchaseActivity.class);
                intent.putExtra("account", "10");
                intent.putExtra("payment", "100");
                startActivity(intent);
                break;
            case R.id.cash_2_button:
                intent = new Intent(CashActivity.this, CashPurchaseActivity.class);
                intent.putExtra("account", "50");
                intent.putExtra("payment", "500");
                startActivity(intent);
                break;
            case R.id.cash_3_button:
                intent = new Intent(CashActivity.this, CashPurchaseActivity.class);
                intent.putExtra("account", "100");
                intent.putExtra("payment", "1100");
                startActivity(intent);
                break;
            case R.id.cash_4_button:
                intent = new Intent(CashActivity.this, CashPurchaseActivity.class);
                intent.putExtra("account", "300");
                intent.putExtra("payment", "3300");
                startActivity(intent);
                break;
            case R.id.cash_5_button:
                intent = new Intent(CashActivity.this, CashPurchaseActivity.class);
                intent.putExtra("account", "500");
                intent.putExtra("payment", "5500");
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        profileCall(user_email);
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
                    String balloon = jsonObject.getString("balloon");

                    // 보유 별풍선 적용
                    if(balloon == null){
                        user_balloon.setText("0");
                    }else {
                        user_balloon.setText(balloon);
                    }
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
