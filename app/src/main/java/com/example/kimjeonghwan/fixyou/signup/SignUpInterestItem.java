package com.example.kimjeonghwan.fixyou.signup;

import java.util.ArrayList;

/**
 * Created by KimJeongHwan on 2018-12-17.
 */

public class SignUpInterestItem {
    private String interest_uri;
    private String interest_text;

    String getInterest_uri(){
        return interest_uri;
    }

    public void setInterest_uri(String interest_uri){
        this.interest_uri = interest_uri;
    }

    String getInterest_text(){
        return interest_text;
    }

    public void setInterest_text(String interest_text){
        this.interest_text = interest_text;
    }
}
