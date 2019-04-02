package com.example.kimjeonghwan.fixyou.friend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.chat.ChatActivity;
import com.example.kimjeonghwan.fixyou.profile.ProfileActivity;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendLocationActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener {

    MapView mapView;
    ViewGroup mapViewContainer;

    private final int MENU_DEFAULT_CALLOUT_BALLOON = Menu.FIRST;
    private final int MENU_CUSTOM_CALLOUT_BALLOON = Menu.FIRST + 1;

    private MapPOIItem mCustomMarker;

    BottomSheetDialog bottomSheetDialog; // 밑에서 올라오는 다이얼로그
    ImageView select_profile;
    ImageView select_search;
    ImageView select_chat;
    ImageView select_video;

    FrameLayout refreshLocation;

    // CalloutBalloonAdapter 인터페이스 구현
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            HashMap<String, String> userObject = (HashMap<String, String>)poiItem.getUserObject();
            String desc = userObject.get("desc");
            String profile = userObject.get("profile");
            ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageBitmap(getBitmapFromURL(profile));
            ((TextView) mCalloutBalloon.findViewById(R.id.title1)).setText(poiItem.getItemName());
            ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText(desc);
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_location);

        refreshLocation = findViewById(R.id.refreshLocation);

        mapView = new MapView(this);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);

        mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        //구현한 CalloutBalloonAdapter 등록
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());

    }

    public void setLocation(double d1, double d2) {
        String email = getApplication().getSharedPreferences("login_info", Context.MODE_PRIVATE).getString("email","none");  // SharedPreference 에서 닉네임 정보를 가져온다.
        String latitude = String.valueOf(d1);
        String longitude = String.valueOf(d2);

        Call<ResponseBody> call = RetrofitClient.getInstance().getService().setLocation(email, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String res = response.body().string();

                    Log.e("getLocation", res);

                    JSONObject json_location = new JSONObject(res);
                    String location_info = json_location.getString("location");
                    JSONArray json_info = new JSONArray(location_info);

                    for(int i = 0; i < json_info.length(); i++){
                        JSONObject jsonObject = json_info.getJSONObject(i);
                        //상대방 위치 정보를 받아옴
                        double latitude = Double.parseDouble(jsonObject.getString("latitude"));
                        double longitude = Double.parseDouble(jsonObject.getString("longitude"));
                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                        mCustomMarker = new MapPOIItem();
                        mCustomMarker.setItemName(jsonObject.getString("name"));
                        mCustomMarker.setTag(i);
                        HashMap<String, String> userObject = new HashMap<>();
                        userObject.put("profile", jsonObject.getString("profile"));
                        userObject.put("desc", jsonObject.getString("email"));
                        userObject.put("name", jsonObject.getString("name"));
                        mCustomMarker.setUserObject(userObject);
                        mCustomMarker.setMapPoint(mapPoint);

                        mCustomMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

                        mCustomMarker.setCustomImageResourceId(R.drawable.marker_current_pink);
                        mCustomMarker.setCustomImageAutoscale(false);
                        mCustomMarker.setCustomImageAnchor(0.5f, 1.0f);     // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

                        mapView.addPOIItem(mCustomMarker);
                        //createCustomMarker(mMapView, mapPoint);
                    }
                    mapViewContainer.setVisibility(View.VISIBLE);
                    refreshLocation.setVisibility(View.GONE);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_DEFAULT_CALLOUT_BALLOON, Menu.NONE, "Default CalloutBalloon");
        menu.add(0, MENU_CUSTOM_CALLOUT_BALLOON, Menu.NONE, "Custom CalloutBalloon");

        return true;
    }

    // URL이미지를 Bitmap이미지로 변환
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        Log.e("onMapViewInitialized", "onMapViewInitialized");
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        mapView.setCurrentLocationRadius(500); // meter
        mapView.setCurrentLocationRadiusStrokeColor(Color.argb(77, 255, 165, 0));
        mapView.setCurrentLocationRadiusFillColor(Color.argb(77, 255, 255, 0));
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        Log.e("ViewCenterPointMoved", "onCurrentLocationUpdate");
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        Log.e("LevelChanged", "onMapViewZoomLevelChanged");
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        Log.e("onMapViewSingleTapped", "onMapViewSingleTapped");
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        Log.e("onMapViewDoubleTapped", "onMapViewDoubleTapped");
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        Log.e("onMapViewLongPressed", "onMapViewLongPressed");
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        Log.e("onMapViewDragStarted", "onMapViewDragStarted");
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        Log.e("onMapViewDragEnded", "onMapViewDragEnded");
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        Log.e("onMapViewMoveFinished", "onMapViewMoveFinished");
        double longitude = mapPoint.getMapPointGeoCoord().longitude;  // 트래킹모드 경도
        double latitude = mapPoint.getMapPointGeoCoord().latitude;  // 트래킹모드 위도

        Log.e("latitude", String.valueOf(latitude));
        setLocation(latitude, longitude);  //DB로부터 주변 사람들의 위치를 받아온다.
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        Log.e("onPOIItemSelected", "onPOIItemSelected");
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        // 밑에서 올라오는 다이어로그 적용
        bottomSheetDialog = new BottomSheetDialog(FriendLocationActivity.this);

        View view = getLayoutInflater().inflate(R.layout.dialog_bottom_map, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        LinearLayout dialog_profile = view.findViewById(R.id.dialog_profile);
        LinearLayout dialog_search = view.findViewById(R.id.dialog_search);
        LinearLayout dialog_chat = view.findViewById(R.id.dialog_chat);
        LinearLayout dialog_video = view.findViewById(R.id.dialog_video);

        select_profile = view.findViewById(R.id.select_profile);
        select_search = view.findViewById(R.id.select_search);
        select_chat = view.findViewById(R.id.select_chat);
        select_video = view.findViewById(R.id.select_video);

        // 상대방 프로필정보로 이동
        dialog_profile.setOnClickListener(v -> {
            Intent intentProfile = new Intent(FriendLocationActivity.this, ProfileActivity.class);
            HashMap<String, String> userObject = (HashMap<String, String>)mapPOIItem.getUserObject();
            String desc = userObject.get("desc");
            intentProfile.putExtra("email", desc);
            startActivity(intentProfile);
            bottomSheetDialog.dismiss();
        });
        // 상대방위치에 대한 경로 검색
        dialog_search.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
        // 상대방과의 채팅 시작
        dialog_chat.setOnClickListener(v -> {
            Intent intentChat = new Intent(FriendLocationActivity.this, ChatActivity.class);
            HashMap<String, String> userObject = (HashMap<String, String>)mapPOIItem.getUserObject();
            String email = userObject.get("desc");
            String name = userObject.get("name");
            intentChat.putExtra("email", email);
            intentChat.putExtra("name", name);
            startActivity(intentChat);
            bottomSheetDialog.dismiss();
        });
        // 상대방과 영상통화 시작
        dialog_video.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
        //Toast.makeText(this, "Clicked " + mapPOIItem.getItemName() + " Callout Balloon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}
