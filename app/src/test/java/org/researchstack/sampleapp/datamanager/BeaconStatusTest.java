package org.researchstack.sampleapp.datamanager;

import org.altbeacon.beacon.Beacon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
/**
 * Created by davis on 8/18/16.
 */
public class BeaconStatusTest {

    private BeaconStatus mStatus;
    private long mDateTime;

    @Before
    public void setUpBeacon() {
        mDateTime = System.currentTimeMillis();
        mStatus = new BeaconStatus("1234ABCD", true, mDateTime, false);
    }

    @Test
    public void testGetID() {
        assertTrue(mStatus.getUID().equals("1234ABCD"));
    }

    @Test
    public void testGetIntBeaconInRange() {
        assertTrue(mStatus.getIntIsBeaconInRange() == 1);
    }

    @Test
    public void testGetDateTime() {
        assertTrue(mStatus.getDateTimeStamp() == (mDateTime));
    }

    @Test
    public void testGetIntUserConfirmed() {
        assertTrue(mStatus.getIntUserConfirmed() == 0);
    }

}
