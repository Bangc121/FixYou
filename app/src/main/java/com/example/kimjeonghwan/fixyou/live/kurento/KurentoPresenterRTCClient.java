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
 * Created by KimJeongHwan on 2018-11-16.
 */

public class KurentoPresenterRTCClient implements RTCClient {
    private static final String TAG = KurentoPresenterRTCClient.class.getSimpleName();

    private SocketService socketService;

    public KurentoPresenterRTCClient(SocketService socketService) {
        this.socketService = socketService;
    }

    public void connectToRoom(String host, BaseSocketCallback socketCallback) {
        socketService.connect(host, socketCallback);
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

    public void createRoom(String broadcastName, String broadcastCreator) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "createRoom");  // 방을 생성할 때 식별할 수 있는 id
            obj.put("name", broadcastName);  // 방이름
            obj.put("creator", broadcastCreator);  // 방생성자

            socketService.sendMessage(obj.toString());  // websocket을 통해 서버로 전송된다.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deleteRoom(String broadcastName, String broadcastCreator) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "deleteRoom");  // 방을 삭제할 때 식별할 수 있는 id
            obj.put("name", broadcastName);  // 방이름
            obj.put("creator", broadcastCreator);  // 방생성자

            socketService.sendMessage(obj.toString());  // websocket을 통해 서버로 전송된다.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendOfferSdp(SessionDescription sdp) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "presenter");  //방송 송출자
            obj.put("sdpOffer", sdp.description);   //방송 송출자의 세션 정보를 Jsonobject 객체에 저장

            socketService.sendMessage(obj.toString());  // websocket을 통해 서버로 전송된다.
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

            socketService.sendMessage(obj.toString());  // websocket을 통해 서버로 전송된다.
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] candidates) {
        Log.e(TAG, "sendLocalIceCandidateRemovals: ");
    }

}