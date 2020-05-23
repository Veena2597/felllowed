package com.example.felllowed;

public class member {
    private String username;
    private String email;
    private String password;
    private Double latitude;
    private Double longitude;

    public member(){

    }

    public String getUsername(){
        return username;
    }

    public String getEmail(){
        return email;
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
}
