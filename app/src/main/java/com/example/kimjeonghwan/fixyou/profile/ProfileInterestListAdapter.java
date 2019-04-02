package com.example.kimjeonghwan.fixyou.profile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.kimjeonghwan.fixyou.R;

import java.util.ArrayList;


/**
 * Created by KimJeongHwan on 2018-12-11.
 */

public class ProfileInterestListAdapter extends RecyclerView.Adapter<ProfileInterestListAdapter.ViewHolder> {
    private ArrayList<InterestItem> interestItems;
    private Context mContext;

    public ProfileInterestListAdapter(ArrayList<InterestItem> interestItems, Context mContext){
        this.interestItems = interestItems;
        this.mContext = mContext;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_my_interest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("InterestListAdapter", "onBindViewHolder position(" + position + ")");

        Log.d("InterestListAdapter", ""+interestItems.size());
        holder.myInterest.setText(interestItems.get(position).getInterest());
    }

    @Override
    public int getItemCount() {
        return interestItems.size();
    }

    //커스텀 뷰홀더를 만들어 InterestItem에 존재하는 항목들을 바인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView myInterest;

        ViewHolder(View itemView) {
            super(itemView);
            myInterest = (TextView)itemView.findViewById(R.id.myInterest);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }
}
