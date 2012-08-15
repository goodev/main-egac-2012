package com.google.androidcamp.unit3.excercise7;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReceiverA extends BroadcastReceiver {

    private static final String MSG_POWER_CONNECTED = "Power cable has been connected!";
    private static final String MSG_POWER_DISCONNECTED = "Power cable has been removed!";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the action string from the intent
        String action = intent.getAction();

        // Evaluate the action string and send up a toast message to the screen
        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, MSG_POWER_CONNECTED, Toast.LENGTH_SHORT).show();
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, MSG_POWER_DISCONNECTED, Toast.LENGTH_SHORT).show();
        }
    }
}
