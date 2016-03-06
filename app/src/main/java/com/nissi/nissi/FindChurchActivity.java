package com.nissi.nissi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindChurchActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private final String TAG = "FindChurchActivity";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mMyCoordinate;
    private List<Place> mPlaceList;

    @Bind(R.id.homeImageView) ImageView mHomeImageView;
    @Bind(R.id.settingsImageView) ImageView mSettingsImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_church);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleClient();


        mHomeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSettingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingIntent = new Intent(FindChurchActivity.this,
                        AppSettings.class);
                startActivity(settingIntent);
            }
        });
    }


    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {

            mMyCoordinate = new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude());

            if (isNetworkAvailable()) {
                    updateCameraPosition();

                    getChurchNearBy(mMyCoordinate);


            } else {
                alertUserAboutError("network");
            }


        } else {
            alertUserAboutError("location");
        }
    }

    private void updateCameraPosition() {
        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mMyCoordinate)
                .zoom(14.5f)
                .bearing(0)
                .tilt(25)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void buildGoogleClient() {
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }
    }

    private void getChurchNearBy(LatLng myCoordinate) {
        double longitude = myCoordinate.longitude, latitude = myCoordinate.latitude;

        OkHttpClient okHttpClient = new OkHttpClient();

        String query = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + latitude + "," + longitude + "&radius=500&types=church&key="
                + getResources().getString(R.string.google_maps_key);

        Log.v(TAG, query);

        Request request = new Request.Builder()
                .url(query)
                .build();

        okhttp3.Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                alertUserAboutError("server");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                try {
                    String jsonData = response.body().string();
                    if(response.isSuccessful()) {
                        mPlaceList = getListOfPlaces(jsonData);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(Place p : mPlaceList) {
                                    mMap.addMarker(new MarkerOptions()
                                        .position(p.getCoordinates())
                                        .title(p.getName())
                                    );
                                }
                            }
                        });

                    } else {
                        alertUserAboutError("sever");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Execption Caught: ",e);
                } catch (JSONException e) {
                    Log.e(TAG, "Execption Caught: ",e);
                }
            }
        });

    }

    private List<Place> getListOfPlaces(String jsonData) throws JSONException {
        List<Place> places = new ArrayList<>();
        JSONObject data = new JSONObject(jsonData);
        JSONArray results = data.getJSONArray("results");

        for(int i = 0; i < results.length(); i++ ) {
            JSONObject currentObject = results.getJSONObject(i);

            double currentLatitude = currentObject.getJSONObject("geometry")
                    .getJSONObject("location")
                    .getDouble("lat");
            double currentLongitude = currentObject.getJSONObject("geometry")
                    .getJSONObject("location")
                    .getDouble("lng");

            String currentTitle = currentObject.getString("name");

//            Log.v(TAG, "Lat: " + currentLatitude + " Lng: " + currentLongitude);

            places.add(new Place(
                    "ID",
                    currentTitle,
                    "ADDRESS",
                    "PHONENUMBER",
                    "URL",
                    currentLongitude,
                    currentLatitude
            ));
        }

        return places;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError(String dialog) {
        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        switch (dialog) {
            case "network":
                dialogFragment.setBody(getResources().getString(R.string.network_error_body));
                dialogFragment.setTitle(getResources().getString(R.string.network_error_title));
                break;
            case "location":
                dialogFragment.setBody(getResources().getString(R.string.location_error_body));
                dialogFragment.setTitle(getResources().getString(R.string.location_error_title));
                break;
            case "server":
                dialogFragment.setBody(getResources().getString(R.string.server_error_body));
                dialogFragment.setTitle(getResources().getString(R.string.server_error_title));
                break;
        }
        dialogFragment.show(getFragmentManager(), "error_dialog");
    }
}
