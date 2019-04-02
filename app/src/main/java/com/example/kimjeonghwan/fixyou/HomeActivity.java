package com.example.kimjeonghwan.fixyou;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kimjeonghwan.fixyou.ethereum.CreateWalletActivity;
import com.example.kimjeonghwan.fixyou.ethereum.GenerateWalletActivity;
import com.example.kimjeonghwan.fixyou.live.BroadCasterActivity;
import com.example.kimjeonghwan.fixyou.signup.SignUpActivity;
import com.example.kimjeonghwan.fixyou.signup.SignUpFirstActivity;
import com.example.kimjeonghwan.fixyou.vod.VodActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    //kakao
    SessionCallback callback;
    LoginButton kakao_login;
    //naver
    public static OAuthLogin mOAuthLoginModule;
    OAuthLoginButton naver_login;
    //google
    private static final int RC_SIGN_IN = 10;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    SignInButton google_login;

    LinearLayout google_login_button;
    LinearLayout naver_login_button;
    LinearLayout kakao_login_button;
    LinearLayout email_login_button;
    TextView email_signup_button;

    private static final String TAG = "HomeActivity";
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setGoogle();  //google
        setNaver();  //naver
        setKakao();  //kakao

        google_login_button = findViewById(R.id.google_login_button);
        naver_login_button = findViewById(R.id.naver_login_button);
        kakao_login_button = findViewById(R.id.kakao_login_button);
        email_login_button = findViewById(R.id.email_login_button);
        email_signup_button = findViewById(R.id.email_signup_button);

        google_login_button.setOnClickListener(this);
        naver_login_button.setOnClickListener(this);
        kakao_login_button.setOnClickListener(this);
        email_login_button.setOnClickListener(this);
        email_signup_button.setOnClickListener(this);
        mContext = this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.kakao_login_button:
                kakao_login.performClick();
                break;
            case R.id.naver_login_button:
                naver_login.performClick();
                break;
            case R.id.email_login_button:
                Intent intentLogin = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intentLogin);
                break;
            case R.id.email_signup_button:
                Intent intentSignUp = new Intent(HomeActivity.this, SignUpFirstActivity.class);
                startActivity(intentSignUp);
                break;
        }
    }

    private void setGoogle() {
        google_login = findViewById(R.id.google_login);
        mAuth = FirebaseAuth.getInstance();//싱글톤 패턴으로 작동이된다 '이해안감'
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setKakao() {
        kakao_login = findViewById(R.id.kakao_login);
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
    }

    private void setNaver() {
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(this, "f0Soqo75nGaxmN9xVdVh", "sYq1A0jePO", "네이버 아이디로 로그인 테스트");
        naver_login = findViewById(R.id.naver_login);
        naver_login.setOAuthLoginHandler(mOAuthLoginHandler);
    }

    // 사용자의 로그인 정보를 저장하는 함수
    private void saveProfile(String user_name, String user_email, String user_profile) {
        // SharedPreferences를 사용해 로그인정보를 저장한다.
        SharedPreferences pref = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("name", user_name);
        editor.putString("email", user_email);
        editor.putString("profile", user_profile);
        editor.apply();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    //구글 로그인 Intent Result값 반환되는 로직
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) { //구글버튼 로그인 누르고, 구글사용자 확인되면 실행되는 로직
                // 구글 로그인이 성공적으로 완료, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account); //구글이용자 확인된 사람정보 파이어베이스로 넘기기
                saveProfile(account.getDisplayName(), account.getEmail(), String.valueOf(account.getPhotoUrl()));
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    //파이어베이스로 값넘기기
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        //파이어베이스로 받은 구글사용자가 확인된 이용자의 값을 토큰으로 받고
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        //Log.d(TAG, "signInWithCredential:success");
                        //   FirebaseUser user = mAuth.getCurrentUser();
                        // updateUI(user);
                        Toast.makeText(HomeActivity.this, "아이디 생성완료", Toast.LENGTH_SHORT).show();
                    } else {
                        // If sign in fails, display a message to the user.
                        //Log.w(TAG, "signInWithCredential:failure", task.getException());
                        // Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
                        //          Toast.LENGTH_SHORT).show();
                        //  updateUI(null);
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * OAuthLoginHandler를 startOAuthLoginActivity() 메서드 호출 시 파라미터로 전달하거나 OAuthLoginButton
     객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다.
     */
    @SuppressLint("HandlerLeak")
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginModule.getAccessToken(mContext);

                naverProflieTask naverProflieTask = new naverProflieTask();  // naver 프로필정보를 불러오는 클래스 선언
                naverProflieTask.execute(accessToken);  // naver 프로필정보를 불러오기 위해 토큰값을 넘긴다
            } else {
                String errorCode = mOAuthLoginModule.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginModule.getLastErrorDesc(mContext);
                Toast.makeText(getApplicationContext(), "errorCode:" + errorCode
                        + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    };

    // 토큰값을 통해 naver 프로필정보를 가져오는 클래스
    @SuppressLint("StaticFieldLeak")
    class naverProflieTask extends AsyncTask<String, Void, String> {
        String result;
        @Override
        protected String doInBackground(String... strings) {
            String token = strings[0];// 네이버 로그인 접근 토큰;
            String header = "Bearer " + token; // Bearer 다음에 공백 추가
            try {
                String apiURL = "https://openapi.naver.com/v1/nid/me";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", header);
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                result = response.toString();
                br.close();
                System.out.println(response.toString());
            } catch (Exception e) {
                System.out.println(e);
            }
            //result 값은 JSONObject 형태로 넘어옵니다.
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                //넘어온 result 값을 JSONObject 로 변환해주고, 값을 가져온다.
                JSONObject object = new JSONObject(result);
                if(object.getString("resultcode").equals("00")) {
                    JSONObject jsonObject = new JSONObject(object.getString("response"));
                    Log.d("mane", jsonObject.getString("name"));
                    saveProfile(jsonObject.getString("name"), jsonObject.getString("email"), jsonObject.getString("profile"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // kakao 프로필정보를 가져오는 클래스
    class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.requestMe(new MeResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        //에러로 인한 로그인 실패
                        // finish();
                    } else {
                        //redirectMainActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                @Override
                public void onNotSignedUp() {
                }

                @SuppressLint("ApplySharedPref")
                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.
                    //editor.putString("email", userProfile.getNickname());
                    Log.d("profile", userProfile.toString());
                    saveProfile(userProfile.getNickname(), userProfile.getUUID(), userProfile.getProfileImagePath());
                    Intent intentLogin = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intentLogin);
                }
            });
        }
        // 세션 실패시
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
        }
    }

}
