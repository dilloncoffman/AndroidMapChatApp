package edu.temple.mapchatapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dilloncoffman on 2020-02-11
 */
public class User implements Comparable<User>, Parcelable {
    private static final String TAG = "User";
    private String name;
    private double latitude;
    private double longitude;
    private float distanceToCurrentUser;

    public User (String name, double latitude, double longitude, float distanceToCurrentUser) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceToCurrentUser = distanceToCurrentUser;
    }

    protected User(Parcel in) {
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public int compareTo(User user) {
        if (this.distanceToCurrentUser > user.distanceToCurrentUser) {
            return 1;
        } else {
            return -1;
        }
    }
}
