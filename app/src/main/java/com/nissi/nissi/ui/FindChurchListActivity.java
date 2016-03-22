package com.nissi.nissi.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.nissi.nissi.adapters.ChruchAdapter;
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

public class FindChurchListActivity extends ListActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks  {

    private final String RADIUS = "2000";
    private final String TYPES = "church";
    private final String TAG = "FindChurchListActivity";
    private Location mLastLocation;
    private LatLng mMyCoordinate;
    private List<Place> mPlaceList;
    private GoogleApiClient mGoogleApiClient;
    private ChruchAdapter mAdapter;
    private ListView mListView;
    @Bind(R.id.showMapButton) Button mShowMapButton;
    @Bind(R.id.homeListImageView) ImageView mHomeListImageView;
    @Bind(R.id.settingsListImageView) ImageView mSettingsListImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_church_list);
        ButterKnife.bind(this);
        mListView = getListView();
        buildGoogleClient();

        mShowMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindChurchListActivity.this,
                        FindChurchActivity.class);
                startActivity(intent);
            }
        });

        mHomeListImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindChurchListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mSettingsListImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindChurchListActivity.this, AppSettingsActivity.class);
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String placeID = (String) ((TextView) view.findViewById(R.id.itemPlaceIDTextView)).getText();
                Intent intent = new Intent(FindChurchListActivity.this, MakerInformationActivity.class);
                intent.putExtra("place_id", placeID);
                startActivity(intent);
            }
        });

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
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            mMyCoordinate = new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude());

            if (isNetworkAvailable()) {
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void getChurchNearBy(LatLng myCoordinate) {
        double longitude = myCoordinate.longitude, latitude = myCoordinate.latitude;
        String places_query =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                        + latitude + "," + longitude
                        + "&radius=" + RADIUS
                        + "&types=" + TYPES
                        +"&key=" + getResources().getString(R.string.google_maps_key);

        Log.v(TAG, places_query);

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
                                updateList(mPlaceList);
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

            places.add(
                new Place(
                    currentId,
                    currentTitle,
                    null,
                    null,
                    null,
                    mMyCoordinate,
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void updateList(List<Place> newList) {
        mAdapter = new ChruchAdapter(this, newList);
        setListAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
}
