package com.example.kimjeonghwan.fixyou.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.live.bases.BaseViewHolder;
import com.example.kimjeonghwan.fixyou.live.bases.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by KimJeongHwan on 2018-11-19.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
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

    ChatMessageAdapter(ArrayList<ChatMessageItem> chatMessageItems, Context mContext){
        this.chatMessageItems = chatMessageItems;
        this.mContext = mContext;

        notifyDataSetChanged();
    }

    public class ViewHolder extends BaseViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SelfHolder extends ViewHolder {
        private TextView selfContents;
        private TextView selfTranslate;
        private LinearLayout selfMessageBox;
        public SelfHolder(View itemView) {
            super(itemView);
            selfContents = itemView.findViewById(R.id.selfContents);
            selfTranslate = itemView.findViewById(R.id.selfTranslate);
            selfMessageBox = itemView.findViewById(R.id.selfMessageBox);
        }
    }

    public class ReceiveHolder extends ViewHolder {
        private TextView TextView_Contents;
        private TextView TextView_Name;
        private ImageView Image_Profile;
        private TextView TextView_Translate;
        private LinearLayout MessageBox;
        public ReceiveHolder(View itemView) {
            super(itemView);
            TextView_Contents = itemView.findViewById(R.id.TextView_Contents);
            TextView_Name = itemView.findViewById(R.id.TextView_Name);
            Image_Profile = itemView.findViewById(R.id.Image_Profile);
            TextView_Translate = itemView.findViewById(R.id.TextView_Translate);
            MessageBox = itemView.findViewById(R.id.MessageBox);
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
                view = inflater.inflate(R.layout.recyclerview_chat_self, parent, false);
                return new SelfHolder(view);
            case RECEIVED_VIEW:
                view = inflater.inflate(R.layout.recyclerview_chat_receive, parent, false);
                return new ReceiveHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageAdapter.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Log.d("ChatMessageAdapter", "onBindViewHolder position(" + position + ")");
        if (viewType == SELF_VIEW) {
            SelfHolder selfHolder = (SelfHolder) holder;

            selfHolder.selfContents.setText(chatMessageItems.get(position).getContents());
        } else if (viewType == RECEIVED_VIEW) {
            ReceiveHolder receiveHolder = (ReceiveHolder) holder;

            receiveHolder.TextView_Contents.setText(chatMessageItems.get(position).getContents());
            receiveHolder.TextView_Name.setText(chatMessageItems.get(position).getName());
            Glide.with(mContext)
                    .load(chatMessageItems.get(position).getProfile())
                    .apply(RequestOptions.circleCropTransform())
                    .into(receiveHolder.Image_Profile);
            // 번역결과가 있을때 챗아이템에 추가하고 리사이클러뷰를 갱신한다.
            if(chatMessageItems.get(position).getTranslatedText() != null){
                receiveHolder.TextView_Translate.setVisibility(View.VISIBLE);
                receiveHolder.TextView_Translate.setText(chatMessageItems.get(position).getTranslatedText());
            }
            // 채팅 메시지를 롱클릭 했을 때 발생하는 이벤트
            receiveHolder.MessageBox.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String chatText = chatMessageItems.get(position).getContents();
                    showMenu(v, chatText, position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageItems.size();
    }

    // 채팅 메시지를 클릭했을 때 보여지는 팝업메뉴
    @SuppressLint("RestrictedApi")
    private void showMenu(View v, String translateText, int position) {
        PopupMenu popup = new PopupMenu(mContext, v);
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.menu_chat, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_translate:
                        Log.d("menu", "menu_translate");
                        // 번역버튼을 클릭하면
                        translateTask translateTask = new translateTask();
                        try {
                            String result = translateTask.execute(translateText).get();
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
                            notifyDataSetChanged();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        return true;
                    case R.id.menu_reading:
                        Log.d("menu", "menu_reading");
                        return true;
                    case R.id.menu_copy:
                        Log.d("menu", "menu_copy");
                        return true;
                    case R.id.menu_bookmark:
                        Log.d("menu", "menu_bookmark");
                        return true;
                    default:
                        return false;
                }
            }
        });
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
}
