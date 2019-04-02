package com.example.kimjeonghwan.fixyou.vod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by KimJeongHwan on 2019-03-22.
 */

public class VodRecentListAdapter extends RecyclerView.Adapter<VodRecentListAdapter.ViewHolder> {
    private Intent intent;
    private ArrayList<VodItem> vodItems;
    private Context mContext;

    public VodRecentListAdapter(ArrayList<VodItem> vodItems, Context mContext) {
        this.vodItems = vodItems;
        this.mContext = mContext;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_vod_recent, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.recentVodName.setText(vodItems.get(position).getVod_name());
        holder.recentVodCreator.setText(vodItems.get(position).getVod_creator());
        Log.e("getVod_position", vodItems.get(position).getVod_position());
        Glide.with(mContext)
                .load(vodItems.get(position).getVod_thumbnail())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(holder.recentVodThumbnail);
        try {
            String time = getTime(vodItems.get(position).getVod_url());
            long timeInmillisec = Long.parseLong( time );
            holder.vodProgressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            holder.vodProgressBar.setMax((int) timeInmillisec);
            holder.vodProgressBar.setProgress(Integer.parseInt(vodItems.get(position).getVod_position()));
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
            holder.recentVodTime.setText(tMinutes+":"+tSeconds);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // vod 리스트 클릭 이벤트
        holder.recentVodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(mContext, VodActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("vodItems", vodItems);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vodItems.size();
    }

    //커스텀 뷰홀더를 만들어 BroadCastItem에 존재하는 항목들을 하인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout recentVodLayout;
        private ImageView recentVodThumbnail;
        private TextView recentVodName;
        private TextView recentVodCreator;
        private TextView recentVodTime;
        private ProgressBar vodProgressBar;

        ViewHolder(View itemView) {
            super(itemView);
            recentVodLayout = itemView.findViewById(R.id.recentVodLayout);
            recentVodThumbnail = itemView.findViewById(R.id.recentVodThumbnail);
            recentVodName = itemView.findViewById(R.id.recentVodName);
            recentVodCreator = itemView.findViewById(R.id.recentVodCreator);
            recentVodTime = itemView.findViewById(R.id.recentVodTime);
            vodProgressBar = itemView.findViewById(R.id.vodProgressBar);
        }
    }

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
}
