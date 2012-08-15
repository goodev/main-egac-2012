package com.google.androidcamp.unit3.excercise5;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ActivityB extends Activity {

    private static final String TAG = "ANDROID_CAMP";
    private static final String NAME = "ActivityB";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, NAME + " created");
        setContentView(R.layout.activity_b);

        Button btnOk = (Button)findViewById((R.id.btn_ok));
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
              Intent result = new Intent();
              setResult(RESULT_OK, result);
              finish();
            }
        });

        Button btnCancel = (Button)findViewById((R.id.btn_cancel));
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent result = new Intent();
                setResult(RESULT_CANCELED, result);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onResume();
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
