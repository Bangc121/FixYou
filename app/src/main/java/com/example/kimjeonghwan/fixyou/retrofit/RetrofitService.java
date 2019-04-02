package com.example.kimjeonghwan.fixyou.retrofit;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by KimJeongHwan on 2018-11-15.
 */

public interface RetrofitService {
    @Multipart
    @POST("SignUp.php")
    Call<ResponseBody> SignUp(@Part MultipartBody.Part image, @Part("email") String email, @Part("password") String password, @Part("name") String name, @Part("birth") String birth, @Part("gender") String gender, @Part("phone") String phone, @Part("nation") String nation, @Part("interest") String interest);

    @FormUrlEncoded
    @POST("SetLocation.php")
    Call<ResponseBody> setLocation(@Field("email") String email, @Field("latitude") String latitude, @Field("longitude") String longitude);

    @FormUrlEncoded
    @POST("LocationInfo.php")
    Call<ResponseBody> locationInfo(@Field("email") String email, @Field("latitude") double latitude, @Field("longitude") double longitude);

    @FormUrlEncoded
    @POST("ProfileInfo.php")
    Call<ResponseBody> profileCall(@Field("email") String email);

    @FormUrlEncoded
    @POST("ProfileUpdate.php")
    Call<ResponseBody> profileUpdate(@Field("email") String email, @Field("keystore") String keystore);

    @FormUrlEncoded
    @POST("FreindList.php")
    Call<ResponseBody> friendCall(@Field("email") String email);

    @FormUrlEncoded
    @POST("BroadCastList.php")
    Call<ResponseBody> BroadCastList(@Field("id") String id);

    @FormUrlEncoded
    @POST("VodList.php")
    Call<ResponseBody> VodList(@Field("id") String id);

    @FormUrlEncoded
    @POST("RecentVodList.php")
    Call<ResponseBody> recentVodList(@Field("email") String email);

    @FormUrlEncoded
    @POST("MeetupList.php")
    Call<ResponseBody> MeetupList(@Field("email") String email);

    @FormUrlEncoded
    @POST("MyMeetupList.php")
    Call<ResponseBody> myMeetupList(@Field("email") String email);

    @FormUrlEncoded
    @POST("ChatList.php")
    Call<ResponseBody> chatList(@Field("email") String email);

    @FormUrlEncoded
    @POST("ChatMessage.php")
    Call<ResponseBody> chatMessage(@Field("roomId") String roomId);

    @FormUrlEncoded
    @POST("MeetupJoin.php")
    Call<ResponseBody> meetupJoin(@Field("id") String id, @Field("email") String email);

    @FormUrlEncoded
    @POST("MeetupInfo.php")
    Call<ResponseBody> meetupInfo(@Field("id") String id);

    @FormUrlEncoded
    @POST("RecommendFriend.php")
    Call<ResponseBody> recommendFriend(@Field("email") String email);

    @FormUrlEncoded
    @POST("VodViewer.php")
    Call<ResponseBody> vodViewer(@Field("vodName") String vodName);

    @FormUrlEncoded
    @POST("VodPosition.php")
    Call<ResponseBody> vodPosition(@Field("vodId") String vodId, @Field("vodPosition") String vodPosition, @Field("user_email") String user_email, @Field("user_name") String user_name);

    @FormUrlEncoded
    @POST("PurchaseBalloon.php")
    Call<ResponseBody> purchaseBalloon(@Field("email") String email, @Field("balloon") String balloon);

    @Multipart
    @POST("MeetupCreate.php")
    Call<ResponseBody> MeetupCreate(@Part MultipartBody.Part image, @Part("meetup_name") String name, @Part("meetup_explanation") String explanation, @Part("user_nickname") String userEmail);
}
