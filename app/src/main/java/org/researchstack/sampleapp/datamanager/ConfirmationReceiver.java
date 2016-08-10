package org.researchstack.sampleapp.datamanager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by davis on 7/29/16.
 */
public class ConfirmationReceiver extends BroadcastReceiver {
    protected static final String TAG = "ConfirmationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "received user input");

        DBHelper dbHelper = new DBHelper(context);

        String beaconDateTime = intent.getStringExtra("beaconDateTime");
        int notificationId = intent.getIntExtra("notificationId", 0);
        boolean openListFlag = intent.getBooleanExtra("userInputed", false);
        Log.d("TAG", "The user pressed " + String.valueOf(openListFlag));
        if (openListFlag) {
            BeaconStatus status = dbHelper.getBeaconStatusFromDateTime(Long.parseLong(beaconDateTime));
            status.markAsUserConfirmed();
            dbHelper.updateBeaconStatus(status);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}