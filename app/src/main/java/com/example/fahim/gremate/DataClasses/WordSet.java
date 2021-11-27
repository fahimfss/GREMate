package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSet {
    private String name, mainList;

    public WordSet() { }

    public WordSet(String name, String mainList) {
        this.name = name;
        this.mainList = mainList;
    }

    public static WordSet newWordSet (String name, String mainList){
        return new WordSet(name, mainList);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainList() {
        return mainList;
    }

    public void setMainList(String mainList) {
        this.mainList = mainList;
    }
}
