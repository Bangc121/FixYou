package com.example.kimjeonghwan.fixyou.friend;

/**
 * Created by KimJeongHwan on 2018-12-10.
 */

public class FriendRecommandItem {
    private String friend_name;
    private String friend_email;
    private String friend_profile;
    private String friend_nation;
    private String friend_location;
    private String friend_language;

    String getFriend_name(){
        return friend_name;
    }

    public void setFriend_name(String friend_name){
        this.friend_name = friend_name;
    }

    String getFriend_email(){
        return friend_email;
    }

    public void setFriend_email(String friend_email){
        this.friend_email = friend_email;
    }

    String getFriend_profile(){
        return friend_profile;
    }

    public void setFriend_profile(String friend_profile){
        this.friend_profile = friend_profile;
    }

    String getFriend_nation(){
        return friend_nation;
    }

    public void setFriend_nation(String friend_nation){
        this.friend_nation = friend_nation;
    }

    String getFriend_location(){
        return friend_location;
    }

    public void setFriend_location(String friend_location){
        this.friend_location = friend_location;
    }

    String getFriend_language(){
        return friend_language;
    }

    public void setFriend_language(String friend_language){
        this.friend_language = friend_language;
    }
}
