package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/15/16.
 */

public class UserData {
    private String userName, userEmail;

    public UserData(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public UserData() {
        userName = "";
        userEmail = "";
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
