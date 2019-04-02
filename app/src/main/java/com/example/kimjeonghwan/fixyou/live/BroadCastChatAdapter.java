package com.example.kimjeonghwan.fixyou.live;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.chat.ChatMessageItem;
import com.example.kimjeonghwan.fixyou.live.bases.BaseViewHolder;
import com.example.kimjeonghwan.fixyou.live.bases.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Created by KimJeongHwan on 2018-11-22.
 */

public class BroadCastChatAdapter extends RecyclerView.Adapter<BroadCastChatAdapter.ViewHolder> {
    //Naver
    public static String clientId = "f0Soqo75nGaxmN9xVdVh";  // 애플리케이션 클라이언트 아이디값";
    public static String clientSecret = "sYq1A0jePO";  // 애플리케이션 클라이언트 시크릿값";
    // 언어선택 옵션
    String sourceLang = "en";  // 번역하기 전 언어
    String targetLang = "ko";  // 번역 후 언어

    private static final int SYSTEM_VIEW = 0;
    private static final int SELF_VIEW = 1;
    private static final int RECEIVED_VIEW = 2;
    private static final int BALLOON_VIEW = 3;

    private ArrayList<ChatMessageItem> chatMessageItems;
    private Context mContext;

    BroadCastChatAdapter(ArrayList<ChatMessageItem> chatMessageItems, Context mContext){
        this.chatMessageItems = chatMessageItems;
        this.mContext = mContext;

        notifyDataSetChanged();
    }

    public class ViewHolder extends BaseViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class BalloonHolder extends ViewHolder {
        private TextView text_send;
        public BalloonHolder(View itemView) {
            super(itemView);
            text_send = itemView.findViewById(R.id.text_send);
        }
    }

    public class SelfHolder extends ViewHolder {
        private TextView broadcast_chat_nickname;
        private TextView broadcast_chat_contents;
        private Button broadcast_chat_trans;
        private LinearLayout broadcast_trans_layout;
        private TextView broadcast_trans_contents;
        public SelfHolder(View itemView) {
            super(itemView);
            broadcast_chat_nickname = itemView.findViewById(R.id.broadcast_chat_nickname);
            broadcast_chat_contents = itemView.findViewById(R.id.broadcast_chat_contents);
            broadcast_chat_trans = itemView.findViewById(R.id.broadcast_chat_trans);
            broadcast_trans_layout = itemView.findViewById(R.id.broadcast_trans_layout);
            broadcast_trans_contents = itemView.findViewById(R.id.broadcast_trans_contents);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageItem chatMessageItem = chatMessageItems.get(position);
        String messageType = chatMessageItem.getMessageType();
        if (messageType.equals(Constants.MESSAGE_TYPE_SYSTEM)) {
            return SYSTEM_VIEW;
        } else if (messageType.equals(Constants.MESSAGE_TYPE_SELF)) {
            return SELF_VIEW;
        } else if (messageType.equals(Constants.MESSAGE_TYPE_RECEIVE)) {
            return RECEIVED_VIEW;
        } else if (messageType.equals(Constants.MESSAGE_TYPE_BALLOON)) {
            return BALLOON_VIEW;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;
        switch (viewType) {
            case SELF_VIEW:
                view = inflater.inflate(R.layout.recyclerview_broadcast_chat, parent, false);
                return new SelfHolder(view);
            case BALLOON_VIEW:
                view = inflater.inflate(R.layout.recyclerview_broadcast_balloon, parent, false);
                return new BalloonHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Log.d("BroadCastChatAdapter", "onBindViewHolder position(" + position + ")");
        if (viewType == SELF_VIEW) {
            SelfHolder selfHolder = (SelfHolder) holder;

            selfHolder.broadcast_chat_nickname.setText(chatMessageItems.get(position).getName());
            selfHolder.broadcast_chat_contents.setText(chatMessageItems.get(position).getContents());
            selfHolder.broadcast_chat_trans.setVisibility(chatMessageItems.get(position).getTransV());
            selfHolder.broadcast_trans_layout.setVisibility(chatMessageItems.get(position).getTextV());
            selfHolder.broadcast_trans_contents.setText(chatMessageItems.get(position).getTranslatedText());

            // 채팅중 영어를 감지해서 번역버튼이 나타남
            detectTask detectTask = new detectTask();
            try {
                String result = detectTask.execute(chatMessageItems.get(position).getContents()).get();
                //감지된 결과를 받아서 처리하는 부분
                Log.e("detectTask", result);
                chatMessageItems.get(position).setLanguage(result);
                if(Objects.equals(chatMessageItems.get(position).getLanguage(), "en")){
                    chatMessageItems.get(position).setTransV(View.VISIBLE);
                    selfHolder.broadcast_chat_trans.setVisibility(chatMessageItems.get(position).getTransV());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            selfHolder.broadcast_chat_trans.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("trans", sourceLang);
                    translateTask translateTask = new translateTask();
                    try {
                        String result = translateTask.execute(chatMessageItems.get(position).getContents()).get();
                        //번역된 결과를 받아서 처리하는 부분
                        //JSON데이터를 자바객체로 변환해야 한다.
                        Gson gson = new GsonBuilder().create();
                        JsonParser parser = new JsonParser();
                        JsonElement rootObj = parser.parse(result)
                                //원하는 데이터 까지 찾아 들어간다.
                                .getAsJsonObject().get("message")
                                .getAsJsonObject().get("result");
                        //안드로이드 객체에 담기
                        ChatMessageItem items = gson.fromJson(rootObj.toString(), ChatMessageItem.class);
                        Log.d("translate", items.getTranslatedText());
                        chatMessageItems.get(position).setTranslatedText(items.getTranslatedText());
                        chatMessageItems.get(position).setTextV(View.VISIBLE);
                        notifyDataSetChanged();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });


        } else if (viewType == BALLOON_VIEW) {
            BalloonHolder balloonHolder = (BalloonHolder) holder;
            balloonHolder.text_send.setText(chatMessageItems.get(position).getName()+"님이 별풍선 "+chatMessageItems.get(position).getBalloon()+"개를 선물하셨습니다.");
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageItems.size();
    }

    // 언어 감지를 담당하는 AsynkTask
    @SuppressLint("StaticFieldLeak")
    public class detectTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // 번역 처리를 담당하는 부분
        @Override
        protected String doInBackground(String... strings) {
            String sourceText = strings[0];
            try {
                String query = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "query=" + query;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(response.toString());
                return removeDoubleQuotes(jsonObject.get("langCode").toString());
            } catch (Exception e) {
                //System.out.println(e);
                Log.d("error", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    // 번역을 담당하는 AsynkTask
    @SuppressLint("StaticFieldLeak")
    public class translateTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // 번역 처리를 담당하는 부분
        @Override
        protected String doInBackground(String... strings) {
            String sourceText = strings[0];
            try {
                String text = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/language/translate";
                //String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source="+sourceLang+"&target="+targetLang+"&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                Log.d("respone", response.toString());
                return response.toString();

            } catch (Exception e) {
                //System.out.println(e);
                Log.d("error", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public static String removeDoubleQuotes(String input){

        StringBuilder sb = new StringBuilder();

        char[] tab = input.toCharArray();
        for( char current : tab ){
            if( current != '"' )
                sb.append( current );
        }

        return sb.toString();
    }
}
