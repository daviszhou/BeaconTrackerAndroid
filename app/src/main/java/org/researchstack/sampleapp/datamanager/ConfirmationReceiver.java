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

        String startDateTime = intent.getStringExtra("startDateTime");
        String endDateTime = intent.getStringExtra("endDateTime");
        int notificationId = intent.getIntExtra("notificationId", 0);
        boolean userConfirmed = intent.getBooleanExtra("userConfirmed", false);
        Log.d("TAG", "The user pressed " + String.valueOf(userConfirmed) + " on notification " + notificationId);
        if (userConfirmed) {

            BeaconStatus startStatus = dbHelper.getBeaconStatusFromDateTime(Long.parseLong(startDateTime));
            startStatus.markAsUserConfirmed();
            dbHelper.updateBeaconStatus(startStatus);

            BeaconStatus endStatus = dbHelper.getBeaconStatusFromDateTime(Long.parseLong(endDateTime));
            endStatus.markAsUserConfirmed();
            dbHelper.updateBeaconStatus(endStatus);

        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

    }
}