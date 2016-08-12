package org.researchstack.sampleapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.support.multidex.MultiDex;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.researchstack.sampleapp.bluetooth.MonitoringActivity;
import org.researchstack.sampleapp.datamanager.BeaconStatus;
import org.researchstack.sampleapp.datamanager.DBHelper;
import org.researchstack.skin.PermissionRequestManager;
import org.researchstack.skin.ResearchStack;

public class SampleApplication extends Application implements BootstrapNotifier
{
    private static final String TAG = "BeaconReferenceApp";
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MonitoringActivity monitoringActivity = null;
    private DBHelper mDBHelper = new DBHelper(this);
    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    public static final String PERMISSION_NOTIFICATIONS = "SampleApp.permission.NOTIFICATIONS";

    @Override
    public void onCreate()
    {
        super.onCreate();

        //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
        // Init RS Singleton
        //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*

        ResearchStack.init(this, new SampleResearchStack());

        //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
        // Init permission objects
        //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*

        // If Build is M or >, add needed permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            PermissionRequestManager.PermissionRequest location = new PermissionRequestManager.PermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION,
                    R.drawable.rss_ic_location_24dp,
                    R.string.rss_permission_location_title,
                    R.string.rss_permission_location_desc);
            location.setIsBlockingPermission(true);
            location.setIsSystemPermission(true);

            PermissionRequestManager.getInstance().addPermission(location);
        }


        // We have some unique permissions that tie into Settings. You will need
        // to handle the UI for this permission along w/ storing the result.
        PermissionRequestManager.PermissionRequest notifications =
                new PermissionRequestManager.PermissionRequest(
                        PERMISSION_NOTIFICATIONS,
                        R.drawable.rss_ic_notification_24dp,
                        R.string.rss_permission_notification_title,
                        R.string.rss_permission_mandatory_notification_desc //Modified to state that notifications are important for core functionality
                );

        PermissionRequestManager.getInstance().addPermission(notifications);

        mBeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));

        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        Region region = new Region("backgroundRegion", null, null, null);
        RegionBootstrap regionBootstrap = new RegionBootstrap(this, region);

        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        // This is needed for android versions < 5.0 or you can extend MultiDexApplication
        super.attachBaseContext(base);
        MultiDex.install(this);
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

        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity");
            // The very first time since boot that we detect an beacon, we launch the
            // MainActivity
            //Intent intent = new Intent(this, MonitoringActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            //this.startActivity(intent);
            haveDetectedBeaconsSinceBoot = true;

        } else {
            if (monitoringActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
                //monitoringActivity.logToDisplay("I see a beacon again" );
            } else {
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if (monitoringActivity != null) {
            //monitoringActivity.logToDisplay("I no longer see a beacon.");
            monitoringActivity.setScanFrequencyForBeaconIsPresent(false);
        }

        //Add "out of range" beacon status if missing
        //EX if the user leaves beacon signal range before app registers that user is outside the in range distance
        //TODO add notification prompt
        BeaconStatus beaconStatus = mDBHelper.getBeaconStatusReverseCount(1);
        if (beaconStatus.isBeaconInRange()) {

            String uid = beaconStatus.getUID();
            Long dateTime = System.currentTimeMillis();
            BeaconStatus newBeaconStatus = new BeaconStatus(uid, false, dateTime, false);

            mDBHelper.addBeaconStatus(newBeaconStatus);

            //rangeStatusChanged = true;
            try {
                BeaconStatus beaconStatusPrevious = mDBHelper.getBeaconStatusReverseCount(2); //Careful with count
                if (beaconStatusPrevious.isBeaconInRange()) {
                    long startTime = beaconStatusPrevious.getDateTimeStamp();
                    long endTime = dateTime;
                    if (endTime - startTime > monitoringActivity.getMinimumBeaconEpisodeDuration()) { // Prevents triggering unwanted notifications from walking past beacon
                        monitoringActivity.sendNotification(String.valueOf(startTime), String.valueOf(endTime)); //TODO get notification user response and save beacon status based on response
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
        if (monitoringActivity != null) {
            //monitoringActivity.logToDisplay("I have just switched from seeing/not seeing beacons: " + state);
        }
    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }
}
