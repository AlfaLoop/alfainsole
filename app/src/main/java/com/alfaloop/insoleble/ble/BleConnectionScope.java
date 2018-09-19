package com.alfaloop.insoleble.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.alfaloop.insoleble.ble.listener.BleConnectionEventLisener;
import com.alfaloop.insoleble.ble.listener.CompleteCallback;
import com.alfaloop.insoleble.ble.listener.SensorRawCallback;
import com.alfaloop.insoleble.ble.config.BleConnConfig;

public class BleConnectionScope implements BleConnectionEventLisener {
    private static final String TAG = BleConnectionScope.class.getSimpleName();

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAIL = 1;

    private Handler handler = null;
    private int discoveyFailCount = 0;

    private String targetAddr = null;
    private CompleteCallback discoverProfileCompleteCallback = null;
    private CompleteCallback enableNotifyCompleteCallback = null;

    private BleConnectionOperator connection = null;

    public BleConnectionScope(Context context, Handler handler, BluetoothAdapter mBluetoothAdapter,
                              String addr, BleConnectionOperator connectionInstance) {
        this.targetAddr = addr;
        this.handler = handler;
        connection = connectionInstance;
        connection.registBleConnectionStatusLisener(this);
    }

    public void excute() {
        boolean result = connection.startup();
        if(result)
            Log.e(TAG, "Connecting.");
    }

    @Override
    public void onEvent(byte eventType) {
        switch (eventType) {
            case BleConnection.CONNECTION_CONNECTED:
                Log.e(TAG, "Connected.");
                discoveyFailCount = 0;
                connection.discoveryService();
                break;
            case BleConnection.CONNECTION_DISCONNECTED:
                Log.e(TAG, "Disconnected.");
                connection.startup();
                break;
            case BleConnection.CONNECTION_DISCOVERIED_SUCCESS:
                Log.e(TAG, "Service discovery success.");
                if(discoverProfileCompleteCallback != null)
                    discoverProfileCompleteCallback.onComplete(RESULT_SUCCESS);
                enableNotifications();
                break;
            case BleConnection.CONNECTION_DISCOVERIED_FAIL:
                Log.e(TAG, "Service discovery failed.");
                discoveyFailCount++;
                if(discoveyFailCount >= 3) {
                    if(discoverProfileCompleteCallback != null)
                        discoverProfileCompleteCallback.onComplete(RESULT_FAIL);
                } else {
                    connection.bleConnectionSleepSlot(BleConnConfig.REDISCOVERY_DELAY_MS);
                    connection.discoveryService();
                }
                break;
            case BleConnection.CONNECTION_NOTIFICATION_ENABLED:
                Log.e(TAG, "Notifications enabled.");
                if(enableNotifyCompleteCallback != null)
                    enableNotifyCompleteCallback.onComplete(BleConnectionScope.RESULT_SUCCESS);
                break;
            case BleConnection.CONNECTION_NOTIFICATION_NOT_ENABLED:
                Log.e(TAG, "Notifications enable fail.");
                break;
            case BleConnection.CONNECTION_WRITE_SUCCESS:
                Log.e(TAG, "Write success.");
                break;
            case BleConnection.CONNECTION_WRITE_FAIL:
                Log.e(TAG, "Write fail.");
                break;
            case BleConnection.CONNECTION_ACTION_SEGMENT:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        connection.keep();
                    }
                });
                break;
        }
    }

    public void registDiscoveryProfileCompleteCallback(CompleteCallback completeCallback) {
        this.discoverProfileCompleteCallback = completeCallback;
    }

    public void registEnabledNotifyCompleteCallback(CompleteCallback completeCallback) {
        this.enableNotifyCompleteCallback = completeCallback;
    }

    public void registSensorRawCallback(SensorRawCallback callback) {
        connection.registSensorRawCallBack(callback);
    }

    public void destroy() {
        connection.destroy();
    }

    public void startWithDelay(int ms) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connection.setupNotityControl();
            }
        }, ms);
    }

    private void enableNotifications() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                connection.setupNotifications();
            }
        });
    }

    public int getDeviceBatteryPower() {
        return connection.getBatteryPower();
    }
}
