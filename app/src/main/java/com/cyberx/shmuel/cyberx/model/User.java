package com.cyberx.shmuel.cyberx.model;

public class User {
    public String ID;
    String username;
    String password;
    String salt;
    String imageUrl;

    public User(){}
    public User(String usernameString, String passwordString,String salt) {
        username=usernameString;
        password=passwordString;
        this.salt=salt;
    }
    public User(User user){
        this.ID=user.ID;
        this.username=user.username;
        this.password=user.password;
        this.salt=user.salt;
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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
