package org.researchstack.sampleapp.datamanager;

/**
 * Created by davis on 7/8/16.
 */
public class BeaconStatus {

    private int mID;
    private String mUID;
    private boolean mBeaconInRange;
    private long mDateTime;
    private boolean mUserConfirmed;

    public BeaconStatus(){}

    public BeaconStatus(String uid, boolean beaconinrange, Long datetimestamp, boolean userconfirmed) {
        super();
        this.mUID = uid;
        this.mBeaconInRange = beaconinrange;
        this.mDateTime = datetimestamp;
        this.mUserConfirmed = userconfirmed;
    }

    @Override
    public String toString(){
        return "Beacon [id=" + mID + ". uid=" + mUID + ". beaconinrange=" + mBeaconInRange +
                ". datetimestamp=" + mDateTime+ ". userconfirmed = " + mUserConfirmed +"]";
    }

    public int getId(){
        return this.mID;
    }

    public String getUID(){
        return this.mUID;
    }


    public boolean isBeaconInRange(){
        return this.mBeaconInRange;
    }

    public int getIntIsBeaconInRange() {
        return (this.mBeaconInRange)? 1:0;
    }

    public boolean ismUserConfirmed() {
        return this.mUserConfirmed;
    }

    public int getIntUserConfirmed() {
        return (this.mUserConfirmed) ? 1:0;
    }

    public Long getDateTimeStamp(){
        return this.mDateTime;
    }

    public void setUID(String uid){
        this.mUID = uid;
    }

    public void setIntBeaconInRange(String aBeaconInRange) {
        int i = Integer.parseInt(aBeaconInRange);
        this.mBeaconInRange = (i == 1); // if i == 1, return true
    }

    public void setDateTimeStamp(Long datetimestamp){
        this.mDateTime = datetimestamp;
    }

    public void setId(int id){
        this.mID = id;
    }

    public void setIntUserConfirmed(String aUserConfirmed) { //Ask about best practice
        int i = Integer.parseInt(aUserConfirmed);
        this.mUserConfirmed = (i == 1); // if i == 1, return true
    }

    public void markAsUserConfirmed() { //Ask about best practice
        this.mUserConfirmed = true;
    }
}