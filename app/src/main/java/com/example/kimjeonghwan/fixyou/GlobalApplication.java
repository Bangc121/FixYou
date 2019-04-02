package com.example.kimjeonghwan.fixyou;

import android.app.Activity;
import android.support.multidex.MultiDexApplication;

import com.kakao.auth.KakaoSDK;

/**
 * Created by KimJeongHwan on 2018-11-26.
 */

public class GlobalApplication extends MultiDexApplication {

    private static volatile GlobalApplication obj = null;
    private static volatile Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        obj = this;
        KakaoSDK.init(new KaKaoSDKAdapter());
    }

    public static GlobalApplication getGlobalApplicationContext() {
        return obj;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    // Activity가 올라올때마다 Activity의 onCreate에서 호출해줘야한다.
    public static void setCurrentActivity(Activity currentActivity) {
        GlobalApplication.currentActivity = currentActivity;
    }
}

