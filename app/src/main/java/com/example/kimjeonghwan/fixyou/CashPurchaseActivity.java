package com.example.kimjeonghwan.fixyou;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;
import com.example.kimjeonghwan.fixyou.signup.SignUpActivity;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import kr.co.bootpay.Bootpay;
import kr.co.bootpay.BootpayAnalytics;
import kr.co.bootpay.CancelListener;
import kr.co.bootpay.CloseListener;
import kr.co.bootpay.ConfirmListener;
import kr.co.bootpay.DoneListener;
import kr.co.bootpay.ErrorListener;
import kr.co.bootpay.ReadyListener;
import kr.co.bootpay.enums.PG;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CashPurchaseActivity extends AppCompatActivity {
    private int stuck = 10;

    LinearLayout kakao_account_button;
    TextView balloon_count;
    TextView balloon_total;
    TextView balloon_payment;
    Spinner product_count;
    String total_amount;
    String total_payment;
    int total;
    String count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_purchase);

        kakao_account_button = findViewById(R.id.kakao_account_button);
        balloon_count = findViewById(R.id.balloon_count);
        balloon_total = findViewById(R.id.balloon_total);
        balloon_payment = findViewById(R.id.balloon_payment);
        product_count = findViewById(R.id.product_count);

        // 결제할 별풍선 갯수, 금액
        Intent intent = getIntent();
        String amount = intent.getStringExtra("account");
        String payment = intent.getStringExtra("payment");

        balloon_count.setText(amount);
        balloon_total.setText(amount);
        balloon_payment.setText(payment);

        // 상품갯수 선택
        product_count.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                count = parent.getItemAtPosition(position).toString();
                Log.e("count", count);
                int a = Integer.parseInt(count);
                int b = Integer.parseInt(amount);
                int c = Integer.parseInt(payment);
                total = a * c;
                total_amount = String.valueOf(a * b);
                total_payment = String.valueOf(total);
                balloon_total.setText(total_amount);
                balloon_payment.setText(total_payment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 초기설정 - 해당 프로젝트(안드로이드)의 application id 값을 설정합니다. 결제와 통계를 위해 꼭 필요합니다.
        BootpayAnalytics.init(this, "59a4d326396fa607cbe75de5");


        kakao_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick_request(v);
            }
        });
    }
    public void onClick_request(View v) {
        // 결제호출
        Bootpay.init(getFragmentManager())
                .setApplicationId("5c1a8ac3396fa648ae77e3e7") // 해당 프로젝트(안드로이드)의 application id 값
                .setPG(PG.KAKAO) // 결제할 PG 사
                //.setMethod() // 결제수단
                .setName("별풍선") // 결제할 상품명
                .setOrderId("1234") //고유 주문번호로, 생성하신 값을 보내주셔야 합니다.
                .setPrice(total) // 결제할 금액
                //.setAccountExpireAt("2018-09-22") // 가상계좌 입금기간 제한 ( yyyy-mm-dd 포멧으로 입력해주세요. 가상계좌만 적용됩니다. 오늘 날짜보다 더 뒤(미래)여야 합니다 )
                .setQuotas(new int[] {0,2,3}) // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)
                .addItem("마우스", 1, "ITEM_CODE_MOUSE", 100) // 주문정보에 담길 상품정보, 통계를 위해 사용
                .addItem("키보드", 1, "ITEM_CODE_KEYBOARD", 200, "패션", "여성상의", "블라우스") // 주문정보에 담길 상품정보, 통계를 위해 사용
                .onConfirm(new ConfirmListener() { // 결제가 진행되기 바로 직전 호출되는 함수로, 주로 재고처리 등의 로직이 수행
                    @Override
                    public void onConfirm(@Nullable String message) {
                        if (0 < stuck) Bootpay.confirm(message); // 재고가 있을 경우.
                        else Bootpay.removePaymentWindow(); // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                        Log.d("confirm", message);
                    }
                })
                .onDone(new DoneListener() { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                    @Override
                    public void onDone(@Nullable String message) {
                        Log.d("done", message);
                        purchaseBalloon();
                    }
                })
                .onReady(new ReadyListener() { // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                    @Override
                    public void onReady(@Nullable String message) {
                        Log.d("ready", message);
                    }
                })
                .onCancel(new CancelListener() { // 결제 취소시 호출
                    @Override
                    public void onCancel(@Nullable String message) {
                        Log.d("cancel", message);
                    }
                })
                .onError(new ErrorListener() { // 에러가 났을때 호출되는 부분
                    @Override
                    public void onError(@Nullable String message) {
                        Log.d("error", message);
                    }
                })
                .onClose(new CloseListener() { //결제창이 닫힐때 실행되는 부분
                    @Override
                    public void onClose(String message) {
                        Log.d("close", "close");
                    }
                })
                .show();
    }

    //회원가입완료 했을때
    private void purchaseBalloon(){

        String user_email = getApplicationContext().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");  // SharedPreference 에서 이메일 정보를 가져온다.

        Call<ResponseBody> call = RetrofitClient.getInstance().getService().purchaseBalloon(user_email, total_payment);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.e("res", res);
                    // success값을 받아오면 회원가입 성공
                    if(Objects.equals(res, "success")){
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(CashPurchaseActivity.this, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
