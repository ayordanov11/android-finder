package com.example.gareth.androidfinder;

/**
 * Created by Gareth on 03/06/2017.
 */

public class User {

    private String uid;
    private String username;
    private double longitude;
    private double latitude;
    private String email;

    public User(){
        this.latitude = 0;
        this.longitude = 0;
    }

    public User(String username,String uid) {
        this.username = username;
        this.latitude = 0;
        this.longitude = 0;
        this.uid = uid;
    }

    public User(double latitude,double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}