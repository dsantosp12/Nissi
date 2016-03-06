package com.nissi.nissi;

import com.google.android.gms.maps.model.LatLng;

public class Place {
    private String mId;
    private String mName;
    private String mAddress;
    private String mPhoneNumber;
    private String mURL;
    private double mLongitude;
    private double mLatitude;

    public Place(String id, String name, String address,
                 String phoneNumber, String URL,
                 double longitude, double latitude) {
        mId = id;
        mName = name;
        mAddress = address;
        mPhoneNumber = phoneNumber;
        mURL = URL;
        mLongitude = longitude;
        mLatitude = latitude;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getURL() {
        return mURL;
    }

    public void setURL(String URL) {
        mURL = URL;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public LatLng getCoordinates() {
        return new LatLng(mLatitude, mLongitude);
    }
}
