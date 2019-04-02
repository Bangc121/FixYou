package com.example.kimjeonghwan.fixyou.live;

/**
 * Created by KimJeongHwan on 2018-11-16.
 */

public class BroadCastItem {
    private String broadcast_name;
    private String broadcast_sessionid;
    private String broadcast_creator;
    private String broadcast_viewer;
    private String broadcast_thumbnail;

    public String getBroadcast_name(){
        return broadcast_name;
    }

    public void setBroadcast_name(String broadcast_name){
        this.broadcast_name = broadcast_name;
    }

    String getBroadcast_sessionid(){
        return broadcast_sessionid;
    }

    public void setBroadcast_sessionid(String broadcast_sessionid){
        this.broadcast_sessionid = broadcast_sessionid;
    }

    public String getBroadcast_creator(){
        return broadcast_creator;
    }

    public void setBroadcast_creator(String broadcast_creator){
        this.broadcast_creator = broadcast_creator;
    }

    public String getBroadcast_viewer(){
        return broadcast_viewer;
    }

    public void setBroadcast_viewer(String broadcast_viewer){
        this.broadcast_viewer = broadcast_viewer;
    }

    public String getBroadcast_thumbnail(){
        return broadcast_thumbnail;
    }

    public void setBroadcast_thumbnail(String broadcast_thumbnail){
        this.broadcast_thumbnail = broadcast_thumbnail;
    }
}
