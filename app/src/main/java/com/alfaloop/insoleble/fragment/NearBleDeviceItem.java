package com.alfaloop.insoleble.fragment;

public class NearBleDeviceItem {
    private String addrStr = null;
    private String deviceName = null;
    private String rssi = null;
    private boolean selected = false;

    public NearBleDeviceItem(String addrStr, String deviceName, String rssi, boolean selected) {
        setAddrStr(addrStr);
        setDeviceName(deviceName);
        setRSSI(rssi);
        setSelected(selected);
    }

    public String getAddrStr() {
        return addrStr;
    }

    public void setAddrStr(String addrStr) {
        this.addrStr = addrStr;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getRSSI() {
        return rssi;
    }

    public void setRSSI(String rssi) {
        this.rssi = rssi;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
