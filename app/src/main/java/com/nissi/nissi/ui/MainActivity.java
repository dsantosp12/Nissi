package com.nissi.nissi.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.nissi.nissi.R;
import com.nissi.nissi.tools.AlertDialogFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String LIST_OR_MAP = "LIST_OR_MAP";
    public static final String SETTINGS_NAME = "SETTINGS";
    boolean isList;

    @Bind(R.id.findChurchImageView) ImageView mFindChurchImageView;
    @Bind(R.id.appSettingImageView) ImageView mAppSettingImageView;
    @Bind(R.id.infoImageView) ImageView mInfoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        isList = settings.getBoolean(LIST_OR_MAP, false);

        mFindChurchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    vibrate();
                    runCorrectActivity();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            IntroActivity.FINE_LOCATION_PERMISSION);
                }

            }
        });

        mAppSettingImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                Intent appSettingIntent = new Intent(MainActivity.this, AppSettingsActivity.class);
                startActivity(appSettingIntent);
            }
        });

        mInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                Intent infoIntent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(infoIntent);
            }
        });

    }

    private void runCorrectActivity() {
        Intent findChurchIntent;
        if (isList) {
            findChurchIntent = new Intent(MainActivity.this, FindChurchListActivity.class);
        } else {
            findChurchIntent = new Intent(MainActivity.this, FindChurchActivity.class);
        }
        startActivity(findChurchIntent);
    }

    @Override
    public void onBackPressed() {

    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(10);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case IntroActivity.FINE_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runCorrectActivity();
                } else {
                    FindChurchActivity.alertUserAboutError("location", getResources(),
                            getFragmentManager());
                }

        }
    }
}
