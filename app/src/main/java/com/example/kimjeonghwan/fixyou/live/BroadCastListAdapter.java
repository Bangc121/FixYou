package com.example.kimjeonghwan.fixyou.live;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by KimJeongHwan on 2018-11-16.
 */

public class BroadCastListAdapter extends RecyclerView.Adapter<BroadCastListAdapter.ViewHolder>{

    private ArrayList<BroadCastItem> broadCastItems;
    private Context mContext;

    public BroadCastListAdapter(ArrayList<BroadCastItem> broadCastItems, Context mContext) {
        this.broadCastItems = broadCastItems;
        this.mContext = mContext;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_broadcast_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("BroadCastListAdapter", "onBindViewHolder position(" + position + ")");
        holder.broadcastName.setText(broadCastItems.get(position).getBroadcast_name());
        holder.broadcastCreator.setText(broadCastItems.get(position).getBroadcast_creator());
        holder.broadcastViewer.setText(broadCastItems.get(position).getBroadcast_viewer());
        Log.e("jsonObject",broadCastItems.get(position).getBroadcast_thumbnail());

        Glide.with(mContext)
                .load(broadCastItems.get(position).getBroadcast_thumbnail())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(holder.broadcastThumbnail);
        //Picasso.get().load(broadCastItems.get(position).getBroadcast_thumbnail()).into(holder.broadcastThumbnail);
        int seesionid = Integer.parseInt(broadCastItems.get(position).getBroadcast_sessionid());
        holder.setClickListener((view, position1, isLongClick) -> {
            Intent intent = new Intent(mContext, ViewerActivity.class);
            intent.putExtra("SessionId", seesionid);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return broadCastItems.size();
    }

    //커스텀 뷰홀더를 만들어 BroadCastItem에 존재하는 항목들을 하인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ItemClickListener clickListener;
        private ImageView broadcastThumbnail;
        private TextView broadcastName;
        private TextView broadcastCreator;
        private TextView broadcastViewer;

        ViewHolder(View itemView) {
            super(itemView);
            broadcastThumbnail = itemView.findViewById(R.id.broadcastThumbnail);
            broadcastName = itemView.findViewById(R.id.broadcastName);
            broadcastCreator = itemView.findViewById(R.id.broadcastCreator);
            broadcastViewer = itemView.findViewById(R.id.broadcastViewer);
            itemView.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition(), false);
        }
    }

}
