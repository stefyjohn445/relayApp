package com.cristal.ble.comm;

import com.clj.fastble.data.BleDevice;

/**
 * Observed
 * When the Observable changes, it will notify the Observer to do the corresponding operation
 * An Observable can be observed by many Observers at the same time
 */
public interface Observable {

    void addObserver(Observer obj);
    void deleteObserver(Observer obj);
    void notifyObserver(BleDevice bleDevice);
}
