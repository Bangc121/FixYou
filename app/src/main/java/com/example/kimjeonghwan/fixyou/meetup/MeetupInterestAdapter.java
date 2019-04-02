package com.example.kimjeonghwan.fixyou.meetup;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.profile.InterestItem;
import com.example.kimjeonghwan.fixyou.profile.ProfileInterestListAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KimJeongHwan on 2019-01-31.
 */

public class MeetupInterestAdapter extends RecyclerView.Adapter<MeetupInterestAdapter.ViewHolder> {

    private ArrayList<MeetupInterestItem> meetupInterestItems;
    private Context mContext;
    private Intent intent;
    private List<String> arrayList;

    // 디테일 관심사
    private ArrayList<InterestItem> interestItems;

    public MeetupInterestAdapter(ArrayList<MeetupInterestItem> meetupInterestItems, Context mContext, List<String> arrayList){
        this.intent = intent;
        this.meetupInterestItems = meetupInterestItems;
        this.mContext = mContext;
        this.arrayList = arrayList;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_meetup_interest, parent, false);
        interestList();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("MeetupInterestAdapter", "onBindViewHolder position(" + position + ")");
        holder.interest_theme.setText(meetupInterestItems.get(position).getInterest_text());

        // 리사이클러뷰 초기화
        holder.recyclerView.setAdapter(new ProfileInterestListAdapter(interestItems, mContext));
        //불규칙 레이아
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
        // Set flex direction.
        layoutManager.setFlexDirection(FlexDirection.ROW);
        // Set JustifyContent.
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        holder.recyclerView.setLayoutManager(layoutManager);
        holder.recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public int getItemCount() {
        return meetupInterestItems.size();
    }

    //커스텀 뷰홀더를 만들어 SignUPInterestItem에 존재하는 항목들을 바인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView interest_theme;
        private RecyclerView recyclerView;

        ViewHolder(View itemView) {
            super(itemView);
            interest_theme = itemView.findViewById(R.id.interest_theme);
            recyclerView = itemView.findViewById(R.id.detail_interest_recyclerview);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

    /**
     * 관심사 디테일 가져오기
     */
    public void interestList() {
        interestItems = new ArrayList<>();
        //룸아이템
        InterestItem interestItem = new InterestItem();
        interestItem.setInterest("해외취업");
        interestItem.setInterest("블록체인");
        interestItems.add(interestItem);
    }

}
