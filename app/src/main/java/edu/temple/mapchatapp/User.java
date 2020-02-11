package edu.temple.mapchatapp;

import java.security.interfaces.RSAPublicKey;

/**
 * Created by dilloncoffman on 2020-02-11
 */
public class User implements Comparable {
    private String name;
    private double latitude;
    private double longitude;

    public User (String name, double latitude, double longitude, RSAPublicKey publicKey) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
