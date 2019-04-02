package com.example.kimjeonghwan.fixyou.live;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.kimjeonghwan.fixyou.CashPurchaseActivity;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.chat.ChatMessageItem;
import com.example.kimjeonghwan.fixyou.friend.FriendLocationActivity;
import com.example.kimjeonghwan.fixyou.live.kurento.KurentoViewerRTCClient;
import com.example.kimjeonghwan.fixyou.live.kurento.models.CandidateModel;
import com.example.kimjeonghwan.fixyou.live.kurento.models.response.ServerResponse;
import com.example.kimjeonghwan.fixyou.live.kurento.models.response.TypeResponse;
import com.example.kimjeonghwan.fixyou.utils.RxScheduler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.DefaultSocketService;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionClient;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionParameters;
import com.nhancv.webrtcpeer.rtc_peer.SignalingEvents;
import com.nhancv.webrtcpeer.rtc_peer.SignalingParameters;
import com.nhancv.webrtcpeer.rtc_peer.StreamMode;
import com.nhancv.webrtcpeer.rtc_peer.config.DefaultConfig;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;
import com.nhancv.webrtcpeer.rtc_plugins.RTCAudioManager;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.example.kimjeonghwan.fixyou.live.bases.Constants.MESSAGE_TYPE_BALLOON;
import static com.example.kimjeonghwan.fixyou.live.bases.Constants.MESSAGE_TYPE_SELF;

public class ViewerActivity extends AppCompatActivity implements View.OnClickListener, SignalingEvents, PeerConnectionClient.PeerConnectionEvents{
    private static final String TAG = BroadCasterActivity.class.getSimpleName(); // 로그 출력을 위한 태그 설정. 현재 클래스의 이름으로 저장함.
    private static final String STREAM_HOST = "wss://52.79.228.68:8443/one2many";

    private SocketService socketService;
    private Gson gson;

    private KurentoViewerRTCClient rtcClient;   // Kurento 에 시청자 연결하기 위한
    private PeerConnectionParameters peerConnectionParameters;
    private PeerConnectionClient peerConnectionClient;
    private SignalingParameters signalingParameters;
    private RTCAudioManager audioManager;

    private SurfaceViewRenderer vGLSurfaceViewCall;

    private ProxyRenderer remoteProxyRenderer;  // SurfaceView 에 장면을 렌더링하는 객체
    private EglBase rootEglBase;
    private Toast logToast;

    private int broadcastSessionId; // 방송의 세션 아이디

    private boolean iceConnected;

    RecyclerView recyclerView;
    ArrayList<ChatMessageItem> chatMessageItems;
    ChatMessageItem item;

    BottomSheetDialog bottomSheetDialog; // 밑에서 올라오는 다이얼로그
    LinearLayout gift_menu;
    EditText broadcast_editText_message;
    Button broadcast_button_send;
    String sendMessage;
    String user_email, user_balloon, userName;
    String user_message;
    JSONObject message_obj;
    SharedPreferences pref;

    TextView userBalloon;  // 보유 별풍선 텍스트뷰
    Button sendBalloon;  // 별풍선 보내기 버튼
    EditText editBalloon;  // 별풍선 입력 에디트텍스트

    LottieAnimationView animationView;  //별풍선 애니메이션 설정
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        animationView = findViewById(R.id.animation_view); // 로티 애니메이션 설정

        animationView.setAnimation(R.raw.balloons_with_string);

        // 밑에서 올라오는 다이어로그 적용
        bottomSheetDialog = new BottomSheetDialog(ViewerActivity.this);

        recyclerView = (RecyclerView)findViewById(R.id.broadcast_chat_recyclerview);
        chatMessageItems = new ArrayList<ChatMessageItem>();
        broadcast_button_send = (Button)findViewById(R.id.broadcast_button_send);
        broadcast_editText_message = (EditText)findViewById(R.id.broadcast_editText_message);
        gift_menu = findViewById(R.id.gift_menu);

        gift_menu.setOnClickListener(this);
        broadcast_button_send.setOnClickListener(this);

        getData();
        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.broadcast_button_send:
                sendMessage();
                break;
            case R.id.gift_menu:
                @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_bottom_gift, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                userBalloon = view.findViewById(R.id.userBalloon);
                userBalloon.setText(user_balloon);

                editBalloon = view.findViewById(R.id.editBalloon);

                sendBalloon = view.findViewById(R.id.sendBalloon);
                sendBalloon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendBalloon();  // 풍선을 서버에 보내 최종적으로 후원할수 있다.
                        showAnimation();  // 풍선 애니메이션을 보여준다.
                        receiveBalloon(user_email, editBalloon.getText().toString());  // 자신이 보낸 풍선는은 서버에서 받지 않고 클라이언트단에서 바로 리스트에 추가해준다.
                    }
                });
                break;
        }
    }

    // 내장 메모리에 저장된 데이터를 가져옴
    protected void getData() {
        pref = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE);
        userName = pref.getString("name", "none");
        user_email = pref.getString("email", "none");
        user_balloon = pref.getString("balloon", "none");
        Intent intent = getIntent();
        broadcastSessionId = intent.getIntExtra("SessionId", 0);
        Log.e("broadcastSessionId",broadcastSessionId+"");
    }


    protected void init() {
        socketService = new DefaultSocketService(getApplication());
        gson = new Gson();

        vGLSurfaceViewCall = findViewById(R.id.BroadCastViewerSurfaceView);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        //config peer
        remoteProxyRenderer = new ProxyRenderer();  // 프록시 렌더러 초기화
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        remoteProxyRenderer.setTarget(vGLSurfaceViewCall);

        initPeerConfig();
    }

    /*
     * PeerConfig 를 초기화한다.
     */
    public void initPeerConfig() {
        rtcClient = new KurentoViewerRTCClient(socketService);  // 소켓 서비스를 매개 변수로 방송 송출자용 RTC Client 객체를 생성한다.
        DefaultConfig defaultConfig = new DefaultConfig();   // 디폴트 설정을 초기화한다.
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.RECV_ONLY);  // PeerConnectionParameter 에 스트림 모드를 받기 전용으로 변경한다.
        peerConnectionClient = PeerConnectionClient.getInstance();  // PeerConnection 객체를 초기화한다.
        peerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, this);   // PeerConnection 을 생성한다.
        rtcClient.setPresenterSID(broadcastSessionId);
        startCall();    // Peer 연결을 시작한다.
    }

    // 채팅 메세지를 보내기 위한 메소드
    public void sendMessage() {
        RxScheduler.runOnUi(o -> {
            sendMessage = broadcast_editText_message.getText().toString( );  // 메시지 내용을 sendMessage에 저장
            rtcClient.sendMessage(sendMessage, userName);  // 메세지 내용과 유저이메일을 서버에 전송
            broadcast_editText_message.setText(null);  // 전송 후, EditText 초기화
            receiveChatMessage(userName, sendMessage);  // 자신이 보낸 메세지는 서버에서 받지 않고 클라이언트단에서 바로 리스트에 추가해준다.
        });
    }

    // 별풍선을 보내기 위한 메소드
    public void sendBalloon() {
        RxScheduler.runOnUi(o -> {
            Log.e("editBalloon.toString()", editBalloon.getText().toString());
            rtcClient.sendBalloon(editBalloon.getText().toString(), user_email);  // 메세지 내용과 유저이메일을 서버에 전송
            editBalloon.setText(null);  // 전송 후, EditText 초기화
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

    private void receiveBalloon(String email, String balloon) {
        RxScheduler.runOnUi(o -> {
            item = new ChatMessageItem();
            item.setName(email);
            item.setBalloon(balloon);
            item.setMessageType(MESSAGE_TYPE_BALLOON);
            chatMessageItems.add(item);
            initChatLayout();
        });
    }

    private void showDisconnectDialog(){
        RxScheduler.runOnUi(o -> {
            AlertDialog.Builder stopDialog = new AlertDialog.Builder(ViewerActivity.this,R.style.myDialog);
            stopDialog.setTitle("방송 종료")
                    .setMessage("방송을 종료하시겠습니까?")
                    .setPositiveButton("종료", (dialog, which) -> disconnect(true))   // 종료 버튼을 누르게되면 서버와 Peer 연결을 끊는 disconnect() 메소드를 호출한다.
                    .setNegativeButton("취소", (dialog, which) -> dialog.cancel())    // 취소 버튼을 누르게되면 다이얼로그를 취소한다.
                    .show();
        });
    }

    /*
     * WebSocket 연결 해제
     */
    public void disconnect(boolean isFinish) {
        runOnUiThread(()->{
            remoteProxyRenderer.setTarget(null);    // 렌더러 타겟 초기화
            if (vGLSurfaceViewCall != null) {
                vGLSurfaceViewCall.release(); // 피어 연결을 끊는다.
                vGLSurfaceViewCall = null; // 피어 연결 초기화
            }
            if (rtcClient != null) {
                rtcClient = null;   // rtcClient 초기화
            }
            if (peerConnectionClient != null) {
                peerConnectionClient.close();   // 서페이스 뷰에서 렌더러를 release 한다.
                peerConnectionClient = null;    // 서페이스 뷰를 초기화한다.
            }

            if (audioManager != null) {
                audioManager.stop();    // 오디오 매니저를 중지한다.
                audioManager = null;    // 오디오 매니저 초기화
            }

            if (socketService != null) {
                socketService.close();  // 소켓 연결을 종료한다.
            }
            if(isFinish){
                finish();   // 액티비티를 종료한다.
            }
        });
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
                onSignalConnected(parameters);
            }

            @Override
            public void onMessage(String serverResponse_) {
                super.onMessage(serverResponse_);
                try {
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);

                    switch (serverResponse.getIdRes()) {
                        case VIEWER_RESPONSE:
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
                    disconnect(true);
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                RxScheduler.runOnUi(o -> {
                    logAndToast(e.getMessage());
                    disconnect(true);
                });
            }

        });
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
            disconnect(true);
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

    @Override
    public void onPeerConnectionError(String s) {
        Log.e(TAG, "onPeerConnectionError: " + s);
    }

    @Override
    public void onSignalConnected(SignalingParameters params) {
        RxScheduler.runOnUi(o -> {
            signalingParameters = params;

            peerConnectionClient
                    .createPeerConnection(getEglBaseContext(), null,    // peerConnection 를 생성한다.
                            getRemoteProxyRenderer(), null, // PeerConnection 에서 받아온 데이터를 proxy 렌더러에 연결한다.
                            signalingParameters);
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
    public void onRemoteDescription(SessionDescription sessionDescription) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            peerConnectionClient.setRemoteDescription(sessionDescription);
            if (!signalingParameters.initiator) {
                logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate iceCandidate) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(iceCandidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(iceCandidates);
        });
    }

    @Override
    public void onChannelClose() {
        RxScheduler.runOnUi(o -> {
            logAndToast("Remote end hung up; dropping PeerConnection");
            disconnect(true);
        });
    }

    @Override
    public void onChannelError(String s) {
        Log.e(TAG, "onChannelError: " + s);
    }

    // 뒤로 가기
    @Override
    public void onBackPressed() {
        showDisconnectDialog(); // 다이얼로그를 띄운다.
    }

    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    private void callConnected() {
        if (peerConnectionClient == null) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, 1000);
    }

    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    public VideoRenderer.Callbacks getRemoteProxyRenderer() { return remoteProxyRenderer; } // 리모트 렌더러를 리턴한다.

}
