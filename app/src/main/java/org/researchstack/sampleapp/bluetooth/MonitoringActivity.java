package org.researchstack.sampleapp.bluetooth;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

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

import org.researchstack.sampleapp.R;
import org.researchstack.sampleapp.SampleApplication;
import  org.researchstack.sampleapp.datamanager.BeaconStatus;
import  org.researchstack.sampleapp.datamanager.ConfirmationReceiver;
import  org.researchstack.sampleapp.datamanager.DBHelper;

/**
 *
 * @author dyoung
 * @author Matt Tyler
 */
//TODO move interface/display functions to fragment object

public class MonitoringActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final long MINIMUM_EPISODE_DURATION = 5L*1000L; //Beacon episode needs to be 5 seconds to be registered
    private BeaconManager mBeaconManager = BeaconManager.getInstanceForApplication(this);
    private DBHelper mDBHelper = new DBHelper(this);
    private HashMap<String, Boolean> mBeaconInRange = new HashMap<String, Boolean>();
    private int notificationId = 0;
    private boolean scanFrequencyForBeaconIsPresent = false;
    private Fragment mMonitoringFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MonitoringActivity onCreate");
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_monitoring);
        verifyBluetooth();

        mMonitoringFragment = getFragmentManager().findFragmentById(R.id.monitoringFragment);
        //mMonitoringFragment.logToDisplay("Application just launched");

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
                        //logToDisplay("Error changing scan frequency");
                    }
                    //logToDisplay("Set to frequent scanning");
                    Log.d(TAG, "Set to frequent scanning");

                } else {
                    mBeaconManager.setForegroundScanPeriod(5L * 1100L); //scan for 5 seconds
                    mBeaconManager.setForegroundBetweenScanPeriod(60L * 1000L); //rest 10 seconds between scans
                    try {
                        mBeaconManager.updateScanPeriods();
                    } catch (RemoteException e) {
                        //create function to alert user
                        Log.d(TAG, "Error changing scan frequency");
                        //logToDisplay("Error changing scan frequency");
                    }

                    //logToDisplay("Set to infrequent scanning");
                    Log.d(TAG, "Set to frequent scanning");
                }
            }
        });

        //Ranging Functions
        mBeaconManager.bind(this);

        //TEST add beacon statuses with random datetime
        //Remove later
        //for (int i=0;i<10;i++) {
        //    BeaconStatus status = generateRandomBeaconStatus();
        //    mDBHelper.addBeaconStatus(status);
        //}

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
        ((SampleApplication) this.getApplicationContext()).setMonitoringActivity(this);
        if (mBeaconManager.isBound(this)) mBeaconManager.setBackgroundMode(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((SampleApplication) this.getApplicationContext()).setMonitoringActivity(null);
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

    @TargetApi(17)
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

    //TODO move this


    /*
    public void onDashboardClicked(View view) {
        if (!mDBHelper.checkIfEmpty()) {
            Intent intent = new Intent(this, DashboardActivity.class);
            this.startActivity(intent);
        } else {
            logToDisplay("No beacon detections stored. Cannot initialize dashboard.");
        }
    }
    */

    //Ranging Functions
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "Scanning is set to highest frequency: " + String.valueOf(scanFrequencyForBeaconIsPresent));
        if (!scanFrequencyForBeaconIsPresent) {
            mBeaconManager.setBackgroundScanPeriod(1100L); //scan for 1 seconds
            mBeaconManager.setBackgroundBetweenScanPeriod(0L); //no rest between scans
            try {
                mBeaconManager.updateScanPeriods();
            } catch (RemoteException e) {
                //create function to alert user
                Log.d(TAG, "Error changing scan frequency");
            }
            scanFrequencyForBeaconIsPresent = true;
        }

        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    int i = 0;
                    for (Beacon b : beacons) {
                        i += 1;
                        String uid = b.getId1().toString();

                        if (i > mBeaconInRange.size()) { //new beacon is found
                            mBeaconInRange.put(uid, false); //put a new key-value pair in the hashmap
                        }

                        //boolean rangeStatusChanged = false;
                        Date time = Calendar.getInstance().getTime();
                        //logToDisplay("At " + time + " the beacon " + b.getId1().toString() + " is about " + b.getDistance() + " meters away.");

                    /*
                    String string = new String();
                    Log.d(TAG, "In Range is " + string.valueOf(mBeaconInRange.get(uid) ) );
                    */
                        long dateTime = System.currentTimeMillis();

                        if (b.getDistance() < 1.0 && isBeaconInRange(uid) == false) { //if phone is within half meters of beacon
                            mBeaconInRange.put(uid, true);
                            BeaconStatus beaconStatus = new BeaconStatus(uid, mBeaconInRange.get(uid), dateTime, false); //Initially set userConfirmed to false
                            mDBHelper.addBeaconStatus(beaconStatus);
                            //rangeStatusChanged = true;

                        } else if (b.getDistance() > 1.0 && isBeaconInRange(uid) == true) {
                            mBeaconInRange.put(uid, false);
                            BeaconStatus beaconStatus = new BeaconStatus(uid, mBeaconInRange.get(uid), dateTime, false); //Initially set userConfirmed to false
                            mDBHelper.addBeaconStatus(beaconStatus);
                            //rangeStatusChanged = true;
                            try {
                                BeaconStatus beaconStatusPrevious = mDBHelper.getBeaconStatusReverseCount(2); //Careful with count
                                if (beaconStatusPrevious.isBeaconInRange()) {
                                    long startTime = beaconStatusPrevious.getDateTimeStamp();
                                    long endTime = dateTime;
                                    if (endTime - startTime > MINIMUM_EPISODE_DURATION) { // Prevents triggering unwanted notifications from walking past beacon
                                        sendNotification(String.valueOf(startTime), String.valueOf(endTime)); //TODO get notification user response and save beacon status based on response
                                    }
                                }
                            } catch (NullPointerException e) { }
                        }

                        //if (rangeStatusChanged) { //only store data if the range status has changed
                        //    BeaconStatus beaconStatus = new BeaconStatus(uid, mBeaconInRange.get(uid), dateTime, false); //Initially set userConfirmed to false
                        //    mDBHelper.addBeaconStatus(beaconStatus);
                        //}
                    }
                }
            }
        });

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    public int sendNotification(String startdatetime, String enddatetime) {

        //Process datetime
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("h:mm aa", Locale.US);

        calendar.setTimeInMillis(Long.parseLong(startdatetime));
        String startdatetimestring = format.format(calendar.getTime());

        calendar.setTimeInMillis(Long.parseLong(enddatetime));
        String enddatetimestring = format.format(calendar.getTime());

        //Intent that runs when "Yes" button is clicked
        Intent yesButtonIntent = new Intent(this, ConfirmationReceiver.class);
        yesButtonIntent.putExtra("startDateTime", startdatetime);
        yesButtonIntent.putExtra("endDateTime", enddatetime);
        yesButtonIntent.putExtra("userConfirmed", true);
        yesButtonIntent.putExtra("notificationId", notificationId);
        yesButtonIntent.setAction("yesButtonAction " + String.valueOf(notificationId)); //Generate unique action to prompt android to create new PendingIntent
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(this, 0, yesButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent that runs when "No" button is clicked
        Intent noButtonIntent = new Intent(this, ConfirmationReceiver.class);
        noButtonIntent.putExtra("userConfirmed", false);
        noButtonIntent.putExtra("notificationId", notificationId);
        noButtonIntent.setAction("noButtonAction " + String.valueOf(notificationId)); //Generate unique action to prompt android to create new PendingIntent
        PendingIntent noPendingIntent = PendingIntent.getBroadcast(this, 0, noButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent bodyPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT > 20) {
            NotificationCompat.Action yesAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_check_alt, "Yes", yesPendingIntent).build();
            NotificationCompat.Action noAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_x_alt, "No", noPendingIntent).build();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle("Bathroom Tracker")
                    .setContentText("Were you pooping " + startdatetimestring + " to " + enddatetimestring + "?")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setVibrate(new long[]{100, 200, 100, 200})
                    .addAction(yesAction)
                    .addAction(noAction);
            builder.setContentIntent(bodyPendingIntent);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, builder.build());

        } else {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setContentTitle("Bathroom Tracker")
                            .setContentText("Were you pooping " + startdatetimestring + " to " + enddatetimestring + "?")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setVibrate(new long[]{100, 200, 100, 200})
                            .addAction(R.drawable.ic_check_alt, "Yes", yesPendingIntent)
                            .addAction(R.drawable.ic_x_alt, "No", noPendingIntent);
            builder.setContentIntent(bodyPendingIntent);

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Log.d("MonitoringActivity: ", "Notification ID: " + String.valueOf(notificationId));
            notificationManager.notify(notificationId, builder.build());
        }

        //Increment and persist the notification id
        Log.d(TAG, "Notification ID is set to " + notificationId);
        return notificationId++;
    }

    public double getMinimumBeaconEpisodeDuration(){
        return this.MINIMUM_EPISODE_DURATION;
    }

    public boolean isBeaconInRange(String uid){
        return mBeaconInRange.get(uid);
    }

    public void setScanFrequencyForBeaconIsPresent(boolean scanfrequencyset) {
        this.scanFrequencyForBeaconIsPresent = scanfrequencyset;
    }

    public BeaconStatus generateRandomBeaconStatus(){
        long currentDateTime = System.currentTimeMillis();
        long lowerRange = 1470000000000L;

        Random r = new Random();
        long datetime =  + (long)(r.nextDouble()*(currentDateTime - lowerRange));

        return new BeaconStatus("testuid", true, datetime, true);
    }
}