package com.example.kimjeonghwan.fixyou.meetup;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
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
import com.example.kimjeonghwan.fixyou.R;

import java.util.ArrayList;

/**
 * Created by KimJeongHwan on 2018-11-29.
 */

public class MeetupListAdapter extends RecyclerView.Adapter<MeetupListAdapter.ViewHolder> {
    private ArrayList<MeetupListItem> meetupListItems;
    private Context mContext;

    public MeetupListAdapter(ArrayList<MeetupListItem> meetupListItems, Context mContext){
        this.meetupListItems = meetupListItems;
        this.mContext = mContext;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_meetup_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("MeetupListAdapter", "onBindViewHolder position(" + position + ")");
        holder.meetup_title.setText(meetupListItems.get(position).getMeetup_title());
        holder.meetup_content.setText(meetupListItems.get(position).getMeetup_content());
        Glide.with(mContext)
                .load(meetupListItems.get(position).getMeetup_picture())
                .into(holder.meetup_picture);
        holder.meetup_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMeetup = new Intent(mContext, MeetupActivity.class);
                intentMeetup.putExtra("id", meetupListItems.get(position).getMeetup_id());
                intentMeetup.putExtra("picture", meetupListItems.get(position).getMeetup_picture());
                intentMeetup.putExtra("title", meetupListItems.get(position).getMeetup_title());
                intentMeetup.putExtra("creater", meetupListItems.get(position).getMeetup_creater());
                mContext.startActivity(intentMeetup);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meetupListItems.size();
    }

    //커스텀 뷰홀더를 만들어 ChatMessageItem에 존재하는 항목들을 바인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView meetup_title;
        private TextView meetup_content;
        private ImageView meetup_picture;
        private LinearLayout meetup_layout;

        ViewHolder(View itemView) {
            super(itemView);
            meetup_title = itemView.findViewById(R.id.meetup_title);
            meetup_content = itemView.findViewById(R.id.meetup_content);
            meetup_picture = itemView.findViewById(R.id.meetup_picture);
            meetup_layout = itemView.findViewById(R.id.meetup_layout);
        }
    }
}
