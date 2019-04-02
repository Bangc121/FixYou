package com.example.kimjeonghwan.fixyou.live;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.chat.ChatMessageItem;
import com.example.kimjeonghwan.fixyou.ethereum.utils.InfoDialog;
import com.example.kimjeonghwan.fixyou.live.kurento.KurentoPresenterRTCClient;
import com.example.kimjeonghwan.fixyou.live.kurento.models.CandidateModel;
import com.example.kimjeonghwan.fixyou.live.kurento.models.response.ServerResponse;
import com.example.kimjeonghwan.fixyou.live.kurento.models.response.TypeResponse;
import com.example.kimjeonghwan.fixyou.utils.RxScheduler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.kacper.smoothcamerabutton.SmoothCameraButton;
import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.DefaultSocketService;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionClient;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionParameters;
import com.nhancv.webrtcpeer.rtc_peer.SignalingEvents;
import com.nhancv.webrtcpeer.rtc_peer.StreamMode;
import com.nhancv.webrtcpeer.rtc_peer.config.DefaultConfig;
import com.nhancv.webrtcpeer.rtc_peer.SignalingParameters;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;
import com.nhancv.webrtcpeer.rtc_plugins.RTCAudioManager;

import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import static com.example.kimjeonghwan.fixyou.live.bases.Constants.MESSAGE_TYPE_BALLOON;
import static com.example.kimjeonghwan.fixyou.live.bases.Constants.MESSAGE_TYPE_SELF;

/**
 * 라이브 방송을 진행하는 방송 송출자용 Activity
 */

public class BroadCasterActivity extends AppCompatActivity implements View.OnClickListener, SignalingEvents, PeerConnectionClient. PeerConnectionEvents {

    private static final String TAG = BroadCasterActivity.class.getSimpleName(); // 로그 출력을 위한 태그 설정. 현재 클래스의 이름으로 저장함.
    private static final String STREAM_HOST = "wss://52.79.228.68:8443/one2many";

    private SocketService socketService;
    private Gson gson;

    private KurentoPresenterRTCClient rtcClient;
    private DefaultConfig defaultConfig;
    private PeerConnectionParameters peerConnectionParameters;
    private PeerConnectionClient peerConnectionClient;
    private SignalingParameters signalingParameters;
    private RTCAudioManager audioManager;

    private SurfaceViewRenderer vGLSurfaceViewCall;

    private ProxyRenderer localProxyRenderer;
    private EglBase rootEglBase;
    private Toast logToast;

    private boolean iceConnected;

    RecyclerView recyclerView;
    ArrayList<ChatMessageItem> chatMessageItems;
    ChatMessageItem item;

    ImageView chatButton;
    SmoothCameraButton cameraButton;
    TextView broadcastName;
    EditText broadcast_editText_message;
    LinearLayout broadcast_name_edit, recyclerview_layout;
    FrameLayout message_layout, button_layout;
    Button broadcast_button_send;
    String sendMessage;
    String userEmail, userName;
    SharedPreferences pref;

    LottieAnimationView animationView;  //별풍선 애니메이션 설정
    private InfoDialog mInfoDialog;   // 생방송 시작 다이얼로그 설정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);

        recyclerView = findViewById(R.id.broadcast_chat_recyclerview);
        broadcast_button_send = findViewById(R.id.broadcast_button_send);
        broadcast_name_edit = findViewById(R.id.broadcast_name_edit);
        broadcast_editText_message = findViewById(R.id.broadcast_editText_message);
        cameraButton = findViewById(R.id.cameraButton);
        chatButton = findViewById(R.id.chatButton);
        broadcastName = findViewById(R.id.broadcastName);
        message_layout = findViewById(R.id.message_layout);
        button_layout = findViewById(R.id.button_layout);
        recyclerview_layout = findViewById(R.id.recyclerview_layout);
        animationView = findViewById(R.id.animation_view); // 로티 애니메이션 설정

        animationView.setAnimation(R.raw.balloons_with_string);

        mInfoDialog = new InfoDialog(this);
        chatMessageItems = new ArrayList<>();
        broadcast_name_edit.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
        recyclerview_layout.setOnClickListener(this);
        broadcast_button_send.setOnClickListener(this);

        pref = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE);
        userEmail = pref.getString("email", "none");
        userName = pref.getString("name", "none");

        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.broadcast_name_edit:
                AlertDialog.Builder broadcast_name_dialog = new AlertDialog.Builder(BroadCasterActivity.this);
                broadcast_name_dialog.setTitle("방송 제목");       // 제목 설정
                // EditText 삽입하기
                final EditText broadcast_name = new EditText(BroadCasterActivity.this);
                broadcast_name_dialog.setView(broadcast_name);
                // 확인 버튼 설정
                broadcast_name_dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "Yes Btn Click");
                        // Text 값 받아서 로그 남기기
                        String value = broadcast_name.getText().toString();
                        broadcastName.setText(value);
                        dialog.dismiss();     //닫기
                        // Event
                    }
                });
                // 취소 버튼 설정
                broadcast_name_dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG,"No Btn Click");
                        dialog.dismiss();     //닫기
                        // Event
                    }
                });
                // 창 띄우기
                broadcast_name_dialog.show();
                break;
            case R.id.broadcast_button_send:
                sendMessage();
                break;
            case R.id.cameraButton:
                if(!cameraButton.isSelected()){
                    mInfoDialog.Get("생방송 준비 중입니다.", "잠시만 기다려주세요.");
                    cameraButton.setSelected(true);
                    rtcClient.createRoom(broadcastName.getText().toString(), userName);
                }else {
                    cameraButton.setSelected(false);
                    rtcClient.deleteRoom(broadcastName.getText().toString(), userName);
                }
                break;
            case R.id.chatButton:
                // 버튼레이아웃이 visible 일 경우
                if(button_layout.getVisibility() == View.VISIBLE){
                    button_layout.setVisibility(View.GONE);
                    message_layout.setVisibility(View.VISIBLE);
                    broadcast_editText_message.post(new Runnable() {
                        @Override
                        public void run() {
                            broadcast_editText_message.setFocusableInTouchMode(true);
                            broadcast_editText_message.requestFocus();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(broadcast_editText_message,0);
                        }
                    });
                }
                break;
            case R.id.recyclerview_layout:
                // 사용자가 채팅중에 채팅 화면을 눌렀을때
                if(message_layout.getVisibility() == View.VISIBLE){
                    message_layout.setVisibility(View.GONE);
                    button_layout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if(cameraPermission == PackageManager.PERMISSION_GRANTED){
            //presenter.startCall();
            startCall();
            Log.e(TAG, "start call");
        }else{
            Log.e(TAG, "camera permission error");
        }
    }

    protected void init() {
        socketService = new DefaultSocketService(getApplication());
        gson = new Gson();

        vGLSurfaceViewCall = findViewById(R.id.BroadCastSurfaceView);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        // SurfaceView 와 렌더러를 설정한다.
        localProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        localProxyRenderer.setTarget(vGLSurfaceViewCall);  // localProxyRenderer 의 타겟을 서페이스 뷰로 설정한다.

        initPeerConfig();
    }

    //초기화
    public void initPeerConfig() {
        rtcClient = new KurentoPresenterRTCClient(socketService);
        defaultConfig = new DefaultConfig();
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.SEND_ONLY);
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, this);
        peerConnectionClient.setVideoEnabled(true);
    }

    // 채팅 메세지를 보내기 위한 메소드
    public void sendMessage() {
        RxScheduler.runOnUi(o -> {
            sendMessage = broadcast_editText_message.getText().toString( );  // 메시지 내용을 sendMessage에 저장
            rtcClient.sendMessage(sendMessage, userName);  // 메세지 내용과 유저네임을 서버에 전송
            broadcast_editText_message.setText(null);  // 전송 후, EditText 초기화
            receiveChatMessage(userName, sendMessage);  // 자신이 보낸 메세지는 서버에서 받지 않고 클라이언트단에서 바로 리스트에 추가해준다.
        });
    }

    // 로티 에니메이션을 실행하는 메소드
    public void showAnimation() {
        RxScheduler.runOnUi(o -> {
            //풍선 애니메이션을 실행시킨다.
            animationView.playAnimation();
            //풍선 애니메이션을 제어한다.
            animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationView.cancelAnimation();
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        });
    }
    /**
     * 채팅 레이아웃 초기화
     */
    private void initChatLayout(){
        recyclerView.setAdapter(new BroadCastChatAdapter(chatMessageItems, getApplicationContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.scrollToPosition(new BroadCastChatAdapter(chatMessageItems, getApplicationContext()).getItemCount()-1);
    }

    /**
     * 채팅 메세지를 받을 경우 리스트에 추가하고 리사이클러뷰에 추가한다.
     */
    private void receiveChatMessage(String email, String message) {
        RxScheduler.runOnUi(o -> {
            item = new ChatMessageItem();
            item.setName(email);
            item.setContents(message);
            item.setMessageType(MESSAGE_TYPE_SELF);
            chatMessageItems.add(item);
            initChatLayout();
        });
    }

    private void receiveBalloon(String email, String message) {
        RxScheduler.runOnUi(o -> {
            item = new ChatMessageItem();
            item.setName(email);
            item.setBalloon(message);
            item.setMessageType(MESSAGE_TYPE_BALLOON);
            chatMessageItems.add(item);
            initChatLayout();
        });
    }

    private void showDisconnectDialog(){
        RxScheduler.runOnUi(o -> {
            AlertDialog.Builder stopDialog = new AlertDialog.Builder(BroadCasterActivity.this,R.style.myDialog);
            stopDialog.setTitle("방송 종료")
                    .setMessage("방송을 종료하시겠습니까?")
                    .setPositiveButton("종료", (dialog, which) -> disconnect())   // 종료 버튼을 누르게되면 서버와 Peer 연결을 끊는 disconnect() 메소드를 호출한다.
                    .setNegativeButton("취소", (dialog, which) -> dialog.cancel())    // 취소 버튼을 누르게되면 다이얼로그를 취소한다.
                    .show();
        });
    }

    public void disconnect() {
        if (rtcClient != null) {
            rtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }

        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }

        if (socketService != null) {
            socketService.close();
        }

        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }

        finish();
    }

    public void startCall() {
        if (rtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }

        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                super.onOpen(serverHandshake);
                RxScheduler.runOnUi(o -> {
                    logAndToast("Socket connected");
                });
                SignalingParameters parameters = new SignalingParameters(
                        new LinkedList<PeerConnection.IceServer>() {
                            {
                                add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
                            }
                        }, true, null, null, null, null, null);
                onSignalConnected(parameters);  // 카메라를 연결한다.
            }

            @Override
            public void onMessage(String serverResponse_) {
                super.onMessage(serverResponse_);
                try {
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);

                    switch (serverResponse.getIdRes()) {
                        case PRESENTER_RESPONSE:
                            if (serverResponse.getTypeRes() == TypeResponse.REJECTED) {
                                RxScheduler.runOnUi(o -> {
                                    logAndToast(serverResponse.getMessage());
                                });
                            } else {
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
                                        serverResponse.getSdpAnswer());
                                onRemoteDescription(sdp);
                            }

                            break;

                        case ICE_CANDIDATE:
                            CandidateModel candidateModel = serverResponse.getCandidate();
                            onRemoteIceCandidate(
                                    new IceCandidate(candidateModel.getSdpMid(), candidateModel.getSdpMLineIndex(),
                                            candidateModel.getSdp()));
                            break;

                        case CHAT_RESPONSE:
                            Log.d("messageeeee", serverResponse.getFrom() + serverResponse.getMessage());
                            receiveChatMessage(serverResponse.getFrom(), serverResponse.getMessage());
                            break;

                        case BALLOON_RESPONSE:
                            Log.d("BALLOON_RESPONSE", serverResponse.getFrom() + serverResponse.getBalloon());
                            showAnimation();
                            receiveBalloon(serverResponse.getFrom(), serverResponse.getBalloon());
                            break;

                        case CREATE_RESPONSE:
                            Log.d("CREATE_RESPONSE", "CREATE_RESPONSE");
                            mInfoDialog.Dismiss();
                            break;

                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                super.onClose(i, s, b);
                RxScheduler.runOnUi(o -> {
                    logAndToast("Socket closed");
                    disconnect();
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                RxScheduler.runOnUi(o -> {
                    logAndToast(e.getMessage());
                    disconnect();
                });
            }

        });

        // 오디오 라우팅 (오디오 모드, 오디오 장치 열거 등)을 처리할 오디오 관리자 만듬.
        audioManager = RTCAudioManager.create(getApplicationContext());
        // 기존 오디오 설정 저장 및 오디오 모드 변경
        // 가능한 최상의 VoIP 성능을 제공
        Log.d(TAG,"Starting audio manager");
        audioManager.start((audioDevice, availableAudioDevices) ->
                Log.d(TAG,"onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                        + "selected: " + audioDevice));
    }

    public DefaultConfig getDefaultConfig() { return defaultConfig; }   // WebRTC 파라미터의 기본 설정을 불러온다.

    private void callConnected() {
        if (peerConnectionClient == null) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, 1000);
    }

    @Override
    public void onSignalConnected(SignalingParameters params) {
        RxScheduler.runOnUi(o -> {
            signalingParameters = params;
            // 1. 비디오 캡쳐를 카메라에서 가져온다.
            VideoCapturer videoCapturer = null;
            if(peerConnectionParameters.videoCallEnabled){
                videoCapturer = createVideoCapturer();  // 비디오 캡쳐러 초기화
            }
            // 2. Peer 연결을 만든다. 피어에 내 비디오 데이터를 넘긴다.
            peerConnectionClient.createPeerConnection(getEglBaseContext(), getLocalProxyRenderer(), new ArrayList<>(), videoCapturer, signalingParameters);

            // 매개변수로 전달받은 파라미터에 initiator 가 존재한다면
            if(signalingParameters.initiator){
                // 시간 내에 클라이언트에 응답하기 위해 SDP 를 보낸다.
                logAndToast("Creating OFFER");
                peerConnectionClient.createOffer(); // 응답을 생성한다.
            } else {
                if(params.offerSdp != null){    // 매개변수로 전달받은 시그널 파라미터에 offerSdp 를 전달 받았다면
                    peerConnectionClient.setRemoteDescription(params.offerSdp); // 원격지 SDP 에 offerSDP 정보를 넣는다.
                    logAndToast("Creating ANSWER");
                    peerConnectionClient.createAnswer();    // sdp 응답을 생성한다.
                }
                if(params.iceCandidates != null){   // 매개변수로 전달받은 시그널 파라미터에 IceCandidates 가 존재한다면
                    // 방에서 원격 ICE 참가자를 추가한다
                    for (IceCandidate iceCandidate : params.iceCandidates){
                        peerConnectionClient.addRemoteIceCandidate(iceCandidate);   // 원격 ice 후보지를 추가한다.
                    }
                }
            }
        });
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            peerConnectionClient.setRemoteDescription(sdp);
            if (!signalingParameters.initiator) {
                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates);
        });
    }

    @Override
    public void onChannelClose() {
        RxScheduler.runOnUi(o -> {
            logAndToast("Remote end hung up; dropping PeerConnection");
            disconnect();
        });
    }

    @Override
    public void onChannelError(String description) {
        Log.e(TAG, "onChannelError: " + description);
    }

    @Override
    public void onLocalDescription(SessionDescription sessionDescription) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                if (signalingParameters.initiator) {
                    rtcClient.sendOfferSdp(sessionDescription);
                } else {
                    rtcClient.sendAnswerSdp(sessionDescription);
                }
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidate(iceCandidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidateRemovals(iceCandidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        RxScheduler.runOnUi(o -> {
            iceConnected = true;
            callConnected();
        });
    }

    @Override
    public void onIceDisconnected() {
        RxScheduler.runOnUi(o -> {logAndToast("ICE disconnected");
            iceConnected = false;
            disconnect();
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] statsReports) {
        RxScheduler.runOnUi(o -> {
            if (iceConnected) {
                Log.e(TAG, "run: " + statsReports);
            }
        });
    }

    // 뒤로 가기
    @Override
    public void onBackPressed() {
        // 채팅입력중 사용자가 뒤로가기 눌렀을때
        if(message_layout.getVisibility() == View.VISIBLE){
            message_layout.setVisibility(View.GONE);
            button_layout.setVisibility(View.VISIBLE);
        } else {
            showDisconnectDialog(); // 다이얼로그를 띄운다.
        }
    }

    public void onPeerConnectionError(String s) {
        Log.e(TAG, "onPeerConnectionError: " + s);
    }

    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    public VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    public VideoRenderer.Callbacks getLocalProxyRenderer() {
        return localProxyRenderer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getDefaultConfig().isUseCamera2();
    }

    private boolean captureToTexture() {
        return getDefaultConfig().isCaptureToTexture();
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }
}
