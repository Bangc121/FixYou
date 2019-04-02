package com.example.kimjeonghwan.fixyou.meetup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;

import java.util.ArrayList;

/**
 * Created by KimJeongHwan on 2019-03-13.
 */

public class MeetupMemberAdapter extends RecyclerView.Adapter<MeetupMemberAdapter.ViewHolder> {
    private ArrayList<MeetupMemberItem> meetupMemberItems;
    private Context mContext;

    public MeetupMemberAdapter(ArrayList<MeetupMemberItem> meetupMemberItems, Context mContext){
        this.meetupMemberItems = meetupMemberItems;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_meetup_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext)
                .load(meetupMemberItems.get(position).getProfile())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.meetupMember);
    }

    @Override
    public int getItemCount() {
        return meetupMemberItems.size();
    }

    //커스텀 뷰홀더를 만들어 MeetupMemberItem 존재하는 항목들을 바인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView meetupMember;

        ViewHolder(View itemView) {
            super(itemView);
            meetupMember = itemView.findViewById(R.id.meetupMember);
        }
    }
}
