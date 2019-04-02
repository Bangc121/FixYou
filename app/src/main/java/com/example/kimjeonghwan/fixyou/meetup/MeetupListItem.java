package com.example.kimjeonghwan.fixyou.meetup;

/**
 * Created by KimJeongHwan on 2018-11-29.
 */

public class MeetupListItem {
    private String meetup_id;
    private String meetup_title;
    private String meetup_content;
    private String meetup_creater;
    private String meetup_picture;

    public String getMeetup_id(){
        return meetup_id;
    }

    public void setMeetup_id(String meetup_id){
        this.meetup_id = meetup_id;
    }

    public String getMeetup_title(){
        return meetup_title;
    }

    public void setMeetup_title(String meetup_title){
        this.meetup_title = meetup_title;
    }

    public String getMeetup_content(){
        return meetup_content;
    }

    public void setMeetup_content(String meetup_content){
        this.meetup_content = meetup_content;
    }

    public String getMeetup_creater(){
        return meetup_creater;
    }

    public void setMeetup_creater(String meetup_creater){
        this.meetup_creater = meetup_creater;
    }

    public String getMeetup_picture() {
        return meetup_picture;
    }

    public void setMeetup_picture(String meetup_picture) {
        this.meetup_picture = meetup_picture;
    }

}
