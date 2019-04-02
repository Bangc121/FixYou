package com.example.kimjeonghwan.fixyou.ethereum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateWalletActivity extends AppCompatActivity {
    EditText et_password1;
    EditText et_password2;
    Button btn_generate;
    TextView tv_mgs;

    ProgressDialog genDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_wallet);

        et_password1 = findViewById(R.id.et_password1);
        et_password2 = findViewById(R.id.et_password2);
        btn_generate = findViewById(R.id.btn_generate);
        tv_mgs = findViewById(R.id.tv_mgs);

        genDialog = new ProgressDialog(GenerateWalletActivity.this);
        genDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        genDialog.setMessage("지갑생성중..");

        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("btn_generate", "지갑생성중");
                // show dialog
                genDialog.show();
                //genWallet();
                createWallet(et_password1.toString());
            }
        });

    }

    private void createWallet(final String password) {
        String[] result = new String[2];
        try {
            File path = new File(Environment.getExternalStorageDirectory().getPath() + "/Wallet");  //다운로드 path 가져오기
            if (!path.exists()) {
                path.mkdir();
            }
            String fileName = WalletUtils.generateLightNewWalletFile(password, new File(String.valueOf(path))); //지갑생성
            result[0] = path+"/"+fileName;

            Credentials credentials = WalletUtils.loadCredentials(password,result[0]);

            result[1] = credentials.getAddress();

            profileUpdate(fileName);

        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | IOException
                | CipherException e) {
            e.printStackTrace();
        }
    }

    // 키스토어 저장할때
    private void profileUpdate(String keystore){

        String user_email = getApplicationContext().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");  // SharedPreference 에서 이메일 정보를 가져온다.

        Log.e("key", user_email+","+keystore);
        Call<ResponseBody> call = RetrofitClient.getInstance().getService().profileUpdate(user_email, keystore);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.e("res", res);
                    // success값을 받아오면 지갑생성 완료
                    if(Objects.equals(res, "success")){
                        // SharedPreferences를 사용해 로그인정보를 저장한다.
                        SharedPreferences pref = getSharedPreferences("login_info", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("keystore", keystore);
                        editor.apply();
                        Intent intent = new Intent(GenerateWalletActivity.this, WalletActivity.class);
                        startActivity(intent);
                        genDialog.dismiss();
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(GenerateWalletActivity.this, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
