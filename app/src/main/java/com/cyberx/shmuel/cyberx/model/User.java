package com.cyberx.shmuel.cyberx.model;

public class User {
    public String ID;
    String username;
    String password;

    public User(){}
    public User(String usernameString, String passwordString) {
        username=usernameString;
        password=passwordString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
