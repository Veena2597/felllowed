package com.example.felllowed;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AppInfoActivity extends NavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        //Calls navigation drawer activity
        onCreateDrawer();
    }
}
