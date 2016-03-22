package com.nissi.nissi.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nissi.nissi.tools.AlertDialogFragment;
import com.nissi.nissi.model.Place;
import com.nissi.nissi.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindChurchActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private final String RADIUS = "2000";
    private final String TYPES = "church";
    private final String TAG = "FindChurchActivity";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mMyCoordinate;
    private List<Place> mPlaceList;
    private String currentPlaceID;
    private Place currentMarkerClicked;

    @Bind(R.id.homeMapImageView) ImageView mHomeImageView;
    @Bind(R.id.settingsMapImageView) ImageView mSettingsImageView;
    @Bind(R.id.showListButton) Button mShowListButton;

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
                Intent intent = new Intent(FindChurchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mSettingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingIntent = new Intent(FindChurchActivity.this,
                        AppSettingsActivity.class);
                startActivity(settingIntent);
            }
        });

        mShowListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindChurchActivity.this,
                        FindChurchListActivity.class);
                startActivity(intent);
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(FindChurchActivity.this,
                        MakerInformationActivity.class);
                intent.putExtra("place_id", marker.getSnippet());
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            mMyCoordinate = new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude());

            if (isNetworkAvailable()) {
                    updateCameraPosition();
                    getChurchNearBy(mMyCoordinate);
            } else {
                FindChurchActivity.alertUserAboutError("network", getResources(),
                        getFragmentManager());
            }


        } else {
            FindChurchActivity.alertUserAboutError("location", getResources(),
                    getFragmentManager());
        }
    }

    private void updateCameraPosition() {
        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mMyCoordinate)
                .zoom(13.5f)
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
        String places_query =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + latitude + "," + longitude
                + "&radius=" + RADIUS
                + "&types=" + TYPES
                +"&key=" + getResources().getString(R.string.google_maps_key);

//        Log.v(TAG, places_query);

            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(places_query)
                    .build();
            okhttp3.Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    FindChurchActivity.alertUserAboutError("server", getResources(),
                            getFragmentManager());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (!jsonData.isEmpty()) {
                            mPlaceList = getListOfPlaces(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    putMarkers();
                                }
                            });

                        } else {
                            FindChurchActivity.alertUserAboutError("server",
                                    getResources(), getFragmentManager());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception Caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception Caught: ",e);
                    }
                }
            });
    }

    private void putMarkers() {
        for (Place p : mPlaceList) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(p.getCoordinates())
                    .title(p.getName())
                    .snippet(p.getId());
            mMap.addMarker(markerOptions);
        }
    }

    private List<Place> getListOfPlaces(String jsonData) throws JSONException {
        List<Place> places = new ArrayList<>();
        JSONObject data = new JSONObject(jsonData);
        JSONArray results = data.getJSONArray("results");

        for(int i = 0; i < results.length(); i++ ) {
            JSONObject currentObject = results.getJSONObject(i);

            String currentId = currentObject.getString("place_id");
            String currentTitle = currentObject.getString("name");
            String currentAddress = currentObject.getString("vicinity");
            double currentLatitude = currentObject.getJSONObject("geometry")
                    .getJSONObject("location")
                    .getDouble("lat");
            double currentLongitude = currentObject.getJSONObject("geometry")
                    .getJSONObject("location")
                    .getDouble("lng");

            places.add(new Place(
                    currentId,
                    currentTitle,
                    null,
                    null,
                    null,
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

    public static void alertUserAboutError(String dialog, Resources resources,
                                           FragmentManager fragmentManager) {
        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.setButton(resources.getString(R.string.ok_button));

        switch (dialog) {
            case "network":
                dialogFragment.setBody(resources.getString(R.string.network_error_body));
                dialogFragment.setTitle(resources.getString(R.string.network_error_title));
                break;
            case "location":
                dialogFragment.setBody(resources.getString(R.string.location_error_body));
                dialogFragment.setTitle(resources.getString(R.string.location_error_title));
                break;
            case "server":
                dialogFragment.setBody(resources.getString(R.string.server_error_body));
                dialogFragment.setTitle(resources.getString(R.string.server_error_title));
                break;
            case "phone":
                dialogFragment.setBody(resources.getString(R.string.phone_error_body));
                dialogFragment.setTitle(resources.getString(R.string.phone_error_title));
                break;

            default:
                dialogFragment.setBody(resources.getString(R.string.default_error_body));
                dialogFragment.setTitle(resources.getString(R.string.default_error_title));
                break;

        }
        dialogFragment.show(fragmentManager, "error_dialog");
    }
}
