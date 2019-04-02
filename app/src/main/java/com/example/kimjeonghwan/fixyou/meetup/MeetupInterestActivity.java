package com.example.kimjeonghwan.fixyou.meetup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.profile.InterestItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeetupInterestActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<MeetupInterestItem> meetupInterestItems;
    MeetupInterestItem meetupInterestItem;

    List<String> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_interest);

        recyclerView = findViewById(R.id.meetup_interest_recyclerview);
        getData();
    }

    /**
     * 관심사 리스트 초기화
     */
    private void initInterest(){
        recyclerView.setAdapter(new MeetupInterestAdapter(meetupInterestItems, getApplicationContext(), arrayList));
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void getData() {
        meetupInterestItems = new ArrayList<>();
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
            meetupInterestItem = new MeetupInterestItem();
            //meetupInterestItem.setInterest_uri(listUri.get(i));
            meetupInterestItem.setInterest_text(listText.get(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            meetupInterestItems.add(meetupInterestItem);
        }

        initInterest();
    }
}
