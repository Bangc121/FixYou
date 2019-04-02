package com.example.kimjeonghwan.fixyou.live.kurento;

import android.util.Log;

import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.RTCClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * Created by nhancao on 7/18/17.
 */

public class KurentoViewerRTCClient implements RTCClient {
    private int presenterSessionId;     // 서버에 Node.js 상 방송 송출자가 가지고 있는 SessionId 값을 저장한다.
    private static final String TAG = KurentoViewerRTCClient.class.getSimpleName();

    private SocketService socketService;

    public KurentoViewerRTCClient(SocketService socketService) {
        this.socketService = socketService;
    }

    public void connectToRoom(String host, BaseSocketCallback socketCallback) {
        socketService.connect(host, socketCallback);
    }

    // 라이브 방송에 연결하기 전, 시청하고자 하는 방의 서버상 세션 아이디를 설정한다.
    public void setPresenterSID(int presenterSessionId){
        this.presenterSessionId = presenterSessionId;   // 세션 아이디 설정
    }

    public void sendMessage(String message, String email) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "chat");  // 채팅을 할 때 식별할 수 있는 id
            obj.put("email", email);  // 이메일
            obj.put("message", message);  // 메시지

            socketService.sendMessage(obj.toString());  // websocket을 통해 서버로 전송된다.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendBalloon(String balloon, String email) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "balloon");  // 채팅을 할 때 식별할 수 있는 id
            obj.put("email", email);  // 이메일
            obj.put("balloon", balloon);  // 별풍선

            socketService.sendMessage(obj.toString());  // websocket을 통해 서버로 전송된다.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendOfferSdp(SessionDescription sdp) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "viewer");
            obj.put("sdpOffer", sdp.description);

            socketService.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAnswerSdp(SessionDescription sdp) {
        Log.e(TAG, "sendAnswerSdp: ");
    }

    @Override
    public void sendLocalIceCandidate(IceCandidate iceCandidate) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "onIceCandidate");
            JSONObject candidate = new JSONObject();
            candidate.put("candidate", iceCandidate.sdp);
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            obj.put("candidate", candidate);

            socketService.sendMessage(obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] candidates) {
        Log.e(TAG, "sendLocalIceCandidateRemovals: ");
    }

}
