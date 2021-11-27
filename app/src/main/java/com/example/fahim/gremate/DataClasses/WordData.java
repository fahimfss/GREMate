package com.example.fahim.gremate.DataClasses;

import com.google.firebase.database.Exclude;

/**
 * Created by fahim on 12/23/16.
 */

public class WordData {
    private String des, extraInfo, pronunciation;

    public WordData() {
        this.des = "";
        this.extraInfo = "";
        pronunciation = "";
    }

    public WordData(String des, String extraInfo, String pronunciation) {
        this.des = des;
        this.extraInfo = extraInfo;
        this.pronunciation = pronunciation;
    }

    public String getDes() {
        return des;
    }

    @Exclude
    public String getHighlightedDes(String word){
        try {
            String textRaw = des;
            return Highlighter.highlight(textRaw, word);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }
}
