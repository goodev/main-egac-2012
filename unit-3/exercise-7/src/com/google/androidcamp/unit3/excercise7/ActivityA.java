package com.google.androidcamp.unit3.excercise7;

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

    public void startActivityB(View view) {
        Intent intent = new Intent(ActivityA.this, ActivityB.class);
        startActivity(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.i(TAG, NAME + " onSaveInstanceState");

        bundle.putString("foo", "bar");
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        Log.i(TAG, NAME + " onRestoreInstanceState");

        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            if (keys != null) {
                for (String key : keys) {
                    Log.i(TAG, NAME + " " + key + ", " + bundle.get(key));
                }
            }
        }

    }

}
