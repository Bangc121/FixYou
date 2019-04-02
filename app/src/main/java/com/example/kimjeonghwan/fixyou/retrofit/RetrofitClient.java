package com.example.kimjeonghwan.fixyou.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by KimJeongHwan on 2018-11-15.
 */

public class RetrofitClient {
    private static RetrofitClient ourInstance = new RetrofitClient();
    public static RetrofitClient getInstance() {
        return ourInstance;
    }
    private RetrofitClient() {
    }

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://52.79.228.68/")
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .build();

    private RetrofitService service = retrofit.create(RetrofitService.class);

    public RetrofitService getService() {
        return service;
    }
}
