package com.example.kimjeonghwan.fixyou.profile.camera;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kimjeonghwan.fixyou.R;
import java.util.ArrayList;

/**
 * Created by KimJeongHwan on 2019-02-26.
 */

public class MaskAdapter extends RecyclerView.Adapter<MaskAdapter.ViewHolder> {
    private ArrayList<MaskItem> maskItems;
    private Context mContext;
    private int maskPosition;
    private int [] maskDrawable = {R.drawable.captin, R.drawable.starwars, R.drawable.op, R.drawable.iron, R.drawable.cat, R.drawable.dog, R.drawable.crown};
    public MaskAdapter(ArrayList<MaskItem> maskItems, Context mContext, int maskPosition){
        this.maskItems = maskItems;
        this.mContext = mContext;
        this.maskPosition = maskPosition;
    }

    @NonNull
    @Override
    public MaskAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_mask, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaskAdapter.ViewHolder holder, int position) {
        Log.d("MaskAdapter", "onBindViewHolder position(" + position + ")");
        //MaskItem mask = maskItems.get(position);
        if(maskItems.get(position).getIsMask()){    // 마스크를 선택했다면
            holder.maskNone.setVisibility(View.GONE);   // None 텍스트뷰를 안보이게한다.
            holder.maskImage.setVisibility(View.VISIBLE);    // 마스크 이미지뷰를 보이게한다.

            holder.maskImage.setImageDrawable(ActivityCompat.getDrawable(mContext, maskDrawable[position-1]));   // 마스크 이미지뷰에 마스크 Drawable 를 넣는다.
        }else{  // 마스크 선택을 해제했다면
            holder.maskNone.setVisibility(View.VISIBLE);    // None 텍스트뷰를 안보이게한다.
            holder.maskImage.setVisibility(View.GONE);    // 마스크 이미지뷰를 보이게한다.
        }

        if(maskItems.get(position).getIsSelected()){        // 현재 선택된 마스크라면
            holder.maskSelected.setVisibility(View.VISIBLE); // 선택되었음을 알려주는 테두리 레이아웃을 보이게한다.
        }else{
            holder.maskSelected.setVisibility(View.GONE);    // 테두리 레이아웃을 숨긴다.
        }
    }

    @Override
    public int getItemCount() {
        return maskItems.size();
    }

    /*
    * 마스크 선택시 테두리가 보이게 하는 메소드
    */
    public void setSelect(int position){
        // 전체 마스크 리스트의 선택을 false 로 초기화한다.
        for(int i = 0; i < maskItems.size(); i++){
            maskItems.get(i).setIsSelected(false);
            Log.d("setIsSelected", String.valueOf(maskItems.get(i).getIsSelected()));
        }
        // 선택한 위치의 마스크의 선택된 상태를 true 로 변경한다.
        maskItems.get(position).setIsSelected(true);
        notifyDataSetChanged();  // 리사이클러 뷰에 변경된 사항에 대해 알림
    }

    //커스텀 뷰홀더를 만들어 MaskItem 존재하는 항목들을 바인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView maskNone;
        private ImageView maskImage;
        private RelativeLayout maskSelected;

        ViewHolder(View itemView) {
            super(itemView);
            maskNone = itemView.findViewById(R.id.maskNone);
            maskImage = itemView.findViewById(R.id.maskImage);
            maskSelected = itemView.findViewById(R.id.maskSelected);
        }
    }
}
