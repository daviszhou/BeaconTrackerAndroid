package org.researchstack.sampleapp.dashboard;

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

    public void setFirst(Object first) {
        this.mFirst = first;
    }

    public void setSecond(Object second) {
        this.mSecond = second;;
    }
}