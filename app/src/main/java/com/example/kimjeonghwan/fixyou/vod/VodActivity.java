package com.example.kimjeonghwan.fixyou.vod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VodActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar mProgressBar;  // 로딩 프로그래스바
    private ArrayList<VodItem> vodItems; // vod 정보 아이템
    private int position; // vod 정보 번호
    private String user_email;
    private String user_name;

    private static final String TAG = VodActivity.class.getSimpleName();

    private static class EventListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case Player.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE";
                    break;
                case Player.STATE_BUFFERING:
                    mProgressBar.setVisibility(View.VISIBLE);
                    stateString = "ExoPlayer.STATE_BUFFERING";
                    break;
                case Player.STATE_READY:
                    mProgressBar.setVisibility(View.GONE);
                    stateString = "ExoPlayer.STATE_READY";
                    break;
                case Player.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED";
                    break;
                default:
                    stateString = "UNKNOWN_STATE";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString + ", playWhenReady: " + playWhenReady);
        }
    }

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private SimpleExoPlayer exoPlayer;
    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    private EventListener eventListener = new EventListener();

    PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);

        mProgressBar = findViewById(R.id.mProgressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
        initPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void initPlayer() {
        String vod_position = vodItems.get(position).getVod_position();
        String url = vodItems.get(position).getVod_url();
        playerView = findViewById(R.id.playerView);

        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
        exoPlayer.addListener(new EventListener());


        MediaSource mediaSource = url.endsWith(".m3u8") ? buildMediaSourceHLS(Uri.parse(url)) : buildMediaSourceVideo(Uri.parse(url));
        exoPlayer.prepare(mediaSource, true, false);
        exoPlayer.setPlayWhenReady(playWhenReady);
        // 저장된 포지션값이 있으면 위치 적용시켜준다.
        if(vod_position != null){
            exoPlayer.seekTo(currentWindow, Long.parseLong(vod_position));
        } else {
            exoPlayer.seekTo(currentWindow, playbackPosition);
        }
        playerView.setPlayer(exoPlayer);
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentWindowIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.removeListener(eventListener);
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private MediaSource buildMediaSourceHLS(Uri uri) {
        String userAgent = System.getProperty("http.agent");
        Log.d(TAG, "UserAgent : " + userAgent);
        DataSource.Factory manifestDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(manifestDataSourceFactory).createMediaSource(uri);

        return new ConcatenatingMediaSource(hlsMediaSource);
    }

    private MediaSource buildMediaSourceVideo(Uri uri) {
        String userAgent = System.getProperty("http.agent");
        Log.d(TAG, "UserAgent : " + userAgent);
        ExtractorMediaSource videoSource =
                new ExtractorMediaSource.Factory(
                        new DefaultHttpDataSourceFactory(TextUtils.isEmpty(userAgent) ? "DefaultHttpDataSourceFactory" : userAgent)).
                        createMediaSource(uri);

        return new ConcatenatingMediaSource(videoSource);
    }

    // 저장된 데이터를 받아온다.
    protected void getData() {
        user_email = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");  // SharedPreference 에서 이메일 정보를 가져온다.
        user_name = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("name","none");  // SharedPreference 에서 이메일 정보를 가져온다.
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);

        Log.e("position", String.valueOf(position));
        vodItems = (ArrayList<VodItem>) intent.getSerializableExtra("vodItems");
    }

    // 뒤로 가기
    @Override
    public void onBackPressed() {
        playWhenReady = false;
        exoPlayer.setPlayWhenReady(playWhenReady);
        playbackPosition = exoPlayer.getCurrentPosition();
        Log.e("playbackPosition", String.valueOf(playbackPosition));
        showBackDialog(); // 다이얼로그를 띄운다.
    }

    private void showBackDialog(){
        AlertDialog.Builder stopDialog = new AlertDialog.Builder(VodActivity.this,R.style.myDialog);
        stopDialog.setTitle("종료")
                .setMessage("동영상 시청을 종료하시겠습니까?")
                .setPositiveButton("종료", (dialog, which) -> vodPosition())   // 종료 버튼을 누르게되면 서버와 Peer 연결을 끊는 disconnect() 메소드를 호출한다.
                .setNegativeButton("취소", (dialog, which) -> dialog.cancel())    // 취소 버튼을 누르게되면 다이얼로그를 취소한다.
                .show();
    }

    private void vodPosition(){
        String vodPosition = String.valueOf(playbackPosition);
        String vodId = vodItems.get(position).getVod_id();
        Call<ResponseBody> call = RetrofitClient.getInstance().getService().vodPosition(vodId, vodPosition, user_email, user_name);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.e("vodPosition", res);
                    // success값을 받아오면 vodPosition 저장 완료
                    if(Objects.equals(res, "success")){
                        finish();
                    }
                } catch (IOException e) {
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
