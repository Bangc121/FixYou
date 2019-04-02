package com.example.kimjeonghwan.fixyou.chat;

/**
 * Created by KimJeongHwan on 2018-11-19.
 */

public class ChatMessageItem {
    private String name;
    private String profile;
    private String contents;
    private String translatedText;
    private String balloon;
    private String language;
    private String messageType;
    private int transV = 0x00000008;
    private int textV = 0x00000008;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType){
        this.messageType = messageType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language){
        this.language = language;
    }

    public String getBalloon() {
        return balloon;
    }

    public void setBalloon(String balloon){
        this.balloon = balloon;
    }

    public int getTransV () {
        return transV;
    }

    public void setTransV(int transV){
        this.transV = transV;
    }

    public int getTextV () {
        return textV;
    }

    public void setTextV(int textV){
        this.textV = textV;
    }
}
