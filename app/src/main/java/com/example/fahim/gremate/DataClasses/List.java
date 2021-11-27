package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class List {
    private String name;


    public List() {
        this.name = "";
    }

    public List(String name) {
        this.name = name;
    }

    public static List getNewList(String listName){
        return new List(listName);
    }

    public static List getAllList(){
        return new List("All Words");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
