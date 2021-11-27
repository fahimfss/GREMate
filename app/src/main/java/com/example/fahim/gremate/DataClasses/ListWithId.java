package com.example.fahim.gremate.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Fahim on 25-Dec-16.
 */

public class ListWithId extends List implements Parcelable {
    String id;

    public ListWithId(String name, String id) {
        super(name);
        this.id = id;
    }

    public ListWithId(List wordList, String id) {
        super(wordList.getName());
        this.id = id;
    }

    public ListWithId(){
        super();
        id  = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getName());
        parcel.writeString(getId());
    }

    private ListWithId(Parcel in) {
        setName(in.readString());
        setId(in.readString());
    }

    public static final Parcelable.Creator<ListWithId> CREATOR
            = new Parcelable.Creator<ListWithId>() {

        @Override
        public ListWithId createFromParcel(Parcel in) {
            return new ListWithId(in);
        }

        @Override
        public ListWithId[] newArray(int size) {
            return new ListWithId[size];
        }
    };
}
