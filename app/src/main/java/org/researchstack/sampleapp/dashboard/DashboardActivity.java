package org.researchstack.sampleapp.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.db.chart.view.LineChartView;
import org.researchstack.sampleapp.R;
import org.researchstack.sampleapp.databasemanager.BeaconStatus;
import org.researchstack.sampleapp.databasemanager.DBHelper;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by davis on 7/18/16.
 */
public class DashboardActivity extends Activity {
    private static final String TAG = "DashboardActivity";
    private static final long ONE_DAY_IN_MILLIS = 24L*60L*60L*1000L;
    private DBHelper mDBHelper = new DBHelper(this); //check for correct usage
    //private List<BeaconEpisode> beaconEpisodes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_beaconstatus);

        TwoValueDataHolder frequencyHolder = generateTimesPerDay();
        float[] dayOfThisMonth = (float[]) frequencyHolder.getFirst();
        String[] dayOfThisMonthString = new String[dayOfThisMonth.length];
        for (int i = 0; i < dayOfThisMonth.length; i++) {
            dayOfThisMonthString[i] = String.format("%s", (long) dayOfThisMonth[i]); //cast to long to remove unwanted decimal place
        }
        float[] episodesPerDay = (float[]) frequencyHolder.getSecond();

        int startDate = findBeaconStartDayOfMonth();
        int endDate = findCurrentDayOfMonth();
        Log.d(TAG, "Start day is " + String.valueOf(startDate));
        Log.d(TAG, "End day is " + String.valueOf(endDate));

        new LineChartHelper( (LineChartView)findViewById(R.id.linechart), this, dayOfThisMonthString, episodesPerDay, startDate, endDate); //update after creating post-process variables

        TwoValueDataHolder durationHolder = generateBeaconTotalDurationPerDay();
        float[] totalDurationPerDay = (float[]) frequencyHolder.getSecond();

        final String[] labelsSecond = {"Jan", "Feb", "Mar", "Apr", "Jun", "May", "Jul", "Aug", "Sep"};
        final float[] valuesSecond = {3f, 4f, 4f, 8f, 6f, 9f, 7f, 8f, 7f};

        new LineChartHelper( (LineChartView)findViewById(R.id.linechart2), this, dayOfThisMonthString, totalDurationPerDay, startDate, endDate); //update after creating post-process variables
    }

    public TwoValueDataHolder generateTimesPerDay() {
        //From the beaconStatus objects, calculate the following
        //Number of bathroom use for each date

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar currentDateTime = Calendar.getInstance(timeZone);
        Log.d(TAG, String.valueOf(currentDateTime.get(Calendar.MONTH)));
        int totalDaysInMonth = currentDateTime.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar startOfThisMonth = mDBHelper.generateStartOfCurrentMonth(currentDateTime); //Set calendar to start of current month
        long startOfThisMonthMillis = startOfThisMonth.getTimeInMillis();

        float[] episodesPerDay = new float[totalDaysInMonth];
        float[] dayOfThisMonth = new float[totalDaysInMonth];

        for (int i=0; i<totalDaysInMonth; i++) {
            dayOfThisMonth[i] = i+1;
            Log.d(TAG, "Date is " + String.valueOf(i+1));
            episodesPerDay[i] = mDBHelper.getFrequency(startOfThisMonthMillis);
            startOfThisMonthMillis += ONE_DAY_IN_MILLIS; //add 24 hours
        }

        return new TwoValueDataHolder(dayOfThisMonth, episodesPerDay);
    }

    public int findBeaconStartDayOfMonth(){
        /*
        //Run SQL DB Helper Command
        BeaconStatus firstStatus = mDBHelper.getBeaconStatus(1);
        long dateTimeMillis = firstStatus.getDateTimeStamp();
        */
        Calendar startOfThisMonth = Calendar.getInstance();
        long dateTimeMillis = mDBHelper.getFirstDayOfMonthWithBeaconStatus(startOfThisMonth);

        //Convert BeaconStatus datetime into a month integer
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTimeInMillis(dateTimeMillis);
        int day = dateTime.get(Calendar.DAY_OF_MONTH)-1; //Convert to zero-based index
        Log.d(TAG, "findBeaconStartDayOfMonth() - " + day);
        return day;
    }

    public int findCurrentDayOfMonth(){
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar dateTime = Calendar.getInstance(timeZone);
        return dateTime.get(Calendar.DAY_OF_MONTH);
    }

    public TwoValueDataHolder generateBeaconTotalDurationPerDay() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar currentDateTime = Calendar.getInstance(timeZone);
        Log.d(TAG, String.valueOf(currentDateTime.get(Calendar.MONTH)));
        int totalDaysInMonth = currentDateTime.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar startOfThisMonth = mDBHelper.generateStartOfCurrentMonth(currentDateTime); //Set calendar to start of current month
        long startOfThisMonthMillis = startOfThisMonth.getTimeInMillis();

        float[] totalDurationPerDay = new float[totalDaysInMonth];
        float[] dayOfThisMonth = new float[totalDaysInMonth];

        List<BeaconStatus> beaconStatuses = mDBHelper.getAllBeaconStatus();

        long dayOfMonthMillis = startOfThisMonthMillis;
        long nextDayOfMonthMillis = dayOfMonthMillis + ONE_DAY_IN_MILLIS;

        for (int day = 0; day < dayOfThisMonth.length; day++) { //Iterate over days of the month

            dayOfThisMonth[day] = day+1;

            for (int i = 0; i < beaconStatuses.size(); i++) {

                BeaconStatus firstStatus = beaconStatuses.get(i);

                if (firstStatus.getDateTimeStamp() > dayOfMonthMillis && firstStatus.getDateTimeStamp() < nextDayOfMonthMillis) {

                    if (firstStatus.isBeaconInRange()) {

                        BeaconStatus secondStatus = beaconStatuses.get(i + 1);

                        if (!secondStatus.isBeaconInRange()) {

                            float episodeDailyDuration = secondStatus.getDateTimeStamp() - firstStatus.getDateTimeStamp();
                            totalDurationPerDay[day] += episodeDailyDuration/1000L/60L; //convert milliseconds to minutes

                        } else {
                            Log.d(TAG, "Found beacon in range status without subsequent out of range status");
                            //TODO Quiet error stating that beacon leave range time was not recorded
                        }
                    }
                }
            }
            dayOfMonthMillis += ONE_DAY_IN_MILLIS;
            nextDayOfMonthMillis += ONE_DAY_IN_MILLIS;
        }
        return new TwoValueDataHolder(dayOfThisMonth, totalDurationPerDay);
    }
}