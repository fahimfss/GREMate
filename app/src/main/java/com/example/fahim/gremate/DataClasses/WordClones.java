package com.example.fahim.gremate.DataClasses;

/**
 * Created by Fahim on 05-Jan-17.
 */

public class WordClones {
    private String cloneId, listId;

    public WordClones(String listId, String cloneId) {
        this.cloneId = cloneId;
        this.listId = listId;
    }

    public WordClones() { }

    public String getCloneId() {
        return cloneId;
    }

    public void setCloneId(String cloneId) {
        this.cloneId = cloneId;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }
}
