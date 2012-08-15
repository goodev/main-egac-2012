package com.google.androidcamp.unit3.excercise2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Set;

public class ActivityA extends Activity {

    private static final String TAG = "ANDROID_CAMP";
    private static final String NAME = "ActivityA";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i(TAG, NAME + " created");
        setContentView(R.layout.activity_a);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, NAME + " started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, NAME + " resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, NAME + " paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, NAME + " stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, NAME + " destroyed");
    }
}
