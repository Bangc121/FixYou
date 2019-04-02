package com.example.kimjeonghwan.fixyou.vod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by KimJeongHwan on 2019-03-07.
 */

public class VodListAdapter extends RecyclerView.Adapter<VodListAdapter.ViewHolder> {
    private Intent intent;
    private ArrayList<VodItem> vodItems;
    private Context mContext;

    public VodListAdapter(ArrayList<VodItem> vodItems, Context mContext) {
        this.vodItems = vodItems;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_vod_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "ObsoleteSdkInt"})
    @Override
    public void onBindViewHolder(@NonNull VodListAdapter.ViewHolder holder, int position) {
        holder.vodName.setText(vodItems.get(position).getVod_name());
        holder.vodCreator.setText(vodItems.get(position).getVod_creator());
        holder.vodViewer.setText(vodItems.get(position).getVod_viewer());
        Glide.with(mContext)
                .load(vodItems.get(position).getVod_thumbnail())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(holder.vodThumbnail);
        try {
            String time = getTime(vodItems.get(position).getVod_url());
            long timeInmillisec = Long.parseLong( time );
            long duration = timeInmillisec / 1000;
            long hours = duration / 3600;
            long minutes = (duration - hours * 3600) / 60;
            long seconds = duration - (hours * 3600 + minutes * 60);
            String tMinutes = String.valueOf(minutes);
            String tSeconds = String.valueOf(seconds);

            // 시간 추출값이 10보다 작을때 숫자앞에 0을 붙여준다.
            if(minutes < 10){
                tMinutes = "0" + tMinutes;
            }
            if(seconds < 10){
                tSeconds = "0" + tSeconds;
            }
            Log.e("time", tMinutes+", "+tSeconds);
            holder.vodTime.setText(tMinutes+":"+tSeconds);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // vod 리스트 클릭 이벤트
        holder.vodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(mContext, VodActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("vodItems", vodItems);
                vodViewer(vodItems.get(position).getVod_name());
            }
        });
    }

    @Override
    public int getItemCount() {
        return vodItems.size();
    }

    //커스텀 뷰홀더를 만들어 BroadCastItem에 존재하는 항목들을 하인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout vodLayout;
        private ImageView vodThumbnail;
        private TextView vodName;
        private TextView vodCreator;
        private TextView vodViewer;
        private TextView vodTime;

        ViewHolder(View itemView) {
            super(itemView);
            vodLayout = itemView.findViewById(R.id.vodLayout);
            vodThumbnail = itemView.findViewById(R.id.vodThumbnail);
            vodName = itemView.findViewById(R.id.vodName);
            vodCreator = itemView.findViewById(R.id.vodCreator);
            vodViewer = itemView.findViewById(R.id.vodViewer);
            vodTime = itemView.findViewById(R.id.vodTime);
        }
    }

    // Url 에서 재생시간을 추출하는 메소드
    @SuppressLint("ObsoleteSdkInt")
    private static String getTime(String videoPath) throws Throwable
    {
        MediaMetadataRetriever mediaMetadataRetriever = null;
        String time = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return time;
    }

    private void vodViewer(String vodName){
        Call<ResponseBody> call = RetrofitClient.getInstance().getService().vodViewer(vodName);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.e("res", res);
                    // success값을 받아오면 회원가입 성공
                    if(Objects.equals(res, "success")){
                        mContext.startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(mContext, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
