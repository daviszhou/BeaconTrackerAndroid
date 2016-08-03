package org.researchstack.sampleapp.databasemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by davis on 7/8/16.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BeaconStatusDB";
    private static final long ONE_DAY_IN_MILLIS = 24L*60L*60L*1000L;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL commands to create table
        String CREATE_BEACON_TABLE = "CREATE TABLE beaconstatus ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uid TEXT, " +
                "beaconinrange INTEGER, " +
                "datetimestamp INTEGER, " +
                "userconfirmed INTEGER )";
        db.execSQL(CREATE_BEACON_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older existing older table
        db.execSQL("DROP TABLE IF EXISTS beaconstatus");
        // Create new table
        this.onCreate(db);
    }

    // Table name
    private static final String TABLE_BEACONSTATUS = "beaconstatus";
    // Table columns
    private static final String KEY_ID = "id";
    private static final String KEY_UID = "uid";
    private static final String KEY_BEACONINRANGE = "beaconinrange";
    private static final String KEY_DATETIMESTAMP = "datetimestamp";
    private static final String KEY_USERCONFIRMED = "userconfirmed";

    private static final String[] COLUMNS = {KEY_ID, KEY_UID, KEY_BEACONINRANGE, KEY_DATETIMESTAMP, KEY_USERCONFIRMED};

    public void addBeaconStatus(BeaconStatus beaconStatus){
        Log.d("DB: addBeaconStatus()", beaconStatus.toString());
        // Get reference to database
        SQLiteDatabase db = this.getWritableDatabase();
        // Create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_UID, beaconStatus.getUID());
        values.put(KEY_BEACONINRANGE, beaconStatus.getIntIsBeaconInRange());
        values.put(KEY_DATETIMESTAMP, beaconStatus.getDateTimeStamp());
        values.put(KEY_USERCONFIRMED, beaconStatus.getIntUserConfirmed()); //Default is false
        // Insert values
        db.insert(TABLE_BEACONSTATUS, null, values);
        // Close
        db.close();
    }

    public BeaconStatus getBeaconStatusFromDateTime(long datetime){
        // Get reference to database
        SQLiteDatabase db = this.getReadableDatabase();
        // Build query
        Cursor cursor = db.query(TABLE_BEACONSTATUS, //table
                COLUMNS, //column names
                " datetimestamp = ?", //seletions
                new String[] { String.valueOf(datetime) }, //selections
                null, //group by
                null, //having
                null, //order by
                null); //limit
        // Check that query yields results
        if (cursor != null)
            cursor.moveToFirst();

        // Build object
        BeaconStatus beaconStatus = new BeaconStatus();
        beaconStatus.setId(cursor.getInt(0));
        beaconStatus.setUID(cursor.getString(1));
        beaconStatus.setIntBeaconInRange(cursor.getString(2));
        beaconStatus.setDateTimeStamp(cursor.getLong(3));
        // Log
        Log.d("DB: getBeaconStatus("+datetime+")", beaconStatus.toString());
        cursor.close();

        return beaconStatus;
    }

    public BeaconStatus getBeaconStatus(int id){
        // Get reference to database
        SQLiteDatabase db = this.getReadableDatabase();
        // Build query
        Cursor cursor = db.query(TABLE_BEACONSTATUS, //table
                COLUMNS, //column names
                " id = ?", //seletions
                new String[] { String.valueOf(id) }, //selections
                null, //group by
                null, //having
                null, //order by
                null); //limit
        // Check that query yields results
        if (cursor != null)
            cursor.moveToFirst();

        // Build object
        BeaconStatus beaconStatus = new BeaconStatus();
        beaconStatus.setId(cursor.getInt(0));
        beaconStatus.setUID(cursor.getString(1));
        beaconStatus.setIntBeaconInRange(cursor.getString(2));
        beaconStatus.setDateTimeStamp(cursor.getLong(3));
        // Log
        Log.d("DB: getBeaconStatus("+id+")", beaconStatus.toString());
        cursor.close();

        return beaconStatus;
    }

    public List<BeaconStatus> getAllBeaconStatus(){
        List<BeaconStatus> beaconStatuses = new LinkedList<BeaconStatus>();

        // Build query
        String query = "SELECT * FROM " + TABLE_BEACONSTATUS;
        // Get reference to database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        // Add each row into elements of beaconStatus and late beaconStatus to list
        BeaconStatus beaconStatus = null;
        if (cursor.moveToFirst()) {
            do {
                beaconStatus = new BeaconStatus();
                beaconStatus.setId(cursor.getInt(0));
                beaconStatus.setUID(cursor.getString(1));
                beaconStatus.setIntBeaconInRange(cursor.getString(2));
                beaconStatus.setDateTimeStamp(cursor.getLong(3));
                beaconStatuses.add(beaconStatus);
            } while (cursor.moveToNext());
        }
        try {
            Log.d("DB: getAllBeacons()", beaconStatus.toString());
        } catch (NullPointerException e) {
            Log.d("DB: getAllBeacons()", "No Logged Beacon Statuses");
        }
        cursor.close();

        return beaconStatuses;
    }


    public int updateBeaconStatus(BeaconStatus beaconStatus){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, beaconStatus.getUID());
        values.put(KEY_BEACONINRANGE, beaconStatus.getIntIsBeaconInRange());
        values.put(KEY_DATETIMESTAMP, beaconStatus.getDateTimeStamp());
        values.put(KEY_USERCONFIRMED, beaconStatus.getIntUserConfirmed());

        int i = db.update(TABLE_BEACONSTATUS,
                values,
                KEY_ID+" = ?",
                new String[] { String.valueOf(beaconStatus.getId()) });

        db.close();
        Log.d("DB: updateBeaconStatus", beaconStatus.toString());

        return i;
    }

    public void deleteBeaconStatus(BeaconStatus beaconStatus){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BEACONSTATUS,
                KEY_ID+" = ?",
                new String[] { String.valueOf(beaconStatus.getId()) });
        db.close();
    }

    public boolean checkIfEmpty(){
        boolean isEmpty = true;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BEACONSTATUS, null);
        // Check that query yields results
        if (cursor != null) {
            isEmpty = !cursor.moveToFirst();
            cursor.close();
        }
        return isEmpty;
    }

    public int getFrequency(long dateinmillis){
        long beginTime = dateinmillis;
        long endTime = dateinmillis + ONE_DAY_IN_MILLIS;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_BEACONSTATUS +
                " WHERE " + KEY_DATETIMESTAMP + " >= " + String.valueOf(beginTime) +
                " AND " + KEY_DATETIMESTAMP + " < " + String.valueOf(endTime) +
                " AND " + KEY_BEACONINRANGE + " = 1";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        db.close();
        cursor.close();

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(dateinmillis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        Log.d("DB FUNCTION", String.valueOf(beginTime));
        Log.d("DB FUNCTION", "DB pull date is " + String.valueOf(day));
        Log.d("DB FUNCTION", "DB pull month is " + String.valueOf(month));
        Log.d("DB FUNCTION", String.valueOf(count));

        return count;
    }

    public int getConfirmedFrequency(long dateinmillis){
        long beginTime = dateinmillis;
        long endTime = dateinmillis + ONE_DAY_IN_MILLIS;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_BEACONSTATUS +
                " WHERE " + KEY_DATETIMESTAMP + " >= " + String.valueOf(beginTime) +
                " AND " + KEY_DATETIMESTAMP + " < " + String.valueOf(endTime) +
                " AND " + KEY_BEACONINRANGE + " = 1" +
                " AND " + KEY_USERCONFIRMED + " = 1";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        db.close();
        cursor.close();

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(dateinmillis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        Log.d("DB FUNCTION", String.valueOf(beginTime));
        Log.d("DB FUNCTION", "DB pull date is " + String.valueOf(day));
        Log.d("DB FUNCTION", "DB pull month is " + String.valueOf(month));
        Log.d("DB FUNCTION", String.valueOf(count));

        return count;
    }

    public long getFirstDayOfMonthWithBeaconStatus(Calendar currentdatetime) {
        Calendar startOfMonth = generateStartOfCurrentMonth(currentdatetime);
        long currentdatetimemillis = currentdatetime.getTimeInMillis();

        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 1);
        startOfMonth.set(Calendar.MINUTE, 1);
        startOfMonth.set(Calendar.SECOND, 1);
        long startOfThisMonthMillis = startOfMonth.getTimeInMillis();

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_BEACONSTATUS +
                " WHERE " + KEY_DATETIMESTAMP + " > " + String.valueOf(startOfThisMonthMillis) +
                " AND " + KEY_BEACONINRANGE + " = 1";
        Cursor cursor = db.rawQuery(query, null);

        long firstdayofmonthmillis;
        if (cursor.moveToFirst()) {
            firstdayofmonthmillis = cursor.getLong(3);
            Log.d("DB HELPER", "First Day is " + String.valueOf(firstdayofmonthmillis));
        } else {
            firstdayofmonthmillis = currentdatetimemillis; //Start plot at today's day of month if no beacon statuses exist
        }
        cursor.close();

        return firstdayofmonthmillis;
    }

    public Calendar generateStartOfCurrentMonth(Calendar currentDateTime) {
        currentDateTime.set(Calendar.DAY_OF_MONTH, 1);
        currentDateTime.set(Calendar.HOUR_OF_DAY, 1);
        currentDateTime.set(Calendar.MINUTE, 1);
        currentDateTime.set(Calendar.SECOND, 1);
        return currentDateTime;
    }

        /*
    public List<BeaconStatus> getBeaconStatusWithinDay(long dateinmillis) {
        long beginTime = dateinmillis;
        long endTime = dateinmillis + ONE_DAY_IN_MILLIS;
        List<BeaconStatus> beaconStatuses = new LinkedList<BeaconStatus>();

        // Build query
        String query = "SELECT * FROM " + TABLE_BEACONSTATUS +
                " WHERE " + KEY_DATETIMESTAMP + " >= " + String.valueOf(beginTime) +
                " AND " + KEY_DATETIMESTAMP + " < " + String.valueOf(endTime) +
                " AND " + KEY_USERCONFIRMED + " = 1";
        // Get reference to database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        // Add each row into elements of beaconStatus and late beaconStatus to list
        BeaconStatus beaconStatus = null;
        if (cursor.moveToFirst()) {
            do {
                beaconStatus = new BeaconStatus();
                beaconStatus.setId(cursor.getInt(0));
                beaconStatus.setUID(cursor.getString(1));
                beaconStatus.setIntBeaconInRange(cursor.getString(2));
                beaconStatus.setDateTimeStamp(cursor.getLong(3));
                beaconStatuses.add(beaconStatus);
            } while (cursor.moveToNext());
        }
        try {
            Log.d("DB: getAllBeacons()", beaconStatus.toString());
        } catch (NullPointerException e) {
            Log.d("DB: getAllBeacons()", "No Logged Beacon Statuses");
        }
        cursor.close();

        return beaconStatuses;
    }
    */
}