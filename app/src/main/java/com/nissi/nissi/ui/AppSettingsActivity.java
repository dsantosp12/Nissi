package com.nissi.nissi.ui;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nissi.nissi.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AppSettingsActivity extends AppCompatActivity {

    @Bind(R.id.backButtonMakerInfo) Button mBackButton;
    @Bind(R.id.mapRadioButton) RadioButton mMapRadioButton;
    @Bind(R.id.listRadioButton) RadioButton mListRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        ButterKnife.bind(this);

        SharedPreferences settings = getSharedPreferences(MainActivity.SETTINGS_NAME, 0);

        boolean isList = settings.getBoolean(MainActivity.LIST_OR_MAP, true);

        setRadioGroup(isList);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMapRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(MainActivity.SETTINGS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(MainActivity.LIST_OR_MAP, !isChecked);

                editor.commit();
            }
        });
    }

    private void setRadioGroup(boolean checked) {
        if (!checked) // If List is not check
            mMapRadioButton.setChecked(true);
    }
}
