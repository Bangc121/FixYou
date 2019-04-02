package com.example.kimjeonghwan.fixyou.vod;

import java.io.Serializable;

/**
 * Created by KimJeongHwan on 2019-03-07.
 */

public class VodItem implements Serializable {
    private String vod_thumbnail;
    private String vod_name;
    private String vod_creator;
    private String vod_viewer;
    private String vod_url;
    private String vod_id;
    private String vod_position;

    public String getVod_position(){
        return vod_position;
    }

    public void setVod_position(String vod_position){
        this.vod_position = vod_position;
    }

    public String getVod_id(){
        return vod_id;
    }

    public void setVod_id(String vod_id){
        this.vod_id = vod_id;
    }

    public String getVod_thumbnail(){
        return vod_thumbnail;
    }

    public void setVod_thumbnail(String vod_thumbnail){
        this.vod_thumbnail = vod_thumbnail;
    }

    public String getVod_name(){
        return vod_name;
    }

    public void setVod_name(String vod_name){
        this.vod_name = vod_name;
    }

    public String getVod_creator(){
        return vod_creator;
    }

    public void setVod_creator(String vod_creator){
        this.vod_creator = vod_creator;
    }

    public String getVod_viewer(){
        return vod_viewer;
    }

    public void setVod_viewer(String vod_viewer){
        this.vod_viewer = vod_viewer;
    }

    public String getVod_url(){
        return vod_url;
    }

    public void setVod_url(String vod_url){
        this.vod_url = vod_url;
    }
}
