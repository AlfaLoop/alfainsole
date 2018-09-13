package com.alfaloop.insoleble.ble;

import android.bluetooth.BluetoothDevice;

public class NearBluetoothDevice {
    private BluetoothDevice device = null;
    private int rssi = 0;

    public NearBluetoothDevice(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRSSI() {
        return rssi;
    }
}
