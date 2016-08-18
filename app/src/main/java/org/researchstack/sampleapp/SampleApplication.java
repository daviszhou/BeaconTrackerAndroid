package org.researchstack.sampleapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.view.View;
import android.widget.Toast;

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

public class SampleApplication extends Application
{
    private static final String TAG = "SampleApp";
    private boolean haveDetectedBeaconsSinceBoot = false;
    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    public static final String PERMISSION_NOTIFICATIONS = "SampleApp.permission.NOTIFICATIONS";
    MonitoringActivity mService;
    boolean mBound = false;

    @Override
    public void onCreate()
    {
        Log.d(TAG, "Checked Bluetooth");
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

        Log.d(TAG, "Check Bluetooth");
        verifyBluetooth();

        Log.d(TAG, "Launch Beacon Monitoring Service");
        Intent intent = new Intent(this, MonitoringActivity.class);
        startService(intent);
    }

    //Bluetooth Function
    private void requestBluetooth(){
        Intent intentRequestBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //#create new intent that sends user-mediate bluetooth request
        startActivity(intentRequestBluetooth); //check usage
    }

    //Bluetooth Function
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
                public void onDismiss(DialogInterface dialog) {;
                    System.exit(0);
                }

            });
            builder.show();
        }
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        // This is needed for android versions < 5.0 or you can extend MultiDexApplication
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
