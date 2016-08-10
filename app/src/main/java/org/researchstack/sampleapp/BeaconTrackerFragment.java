package org.researchstack.sampleapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.researchstack.sampleapp.bluetooth.MonitoringActivity;
import org.researchstack.sampleapp.datamanager.DBHelper;

/**
 * Created by davis on 8/3/16.
 */
public class BeaconTrackerFragment extends Fragment {
    private static final String TAG = "BeaconReferenceApp";
    private View emptyView;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MonitoringActivity monitoringActivity = null;
    private DBHelper mDBHelper;
    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.activity_monitoring, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        emptyView = view.findViewById(R.id.activity_monitoring_empty);

        initScanner(view);
    }

    private void initScanner(View view)
    {
        Context context = getActivity().getApplicationContext();

    }

    public void logToDisplay(final String line)
    {
        if (isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    TextView textView = (TextView) getActivity().findViewById(R.id.monitoringText);
                    if (textView.getMovementMethod() == null) textView.setMovementMethod(new ScrollingMovementMethod()); //allow scrolling of text window
                    textView.append(line+"\n\n");
                }
            });
        }
    }
}
