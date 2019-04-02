package com.example.kimjeonghwan.fixyou.profile.camera;

/**
 * Created by KimJeongHwan on 2019-02-26.
 */

public class MaskItem {
    private String name;
    private Boolean isMask;
    private Boolean isSelected;

    String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    Boolean getIsMask(){
        return isMask;
    }

    public void setIsMask(Boolean isMask){
        this.isMask = isMask;
    }

    Boolean getIsSelected(){
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected){
        this.isSelected = isSelected;
    }
}

