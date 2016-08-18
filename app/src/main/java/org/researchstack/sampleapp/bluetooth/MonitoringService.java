package org.researchstack.sampleapp.bluetooth;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.researchstack.sampleapp.R;
import  org.researchstack.sampleapp.datamanager.BeaconStatus;
import  org.researchstack.sampleapp.datamanager.ConfirmationReceiver;
import  org.researchstack.sampleapp.datamanager.DBHelper;

public class MonitoringService extends Service implements BeaconConsumer, BootstrapNotifier {

    protected static final String TAG = "MonitoringService";
    private static final long MINIMUM_EPISODE_DURATION = 5L * 1000L; //Beacon episode needs to be 5 seconds to be registered
    private int notificationId = 0;
    private boolean scanFrequencyForBeaconIsPresent = false;
    private HashMap<String, Boolean> mBeaconInRange = new HashMap<String, Boolean>();
    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    @Override
    public void onCreate() {
        Log.d(TAG, "MonitoringService onCreate");
        super.onCreate();

        mBeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19")); //Modify for non Eddy Stone Beacons

        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        Region region = new Region("backgroundRegion", null, null, null);
        RegionBootstrap regionBootstrap = new RegionBootstrap(this, region);

        backgroundPowerSaver = new BackgroundPowerSaver(this);

        Log.d(TAG, "Checked Bluetooth");
        Intent intent = new Intent(this, MonitoringService.class);
        Log.d(TAG, "Created Monitoring Intent");
        startService(intent);
        Log.d(TAG, "Launched Monitoring Intent");

        mBeaconManager.setBackgroundScanPeriod(1100L); //scan for 1 seconds
        mBeaconManager.setBackgroundBetweenScanPeriod(0L); //no rest between scans
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            //create function to alert user
            Log.d(TAG, "Error changing scan frequency");
        }

        //Ranging Functions
        mBeaconManager.bind(this);
        Log.d(TAG, "Binding activity manager to app");

        //TEST add beacon statuses with random datetime
        //Remove later
        //for (int i=0;i<10;i++) {
        //    BeaconStatus status = generateRandomBeaconStatus(true);
        //    mDBHelper.addBeaconStatus(status);
        //}
    }

    //Ranging Functions
    @Override
    public void onDestroy() {
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

        DBHelper dBHelper = new DBHelper(this);

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
                            dBHelper.addBeaconStatus(beaconStatus);
                            //rangeStatusChanged = true;

                        } else if (b.getDistance() > 1.0 && isBeaconInRange(uid) == true) {
                            mBeaconInRange.put(uid, false);
                            BeaconStatus beaconStatus = new BeaconStatus(uid, mBeaconInRange.get(uid), dateTime, false); //Initially set userConfirmed to false
                            dBHelper.addBeaconStatus(beaconStatus);
                            //rangeStatusChanged = true;
                            try {
                                BeaconStatus beaconStatusPrevious = dBHelper.getBeaconStatusReverseCount(2); //Careful with count
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
            Log.d("MonitoringService: ", "Notification ID: " + String.valueOf(notificationId));
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

    public BeaconStatus generateRandomBeaconStatus(boolean inRange){
        long currentDateTime = System.currentTimeMillis();
        long lowerRange = System.currentTimeMillis()-30L*24L*60L*60L*1000L;

        Random r = new Random();
        long datetime =  + (long)(r.nextDouble()*(currentDateTime - lowerRange));

        return new BeaconStatus("testuid", inRange, datetime, true);
    }

    @Override
    public IBinder onBind(Intent intent) { //Required method for service
        return null;
    }

    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "did enter region.");

        mBeaconManager.setBackgroundScanPeriod(1100L); //scan for 1 seconds
        mBeaconManager.setBackgroundBetweenScanPeriod(60L*1000L); //rest 60 seconds between scans
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            //create function to alert user
            Log.d(TAG, "Error changing scan frequency");
        }

//        if (!haveDetectedBeaconsSinceBoot) {
//            Log.d(TAG, "auto launching MainActivity");
//            // The very first time since boot that we detect an beacon, we launch the
//            // MainActivity
//            //Intent intent = new Intent(this, MonitoringService.class);
//            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
//            // to keep multiple copies of this activity from getting created if the user has
//            // already manually launched the app.
//            //this.startActivity(intent);
//            haveDetectedBeaconsSinceBoot = true;
//
//        } else {
//            if (monitoringActivity != null) {
//                // If the Monitoring Activity is visible, we log info about the beacons we have
//                // seen on its display
//                //monitoringActivity.logToDisplay("I see a beacon again" );
//            } else {
//                // If we have already seen beacons before, but the monitoring activity is not in
//                // the foreground
//            }
//        }
    }

    @Override
    public void didExitRegion(Region region) {
        //monitoringActivity.logToDisplay("I no longer see a beacon.");
        setScanFrequencyForBeaconIsPresent(false);

        DBHelper dBHelper = new DBHelper(this);

        //Add "out of range" beacon status if missing
        //EX if the user leaves beacon signal range before app registers that user is outside the in range distance
        //TODO add notification prompt
        BeaconStatus beaconStatus = dBHelper.getBeaconStatusReverseCount(1);
        if (beaconStatus.isBeaconInRange()) {

            String uid = beaconStatus.getUID();
            Long dateTime = System.currentTimeMillis();
            BeaconStatus newBeaconStatus = new BeaconStatus(uid, false, dateTime, false);

            dBHelper.addBeaconStatus(newBeaconStatus);

            //rangeStatusChanged = true;
            try {
                BeaconStatus beaconStatusPrevious = dBHelper.getBeaconStatusReverseCount(2); //Careful with count
                if (beaconStatusPrevious.isBeaconInRange()) {
                    long startTime = beaconStatusPrevious.getDateTimeStamp();
                    long endTime = dateTime;
                    if (endTime - startTime > getMinimumBeaconEpisodeDuration()) { // Prevents triggering unwanted notifications from walking past beacon
                        sendNotification(String.valueOf(startTime), String.valueOf(endTime)); //TODO get notification user response and save beacon status based on response
                    }
                }
            } catch (NullPointerException e) { }
        }

        mBeaconManager.setBackgroundScanPeriod(5L*1100L); //scan for 5 seconds
        mBeaconManager.setBackgroundBetweenScanPeriod(5L*60L*1000L); //rest 5 minutes between scans
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            //create function to alert user
            Log.d(TAG, "Error changing scan frequency");
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
    }
}