package org.researchstack.sampleapp.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.researchstack.sampleapp.R;
import org.researchstack.sampleapp.dashboard.DashboardActivity;
import org.researchstack.sampleapp.databasemanager.BeaconStatus;
import org.researchstack.sampleapp.databasemanager.ConfirmationReceiver;
import org.researchstack.sampleapp.databasemanager.DBHelper;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author dyoung
 * @author Matt Tyler
 */
//TODO Add check for beacon out of range status

public class MonitoringActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BeaconManager mBeaconManager = BeaconManager.getInstanceForApplication(this);
    private DBHelper mDBHelper = new DBHelper(this);
    private HashMap<String, Boolean> mBeaconInRange = new HashMap<String, Boolean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MonitoringActivity onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_monitoring);
        verifyBluetooth();
        logToDisplay("Application just launched");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }
        //Initiate Switch
        Switch mySwitch = (Switch) findViewById(R.id.mySwitch); //#set variable mySwitch to interface switch
        mySwitch.setChecked(true); //#set switch to start with "ON"
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ //#create a new change listener and attach to switch
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    mBeaconManager.setForegroundScanPeriod(1100L); //scan for 1 seconds
                    mBeaconManager.setForegroundBetweenScanPeriod(0L); //rest 0 seconds between scans
                    try {
                        mBeaconManager.updateScanPeriods();
                    } catch (RemoteException e) {
                        //create function to alert user
                        Log.d(TAG, "Error changing scan frequency");
                        logToDisplay("Error changing scan frequency");
                    }
                    logToDisplay("Set to frequent scanning");

                } else {
                    mBeaconManager.setForegroundScanPeriod(5L * 1100L); //scan for 5 seconds
                    mBeaconManager.setForegroundBetweenScanPeriod(60L * 1000L); //rest 10 seconds between scans
                    try {
                        mBeaconManager.updateScanPeriods();
                    } catch (RemoteException e) {
                        //create function to alert user
                        Log.d(TAG, "Error changing scan frequency");
                        logToDisplay("Error changing scan frequency");
                    }

                    logToDisplay("Set to infrequent scanning");
                }

            }
        });

        //Ranging Functions
        mBeaconManager.bind(this);

        //TEST add beacon statuses with random datetime
        //Remove laterqq
        for (int i=0;i<10;i++) {
            BeaconStatus status = generateRandomBeaconStatus();
            mDBHelper.addBeaconStatus(status);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle currentState) {
        super.onSaveInstanceState(currentState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BeaconTrackerApplication) this.getApplicationContext()).setMonitoringActivity(this);
        if (mBeaconManager.isBound(this)) mBeaconManager.setBackgroundMode(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconTrackerApplication) this.getApplicationContext()).setMonitoringActivity(null);
        if (mBeaconManager.isBound(this)) mBeaconManager.setBackgroundMode(true);
    }

    private void requestBluetooth(){
        Intent intentRequestBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //#create new intent that sends user-mediate bluetooth request
        int REQUEST_ENABLE_BT = 1; //#: If >= 0, this code will be returned in onActivityResult() when the activity exits
        startActivityForResult(intentRequestBluetooth, REQUEST_ENABLE_BT); //#launch activity with request to receive result back
    }

    public void onDisableBluetoothClicked(View view) {
        if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) { //#check that there is instance of Beacon Manager
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //#attaches variable to bluetooth adapter
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                //TODO add error handling + need to add permissions to function
            }
        }
    }

    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) { //#check that there is instance of Beacon Manager
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //#attaches variable to bluetooth adapter
                if (!bluetoothAdapter.isEnabled()) {
                    requestBluetooth(); //#if bluetooth is not enabled, launch requestBluetooth method
                }
            }
        } catch (RuntimeException e) { //#create alert for user if the phone does nto support bluetooth
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();
        }
    }

    public void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = (TextView)MonitoringActivity.this
                        .findViewById(R.id.monitoringText);
                if (textView.getMovementMethod() == null) textView.setMovementMethod(new ScrollingMovementMethod()); //allow scrolling of text window
                textView.append(line+"\n\n");
            }
        });
    }

	public void onDashboardClicked(View view) {
        if (!mDBHelper.checkIfEmpty()) {
            Intent intent = new Intent(this, DashboardActivity.class);
            this.startActivity(intent);
        } else {
            logToDisplay("No beacon detections stored. Cannot initialize dashboard.");
        }
    }

    //Ranging Functions
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size() > 0) {
                int i = 0;
                for (Beacon b : beacons) {
                    i += 1;
                    String uid = b.getId1().toString(); //look for alternatives to getServiceUuid()

                    if (i > mBeaconInRange.size()) { //new beacon is found
                        mBeaconInRange.put(uid, false); //put a new key-value pair in the hashmap
                    }

                    boolean rangeStatusChanged = false;
                    Date time = Calendar.getInstance().getTime();
                    logToDisplay("At " + time + " the beacon " + b.getId1().toString() + " is about " + b.getDistance() + " meters away.");

                    /*
                    String string = new String();
                    Log.d(TAG, "In Range is " + string.valueOf(mBeaconInRange.get(uid) ) );
                    */

                    if (b.getDistance() < 1.0 && isBeaconInRange(uid) == false) { //if phone is within half meters of beacon
                        mBeaconInRange.put(uid, true);
                        rangeStatusChanged = true;
                    } else if (b.getDistance() > 1.0 && isBeaconInRange(uid) == true) {
                        mBeaconInRange.put(uid, false);
                        rangeStatusChanged = true;
                    }

                    if (rangeStatusChanged == true) { //only store data if the range status has changed
                        Long dateTime = System.currentTimeMillis();
                        BeaconStatus beaconStatus = new BeaconStatus(uid, mBeaconInRange.get(uid), dateTime, false); //Initially set userConfirmed to false
                        sendNotification(String.valueOf(dateTime)); //TODO get notification user response and save beacon status based on response
                        mDBHelper.addBeaconStatus(beaconStatus);
                    }
                }
            }
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    public boolean isBeaconInRange(String uid){
        return mBeaconInRange.get(uid);
    }

    private void sendNotification(String beaconDateTime) {
        /*
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));
        PendingIntent yesPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        */

        //Process datetime
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(beaconDateTime));
        SimpleDateFormat format = new SimpleDateFormat("h:mm aa", Locale.US);
        String datetimestring = format.format(calendar.getTime());

        //Set ID that increments with each new notification
        SharedPreferences prefs = getSharedPreferences(Activity.class.getSimpleName(), Context.MODE_PRIVATE);
        int notificationId = prefs.getInt("notificationNumber", 0);

        //Intent that runs when Yes button is clicked
        Intent yesButtonIntent = new Intent(this, ConfirmationReceiver.class);
        yesButtonIntent.putExtra("beaconDateTime", beaconDateTime);
        yesButtonIntent.putExtra("userInputed", true);
        yesButtonIntent.putExtra("notificationId", notificationId);
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(this, 0, yesButtonIntent, PendingIntent.FLAG_ONE_SHOT);

        PendingIntent noPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, ConfirmationReceiver.class), PendingIntent.FLAG_ONE_SHOT);
        PendingIntent bodyPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT > 20) {
            NotificationCompat.Action yesAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_check_alt, "Yes", yesPendingIntent).build();
            NotificationCompat.Action noAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_x_alt, "No", noPendingIntent).build();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setContentTitle("Bathroom Tracker")
                            .setContentText("Were you pooping at " + datetimestring + "?")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setVibrate(new long[]{1000, 1000})
                            .addAction(yesAction)
                            .addAction(noAction);
            builder.setContentIntent(bodyPendingIntent);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, builder.build());

        } else {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setContentTitle("Bathroom Tracker")
                            .setContentText("Were you pooping at " + datetimestring + "?")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setVibrate(new long[]{1000, 1000})
                            .addAction(R.drawable.ic_check_alt, "Yes", yesPendingIntent)
                            .addAction(R.drawable.ic_x_alt, "No", noPendingIntent);
            builder.setContentIntent(bodyPendingIntent);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, builder.build());

        }

        //Increment and persist the notification id
        Log.d(TAG, "Notification ID is " + notificationId);
        SharedPreferences.Editor editor = prefs.edit();
        notificationId++;
        editor.putInt("notificationNumber", notificationId);
        editor.commit();
    }

    public BeaconStatus generateRandomBeaconStatus(){
        long currentDateTime = System.currentTimeMillis();
        long lowerRange = 1470000000000L;

        Random r = new Random();
        long datetime =  + (long)(r.nextDouble()*(currentDateTime - lowerRange));

        return new BeaconStatus("testuid", true, datetime, true);
    }
}