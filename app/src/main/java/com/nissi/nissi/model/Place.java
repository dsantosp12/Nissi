package com.nissi.nissi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Place {
    private String mId;
    private String mName;
    private String mAddress;
    private String mPhoneNumber;
    private String mURL;
    private Double mDistance;
    private LatLng mCurrentCoordinates;
    private Double mLongitude;
    private Double mLatitude;
    final double EARTHRADIUS = 3961;

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

    public Place(String id, String name, String address,
                 String phoneNumber, String URL,
                 LatLng currentCoordinates, Double longitude,
                 Double latitude) {
        mId = id;
        mName = name;
        mAddress = address;
        mPhoneNumber = phoneNumber;
        mURL = URL;
        mCurrentCoordinates = currentCoordinates;
        mLongitude = longitude;
        mLatitude = latitude;
        mDistance = coordinateToDistance();
    }

    public String getId() {
        return mId;
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

    public void setId(String id) {
        mId = id;
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double distance) {
        mDistance = distance;
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

    private Double coordinateToDistance() {
        Double dist = null;
        if (mLatitude != null && mLongitude != null) {
            double lat1 = Math.toRadians(mCurrentCoordinates.latitude);
            double lon1 = Math.toRadians(mCurrentCoordinates.longitude);
            double lat2 = Math.toRadians(mLatitude);
            double lon2 = Math.toRadians(mLongitude);

            double dLat = lat2 - lat1;
            double dLon = lon2 - lon1;

            double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2)
                    * Math.pow(Math.sin(dLon / 2), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            dist = c * EARTHRADIUS;
        }
        BigDecimal b = new BigDecimal(dist);
        return b.setScale(2, RoundingMode.DOWN).doubleValue();
    }

    @Override
    public String toString() {
        return getName();
    }

}
