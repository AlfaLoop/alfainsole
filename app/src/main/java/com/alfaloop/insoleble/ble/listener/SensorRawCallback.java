package com.alfaloop.insoleble.ble.listener;

public interface SensorRawCallback {
    void onComplete(short seq, byte[] pressure, byte[] accesl, byte[] gyro, boolean wasLoss);
}
