package com.example.kimjeonghwan.fixyou.friend;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.live.ItemClickListener;
import com.example.kimjeonghwan.fixyou.profile.ProfileActivity;

import java.util.ArrayList;

/**
 * Created by KimJeongHwan on 2018-12-10.
 */

public class FriendRecommandAdpater extends RecyclerView.Adapter<FriendRecommandAdpater.ViewHolder>{
    private ArrayList<FriendRecommandItem> friendRecommandItems;
    private Context mContext;

    public FriendRecommandAdpater(ArrayList<FriendRecommandItem> broadCastItems, Context mContext) {
        this.friendRecommandItems = broadCastItems;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_friend_recommand, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRecommandAdpater.ViewHolder holder, int position) {
        Log.d("FriendRecommandAdpater", "onBindViewHolder position(" + position + ")");
        holder.friend_name.setText(friendRecommandItems.get(position).getFriend_name());
        holder.friend_language.setText(friendRecommandItems.get(position).getFriend_nation());
        holder.friend_location.setText(friendRecommandItems.get(position).getFriend_location());
        Glide.with(mContext)
                .load(friendRecommandItems.get(position).getFriend_profile())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.friend_profile);
        Glide.with(mContext)
                .load("http://52.79.228.68/nation/"+friendRecommandItems.get(position).getFriend_nation()+".png")
                .into(holder.friend_nation);

        holder.friend_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentProfile = new Intent(mContext, ProfileActivity.class);
                intentProfile.putExtra("email", friendRecommandItems.get(position).getFriend_email());
                mContext.startActivity(intentProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendRecommandItems.size();
    }

    //커스텀 뷰홀더를 만들어 BroadCastItem에 존재하는 항목들을 하인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout friend_layout;
        private ImageView friend_profile;
        private ImageView friend_nation;
        private TextView friend_name;
        private TextView friend_location;
        private TextView friend_language;
        ViewHolder(View itemView) {
            super(itemView);
            friend_layout = itemView.findViewById(R.id.friend_layout);
            friend_profile = itemView.findViewById(R.id.friend_profile);
            friend_nation = itemView.findViewById(R.id.friend_nation);
            friend_name = itemView.findViewById(R.id.friend_name);
            friend_location = itemView.findViewById(R.id.friend_location);
            friend_language = itemView.findViewById(R.id.friend_language);
        }
    }
}
