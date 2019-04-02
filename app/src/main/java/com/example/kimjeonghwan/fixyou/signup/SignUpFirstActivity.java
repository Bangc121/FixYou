package com.example.kimjeonghwan.fixyou.signup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.example.kimjeonghwan.fixyou.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignUpFirstActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<SignUpInterestItem> signUpInterestItems;
    SignUpInterestItem signUpInterestItem;
    Intent intent;

    Button signup_next_button;

    List<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_first);

        recyclerView = findViewById(R.id.signup_interest_recyclerview);
        signup_next_button = findViewById(R.id.signup_next_button);

        getData();

        intent = new Intent(this, SignUpActivity.class);

        signup_next_button.setOnClickListener(v -> {
            intent.putStringArrayListExtra("interest_text", (ArrayList<String>) arrayList);
            startActivity(intent);
        });
    }

    /**
     * 관심사
     */
    private void initInterest(){
        recyclerView.setAdapter(new SignUpInterestAdapter(signUpInterestItems, getApplicationContext(), intent, arrayList));
        //그리드 레이아웃
        GridLayoutManager gridLayoutManager
                = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void getData() {
        signUpInterestItems = new ArrayList<>();
        // 임의의 데이터입니다.
        List<String> listText = Arrays.asList("예술", "음식", "게임", "건강", "영화", "음악", "사진", "스포츠",
                "기술");
        List<String> listUri = Arrays.asList(
                "http://52.79.228.68/photo/art.jpg",
                "http://52.79.228.68/photo/food.jpg",
                "http://52.79.228.68/photo/game.jpg",
                "http://52.79.228.68/photo/health.jpg",
                "http://52.79.228.68/photo/movie.png",
                "http://52.79.228.68/photo/music.jpg",
                "http://52.79.228.68/photo/photo.png",
                "http://52.79.228.68/photo/sport.jpg",
                "http://52.79.228.68/photo/tech.jpg"
        );
        for (int i = 0; i < listUri.size(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            signUpInterestItem = new SignUpInterestItem();
            signUpInterestItem.setInterest_uri(listUri.get(i));
            signUpInterestItem.setInterest_text(listText.get(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            signUpInterestItems.add(signUpInterestItem);
        }

        initInterest();
    }
}
