package com.example.kimjeonghwan.fixyou.live.kurento.models.response;

/**
 * Created by nhancao on 6/19/17.
 */

public enum IdResponse {

    REGISTER_RESPONSE("registerResponse"),
    PRESENTER_RESPONSE("presenterResponse"),
    ICE_CANDIDATE("iceCandidate"),
    VIEWER_RESPONSE("viewerResponse"),
    STOP_COMMUNICATION("stopCommunication"),
    CLOSE_ROOM_RESPONSE("closeRoomResponse"),
    INCOMING_CALL("incomingCall"),
    START_COMMUNICATION("startCommunication"),
    CALL_RESPONSE("callResponse"),
    CHAT_RESPONSE("chatResponse"),                               // 채팅을 전달받았음을 알려주는 응답
    BALLOON_RESPONSE("balloonResponse"),                               // 별풍선을 전달받았음을 알려주는 응답
    CREATE_RESPONSE("createResponse"),                   // 방일 생성됬을때 알려주는 응답

    UN_KNOWN("unknown");

    private String id;

    IdResponse(String id) {
        this.id = id;
    }

    public static IdResponse getIdRes(String idRes) {
        for (IdResponse idResponse : IdResponse.values()) {
            if (idRes.equals(idResponse.getId())) {
                return idResponse;
            }
        }
        return UN_KNOWN;
    }

    public String getId() {
        return id;
    }
}
