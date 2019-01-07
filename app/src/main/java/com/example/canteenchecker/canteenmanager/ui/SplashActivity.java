package com.example.canteenchecker.canteenmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // fake procedure to show splash for 2 secs
                Intent intent = LoginActivity.createIntent(SplashActivity.this);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}