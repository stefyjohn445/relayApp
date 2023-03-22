package com.cristal.ble.comm;

import com.clj.fastble.data.BleDevice;

/**
 * Observer
 * When the Observable changes, it will notify the Observer to do the corresponding operation
 * An Observable can be observed by many Observers at the same time
 */
public interface Observer {

    void disConnected(BleDevice bleDevice);
}
