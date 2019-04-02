package com.example.kimjeonghwan.fixyou.chat;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by KimJeongHwan on 2019-02-02.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private ArrayList<ChatListItem> chatListItems;
    private Context mContext;
    private Intent intent;

    ChatListAdapter(ArrayList<ChatListItem> chatListItems, Context mContext){
        this.chatListItems = chatListItems;
        this.mContext = mContext;

        intent = new Intent(mContext, ChatActivity.class);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("ChatListAdapter", "onBindViewHolder position(" + position + ")");
        profileCall(holder, chatListItems.get(position).getEmail());
        //holder.chatName.setText(chatListItems.get(position).getName());
        holder.chatContent.setText(chatListItems.get(position).getContent());
        holder.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("email", chatListItems.get(position).getEmail());
                intent.putExtra("roomId", chatListItems.get(position).getRoomId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatListItems.size();
    }

    //커스텀 뷰홀더를 만들어 ChatListItem 존재하는 항목들을 바인딩 합니다.
    class ViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout chatLayout;
        private ImageView chatProfile;
        private TextView chatName;
        private TextView chatContent;

        ViewHolder(View itemView) {
            super(itemView);
            chatLayout = itemView.findViewById(R.id.chatLayout);
            chatProfile = itemView.findViewById(R.id.chatProfile);
            chatName = itemView.findViewById(R.id.chatName);
            chatContent = itemView.findViewById(R.id.chatContent);
        }
    }

    private void profileCall(ViewHolder holder, String email) {
        Call<ResponseBody> call = RetrofitClient.getInstance().getService().profileCall(email);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    //프로필정보 받아옴
                    String res = response.body().string();
                    Log.e("profile", res);
                    JSONObject json_profile = new JSONObject(res);
                    String profile_info = json_profile.getString("profile");
                    JSONArray json_info = new JSONArray(profile_info);
                    JSONObject jsonObject = json_info.getJSONObject(0);
                    String receive_name = jsonObject.getString("name");
                    String receive_profile = jsonObject.getString("profile");
                    Log.e("receive_name", receive_name);
                    Log.e("receive_profile", receive_profile);
                    intent.putExtra("profile", receive_profile);
                    intent.putExtra("name", receive_name);
                    holder.chatName.setText(receive_name);
                    Glide.with(mContext)
                            .load(receive_profile)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(25)))
                            .into(holder.chatProfile);
                } catch (IOException | JSONException e) {
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
