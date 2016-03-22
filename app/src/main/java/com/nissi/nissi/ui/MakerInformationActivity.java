package com.nissi.nissi.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.nissi.nissi.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MakerInformationActivity extends AppCompatActivity {

    private final String TAG = "MakerInfoActivity";
    private String mCurrentPlaceID;
    private String mJSONData;
    private String mPhoneNumber;
    private String mWebsite;
    private LatLng mCoordinates;
    private boolean isWebsite;
    private boolean isPhoneNumber;
    @Bind(R.id.churchNameTextView) TextView mChurchNameTextView;
    @Bind(R.id.addressTextView) TextView mAddressTextView;
    @Bind(R.id.websiteButtonMakerInfo) Button mWebSiteButton;
    @Bind(R.id.backButtonMakerInfo) Button mBackButton;
    @Bind(R.id.callButtonMakerInfo) Button mCallButton;
    @Bind(R.id.directionsButtonMakerInfo) Button mDirectionsButton;
    @Bind(R.id.markerInfoProgressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maker_information);
        ButterKnife.bind(this);

        mCallButton.setVisibility(View.INVISIBLE);
        mWebSiteButton.setVisibility(View.INVISIBLE);

        mProgressBar.setVisibility(View.VISIBLE);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MakerInformationActivity.this,
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                } else {
                    ActivityCompat.requestPermissions(MakerInformationActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            IntroActivity.PHONE_USAGE_PERMISSION);
                }
            }
        });

        mDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri googleMapUri = Uri.parse("http://maps.google.com/maps?daddr="
                        + mCoordinates.latitude + ","
                        + mCoordinates.longitude
                        + "(" + mChurchNameTextView.getText() + ")"
                );
                Intent directionsIntent = new Intent(Intent.ACTION_VIEW, googleMapUri);
                directionsIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(directionsIntent);

            }
        });

        mWebSiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(mWebsite);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }
        });

        mCurrentPlaceID = getIntent().getStringExtra("place_id");
        try {
            getPlaceDetails();
        } catch (JSONException e) {
            Log.e(TAG, "Exception Caught: ", e);
            FindChurchActivity.alertUserAboutError("server", getResources(),
                    getFragmentManager());
        }
    }

    private void makeCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + mPhoneNumber));
        startActivity(callIntent);
    }

    private void getPlaceDetails() throws JSONException {
        JSONObject results;
        String queryDetails = "https://maps.googleapis.com/maps/api/place/details/json?"
                + "placeid=" + this.mCurrentPlaceID +
                "&key=" + getResources().getString(R.string.google_maps_key);
        Log.v(TAG, queryDetails);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(queryDetails)
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
                mJSONData = response.body().string();
                if (!mJSONData.isEmpty()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLoading();
                            updateDisplay();

                            if (isWebsite) mWebSiteButton.setVisibility(View.VISIBLE);
                            if (isPhoneNumber) mCallButton.setVisibility(View.VISIBLE);

                        }
                    });

                } else {
                    FindChurchActivity.alertUserAboutError("server", getResources(),
                            getFragmentManager());
                }
            }
        });
    }

    void updateLoading() {
        if(mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }else {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    void updateDisplay() {
        try{
            JSONObject data = new JSONObject(mJSONData);
            JSONObject results = data.getJSONObject("result");
            mChurchNameTextView.setText(results.getString("name"));
            mAddressTextView.setText(results.getString("formatted_address"));
            if (results.isNull("formatted_phone_number")) {
                isPhoneNumber = false;
            } else {
                mPhoneNumber = results.getString("formatted_phone_number");
                isPhoneNumber = true;
            }
            if (results.isNull("website")) {
                isWebsite = false;
            } else {
                mWebsite = results.getString("website");
                isWebsite = true;
            }
            mCoordinates = new LatLng(
                    results.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                    results.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                );
        } catch (JSONException e) {
            Log.e(TAG, "Exception Caught: ", e);
            FindChurchActivity.alertUserAboutError("", getResources(),
                    getFragmentManager());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case IntroActivity.PHONE_USAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                } else {
                    FindChurchActivity.alertUserAboutError("phone", getResources(),
                            getFragmentManager());
                }

        }

    }
}
