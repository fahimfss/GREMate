package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class WordSetWithId extends WordSet {
    private String id;

//    public WordSet(String name, String mainList, int wordCount)

    public WordSetWithId(String name, String mainList, String id) {
        super(name, mainList);
        this.id = id;
    }
    public WordSetWithId(WordSet ws, String id) {
        super(ws.getName(), ws.getMainList());
        this.id = id;
    }
    public WordSetWithId() {
        super();
        this.id = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
