package com.example.kimjeonghwan.fixyou;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

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


public class TestActivity extends AppCompatActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    private static class EventListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case Player.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE";
                    break;
                case Player.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING";
                    break;
                case Player.STATE_READY:
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        String url = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8";
        PlayerView playerView = findViewById(R.id.playerView);

        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
        exoPlayer.addListener(new EventListener());


        MediaSource mediaSource = url.endsWith(".m3u8") ? buildMediaSourceHLS(Uri.parse(url)) : buildMediaSourceVideo(Uri.parse(url));
        exoPlayer.prepare(mediaSource, true, false);
        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.seekTo(currentWindow, playbackPosition);
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
}