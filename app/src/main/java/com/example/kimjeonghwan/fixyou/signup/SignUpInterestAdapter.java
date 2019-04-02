package com.example.kimjeonghwan.fixyou.signup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CheckableImageButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.CheckableImageView;
import com.example.kimjeonghwan.fixyou.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KimJeongHwan on 2018-12-15.
 */

public class SignUpInterestAdapter extends RecyclerView.Adapter<SignUpInterestAdapter.ViewHolder> {
    private ArrayList<SignUpInterestItem> signUpInterestItems;
    private Context mContext;
    private Intent intent;
    private List<String> arrayList;

    public SignUpInterestAdapter(ArrayList<SignUpInterestItem> signUpInterestItems, Context mContext, Intent intent, List<String> arrayList){
        this.intent = intent;
        this.signUpInterestItems = signUpInterestItems;
        this.mContext = mContext;
        this.arrayList = arrayList;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_signup_first, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("SignUpInterestAdapter", "onBindViewHolder position(" + position + ")");
        Picasso.get().load(signUpInterestItems.get(position).getInterest_uri()).into(holder.signup_interest_image);
        holder.signup_interest_text.setText(signUpInterestItems.get(position).getInterest_text());

        // 이미지를 체크했을때 발생하는 이벤트
        holder.signup_interest_image.setOnClickListener(v -> {
            if(holder.signup_interest_image.isChecked()){
                holder.signup_interest_check.setVisibility(View.INVISIBLE);
                holder.signup_interest_image.setChecked(false);
            }else {
                holder.signup_interest_check.setVisibility(View.VISIBLE);
                holder.signup_interest_image.setChecked(true);

                // 체크된 값을 다음 arrayList에 담아 넘긴다.
                arrayList.add(signUpInterestItems.get(position).getInterest_text());
            }
        });
    }

    @Override
    public int getItemCount() {
        return signUpInterestItems.size();
    }

    //커스텀 뷰홀더를 만들어 SignUPInterestItem에 존재하는 항목들을 바인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CheckableImageView signup_interest_image;
        private FrameLayout signup_interest_check;
        private TextView signup_interest_text;

        ViewHolder(View itemView) {
            super(itemView);
            signup_interest_image = itemView.findViewById(R.id.signup_interest_image);
            signup_interest_check = itemView.findViewById(R.id.signup_interest_check);
            signup_interest_text = itemView.findViewById(R.id.signup_interest_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }
}
