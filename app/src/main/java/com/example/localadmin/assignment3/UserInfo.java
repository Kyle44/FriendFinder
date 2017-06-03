package com.example.localadmin.assignment3;

/**
 * Created by LocalAdmin on 5/17/2017.
 */

public class UserInfo {
    private String username, password;
    private Float lat, lon;

    public UserInfo(){

    }


    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public Float getLat() {
        return lat;
    }
    public Float getLon() {
        return lon;
    }

    public void setUsername(String Username) {
        username = Username;
    }
    public void setPassword(String Password) {
        password = Password;
    }
    public void setLat(Float Lat) { lat = Lat; }
    public void setLon(Float Lon) {
        lon = Lon;
    }

}

