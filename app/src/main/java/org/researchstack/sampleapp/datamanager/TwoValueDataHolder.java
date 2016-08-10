package org.researchstack.sampleapp.datamanager;

/**
 * Created by davis on 7/21/16.
 */
public class TwoValueDataHolder {
    private static Object mFirst;
    private static Object mSecond;

    TwoValueDataHolder(){}

    TwoValueDataHolder(Object first, Object second) {
        this.mFirst = first;
        this.mSecond = second;
    }

    public Object getFirst() {
        return this.mFirst;
    }

    public Object getSecond() {
        return this.mSecond;
    }
}