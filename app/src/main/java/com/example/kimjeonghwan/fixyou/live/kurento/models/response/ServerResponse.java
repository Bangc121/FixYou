package com.example.kimjeonghwan.fixyou.live.kurento.models.response;

import com.example.kimjeonghwan.fixyou.live.kurento.models.CandidateModel;
import com.example.kimjeonghwan.fixyou.live.kurento.models.IdModel;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by nhancao on 6/19/17.
 */

public class ServerResponse extends IdModel implements Serializable {
    @SerializedName("response")
    private String response;
    @SerializedName("sdpAnswer")
    private String sdpAnswer;
    @SerializedName("candidate")
    private CandidateModel candidate;
    @SerializedName("message")
    private String message;
    @SerializedName("success")
    private boolean success;
    @SerializedName("from")
    private String from;
    @SerializedName("balloon")
    private String balloon;

    public IdResponse getIdRes() {
        return IdResponse.getIdRes(getId());
    }

    public TypeResponse getTypeRes() {
        return TypeResponse.getType(getResponse());
    }

    public String getResponse() {
        return response;
    }

    public String getSdpAnswer() {
        return sdpAnswer;
    }

    public CandidateModel getCandidate() {
        return candidate;
    }

    public String getMessage() {
        return message;
    }

    public String getBalloon() {
        return balloon;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
