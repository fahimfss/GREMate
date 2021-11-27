package com.example.fahim.gremate.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fahim on 12/23/16.
 */

public class Word implements Parcelable{

    public static final int VALID = 1;
    public static final int INVALID = -1;
    public static final int UNKNOWN = 0;

    public static final int LVL_EASY = 0;
    public static final int LVL_NORMAL = 1;
    public static final int LVL_HARD = 2;
    public static final int LVL_VHARD = 3;

    private String cloneOf, value, sourceListName;
    private boolean practicable;
    private int validity,  level;

    public static Word newWord(String listName, String cloneOf,  String value) {
        return new Word(listName, cloneOf, value, false, UNKNOWN, LVL_NORMAL);
    }

    public Word() {
        this.cloneOf = "";
        this.value = "";
        this.sourceListName = "";
        this.practicable = false;
        this.validity = 0;
        this.level = 0;
    }
    public Word(String sourceListName, String cloneOf, String value,
                boolean practicable, int validity, int level) {
        this.cloneOf = cloneOf;
        this.value = value;
        this.sourceListName = sourceListName;
        this.practicable = practicable;
        this.validity = validity;
        this.level = level;
    }

    public Word(String fileLine){
        String []str = fileLine.split(DB.DELIM);

        this.cloneOf = str[0];
        this.value = str[1];
        this.sourceListName = str[2];
        this.practicable = str[3].equals("true");
        this.validity = Integer.valueOf(str[4]);
        this.level = Integer.valueOf(str[5]);
    }

    public String getStringForm(){
        String regex = DB.DELIM;
        return getCloneOf() + regex +
                getValue() + regex +
                getSourceListName() + regex +
                isPracticable() + regex +
                getValidity() + regex +
                getLevel() + "\n";
    }

    public String getSourceListName() {
        return sourceListName;
    }

    public void setSourceListName(String sourceListName) {
        this.sourceListName = sourceListName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getValue() {
        return value.replaceAll("\\s+","");
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPracticable() {
        return practicable;
    }

    public void setPracticable(boolean practicable) {
        this.practicable = practicable;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getCloneOf() {
        return cloneOf;
    }

    public void setCloneOf(String cloneOf) {
        this.cloneOf = cloneOf;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cloneOf);
        parcel.writeString(value);
        parcel.writeString(sourceListName);
        parcel.writeValue(practicable);
        parcel.writeInt(validity);
        parcel.writeInt(level);
    }

    private Word(Parcel in) {
        cloneOf = in.readString();
        value = in.readString();
        sourceListName = in.readString();
        practicable = (Boolean) in.readValue( null );
        validity = in.readInt();
        level = in.readInt();
    }

    public static final Parcelable.Creator<Word> CREATOR
            = new Parcelable.Creator<Word>() {

        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
}
