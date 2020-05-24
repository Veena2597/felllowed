package com.example.felllowed;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class member {
    private String username;
    private String email;
    private String password;
    private String phone;
    private Double latitude;
    private Double longitude;
    private List<String> friends = new ArrayList<>();
    private int numFriends = 0;

    public member(String username, String email, String phone){
        this.username = username;
        this.email = email;
        this.phone = phone;
    }
    public String getUsername(){
        return username;
    }
    public String getEmail(){
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Double getLatitude() {
        return latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void updateFriendsList(String username){
        System.out.println(username);
        friends.add(username);
        //numFriends++;
    }
}
