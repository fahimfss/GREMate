package com.example.fahim.gremate.DataClasses;

import java.util.Comparator;

/**
 * Created by Fahim on 27-Dec-16.
 */

public class WordWithId extends Word {
    private String id;
    private int serial;

    public WordWithId() {
        super();
        this.id = "";
    }

//    public Word(String sourceListName, String cloneOf, String value,
//                boolean practicable, int validity, int level)

    public WordWithId(String listName, String cloneOf, String value, boolean practicable,
                      int validity, int level, String id, int serial) {
        super(listName, cloneOf, value, practicable, validity, level);
        this.id = id;
        this.serial = serial;
    }

    public WordWithId(Word word, String id, int serial){
        super(word.getSourceListName(), word.getCloneOf(), word.getValue(),
                word.isPracticable(), word.getValidity(), word.getLevel());
        this.id = id;
        this.serial = serial;
    }

    public boolean isClone(){
        return !id.equals(getCloneOf());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Word toWord(){
        return new Word(getSourceListName(), getCloneOf(), getValue(),
                isPracticable(), getValidity(), getLevel());
    }

    public static Comparator<WordWithId> alphabetical_Asc = new Comparator<WordWithId>() {
        public int compare(WordWithId w1, WordWithId w2) {
            return w1.getValue().toUpperCase().compareTo(w2.getValue().toUpperCase());
        }
    };

    public static Comparator<WordWithId> alphabetical_Dsc = new Comparator<WordWithId>() {
        public int compare(WordWithId w1, WordWithId w2) {
            return w2.getValue().toUpperCase().compareTo(w1.getValue().toUpperCase());
        }
    };

    public static Comparator<WordWithId> difficulty_Asc = new Comparator<WordWithId>() {
        public int compare(WordWithId w1, WordWithId w2) {
            return w1.getLevel() - w2.getLevel();
        }
    };

    public static Comparator<WordWithId> difficulty_Dsc = new Comparator<WordWithId>() {
        public int compare(WordWithId w1, WordWithId w2) {
            return w2.getLevel() - w1.getLevel();
        }
    };

    public static Comparator<WordWithId> addedTime_Asc = new Comparator<WordWithId>() {
        public int compare(WordWithId w1, WordWithId w2) {
            return w1.getSerial() - w2.getSerial();
        }
    };

    public static Comparator<WordWithId> addedTime_Dsc = new Comparator<WordWithId>() {
        public int compare(WordWithId w1, WordWithId w2) {
            return w2.getSerial() - w1.getSerial();
        }
    };

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }
}
