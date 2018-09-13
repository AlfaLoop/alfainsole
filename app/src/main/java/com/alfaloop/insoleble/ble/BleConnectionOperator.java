package com.alfaloop.insoleble.ble;

import com.alfaloop.insoleble.ble.listener.BleConnectionEventLisener;
import com.alfaloop.insoleble.ble.listener.SensorRawCallback;

public interface BleConnectionOperator {
    boolean startup();
    boolean destroy();
    void discoveryService();
    void registBleConnectionStatusLisener(BleConnectionEventLisener callback);
    void registSensorRawCallBack(SensorRawCallback callback);
    void setupNotityControl();
    void setupNotifications();
    void bleConnectionSleepSlot(int timeInMs);
    void keep();
    int getBatteryPower();
}
