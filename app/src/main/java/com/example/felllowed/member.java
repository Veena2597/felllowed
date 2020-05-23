package com.example.felllowed;

public class member {
    private String username;
    private String email;
    private String password;

    private Double latitude;
    private Double longitude;

    public member(){

    }
    public member(String username, String email){
        this.username = username;
        this.email = email;
    }
    public String getUsername(){
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
