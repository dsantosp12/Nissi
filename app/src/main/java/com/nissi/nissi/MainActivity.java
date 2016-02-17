package com.nissi.nissi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.findChurchImageView) ImageView mFindChurchImageView;
    @Bind(R.id.appSettingImageView) ImageView mAppSettingImageView;
    @Bind(R.id.infoImageView) ImageView mInfoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFindChurchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findChurchIntent = new Intent(MainActivity.this, FindChurchActivity.class);
                startActivity(findChurchIntent);
            }
        });

        mAppSettingImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appSettingIntent = new Intent(MainActivity.this, AppSettings.class);
                startActivity(appSettingIntent);
            }
        });

        mInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent infoIntent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(infoIntent);
            }
        });
    }
}
