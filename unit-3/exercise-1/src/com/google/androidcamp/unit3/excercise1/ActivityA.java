package com.google.androidcamp.unit3.excercise1;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ActivityA extends Activity {

    private static final String TAG = "ANDROID_CAMP";
    private static final String NAME = "ActivityA";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i(TAG, NAME + " created");
        setContentView(R.layout.activity_a);
    }
}
