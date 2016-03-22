package com.nissi.nissi.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.nissi.nissi.R;
import com.nissi.nissi.tools.AlertDialogFragment;

public class IntroActivity extends Activity {

    final static int FINE_LOCATION_PERMISSION = 1;
    final static int PHONE_USAGE_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int locationPermissionCheck = ContextCompat.checkSelfPermission(IntroActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (locationPermissionCheck != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(IntroActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            IntroActivity.FINE_LOCATION_PERMISSION);

                } else {
                    Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case IntroActivity.FINE_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                    startActivity(intent);
                }

        }

    }
}
