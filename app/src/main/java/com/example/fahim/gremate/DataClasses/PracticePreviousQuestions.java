package com.example.fahim.gremate.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by Fahim on 02-Mar-18.
 */

public class PracticePreviousQuestions implements Parcelable {
    private String[] values;

    public PracticePreviousQuestions(String id, String value, String ans1,
                              String ans2, String ans3, String ans4,
                              String ans5, int correctIndex, boolean wasCorrect){

        values = new String[] {id, value, ans1, ans2, ans3, ans4, ans5,
            String.valueOf(correctIndex), String.valueOf(wasCorrect)};
    }

    public PracticePreviousQuestions(){
        values = new String[9];
    }

    public PracticePreviousQuestions(String fileLine){
        values = fileLine.split(DB.DELIM);
    }

    public String getId(){
        return values[0];
    }
    public String getValue(){
        return values[1];
    }
    public String getAns1(){
        return values[2];
    }
    public String getAns2(){
        return values[3];
    }
    public String getAns3(){
        return values[4];
    }
    public String getAns4(){
        return values[5];
    }
    public String getAns5(){
        return values[6];
    }
    public int getCorrectIndex(){
        return Integer.valueOf(values[7]);
    }
    public boolean getWasCorrect(){
        return Boolean.valueOf(values[8]);
    }

    public String getCorrectAns(){
        int ci = Integer.valueOf(values[7]);
        return values[ci+2];
    }

    public String getStringForm(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < values.length; i++){
            if(i > 0) sb.append(DB.DELIM);
            sb.append(values[i]);
        }
        sb.append("\n");
        return sb.toString();
    }

    public void setId(String id){
        values[0] = id;
    }
    public void setValue(String value){
        values[1] = value;
    }
    public void setAns1(String ans1){
        values[2] = ans1;
    }
    public void setAns2(String ans2){
        values[3] = ans2;
    }
    public void setAns3(String ans3){
        values[4] = ans3;
    }
    public void setAns4(String ans4){
        values[5] = ans4;
    }
    public void setAns5(String ans5){
        values[6] = ans5;
    }
    public void setCorrectIndex(int correctIndex){
        values[7] = "" + correctIndex;
    }
    public void setWasCorrect(boolean wasCorrect){
        values[8] = "" + wasCorrect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(values);
    }

    private PracticePreviousQuestions(Parcel in) {
        values = in.createStringArray();
    }

    public static final Parcelable.Creator<PracticePreviousQuestions> CREATOR
            = new Parcelable.Creator<PracticePreviousQuestions>() {

        @Override
        public PracticePreviousQuestions createFromParcel(Parcel in) {
            return new PracticePreviousQuestions(in);
        }

        @Override
        public PracticePreviousQuestions[] newArray(int size) {
            return new PracticePreviousQuestions[size];
        }
    };


}
