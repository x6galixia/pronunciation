package com.example.pronunciationchecker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Simulate loading or API readiness (can replace with actual API initialization check)
        new Handler().postDelayed(this::initializeApp, SPLASH_TIME_OUT); // Delayed transition to main activity
    }

    private void initializeApp() {
        // Start the MainActivity after the loading process
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close this activity
    }
}