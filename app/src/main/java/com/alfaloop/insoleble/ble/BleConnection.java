package com.alfaloop.insoleble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.alfaloop.insoleble.R;
import com.alfaloop.insoleble.ble.listener.BleConnectionEventLisener;
import com.alfaloop.insoleble.ble.listener.SensorRawCallback;

import java.util.List;
import java.util.UUID;

public abstract class BleConnection implements BleConnectionOperator {
    private static final String TAG = BleConnection.class.getSimpleName();

    public static final byte CONNECTION_CONNECTED = 0;
    public static final byte CONNECTION_DISCONNECTED = 1;
    public static final byte CONNECTION_DISCOVERIED_SUCCESS = 2;
    public static final byte CONNECTION_DISCOVERIED_FAIL = 3;
    public static final byte CONNECTION_NOTIFICATION_ENABLED = 4;
    public static final byte CONNECTION_NOTIFICATION_NOT_ENABLED = 5;
    public static final byte CONNECTION_WRITE_SUCCESS = 6;
    public static final byte CONNECTION_WRITE_FAIL = 7;
    public static final byte CONNECTION_ACTION_SEGMENT = 8;

    protected Context context = null;
    protected BluetoothAdapter mBluetoothAdapter = null;
    protected BluetoothGatt mBluetoothGatt = null;
    protected String targetAddr = null;
    protected BluetoothGattCallback mGattCallback = null;
    protected BleConnectionEventLisener callback = null;
    protected SensorRawCallback sensorRawCallback = null;

    public BleConnection(Context context, BluetoothAdapter mBluetoothAdapter, String addr) {
        this.targetAddr = addr;
        this.context = context;
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    public boolean startup() {
        if(mGattCallback != null) {
            BluetoothDevice bleDevice = mBluetoothAdapter.getRemoteDevice(targetAddr);
            mBluetoothGatt = bleDevice.connectGatt(context, false, mGattCallback);
            return true;
        } else {
            return false;
        }
    }

    public boolean destroy() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            return true;
        }

        return false;
    }

    public void discoveryService() {
        mBluetoothGatt.discoverServices();
    }

    public void registBleConnectionStatusLisener(BleConnectionEventLisener callback) {
        this.callback = callback;
    }

    public void registSensorRawCallBack(SensorRawCallback callback) {
        this.sensorRawCallback = callback;
    }

    public abstract void setupNotifications();

    protected abstract boolean fetchGattServices(List<BluetoothGattService> gattServices);

    public abstract void keep();

    public int getBatteryPower() {
        return 0;
    }

    public void bleConnectionSleepSlot(int timeInMs) {
        try {
            Thread.sleep(timeInMs);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void connectionOnConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            callback.onEvent(CONNECTION_CONNECTED);
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            callback.onEvent(CONNECTION_DISCONNECTED);
        }
    }

    protected void connectionOnServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            boolean result = fetchGattServices(mBluetoothGatt.getServices());
            if(result)
                callback.onEvent(CONNECTION_DISCOVERIED_SUCCESS);
            else
                callback.onEvent(CONNECTION_DISCOVERIED_FAIL);
        }
    }

    protected boolean setCharacteristicValue(BluetoothGatt bleGatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        if (bleGatt == null)
            return false;
        characteristic.setValue(value);
        return bleGatt.writeCharacteristic(characteristic);
    }

    protected boolean setCharacteristicNotification(BluetoothGatt bleGatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        boolean flag = false;
        if (bleGatt == null)
            return flag;

        if(bleGatt.setCharacteristicNotification(characteristic, enabled)) {
            BluetoothGattDescriptor descriptor =
                    characteristic.getDescriptor(
                            UUID.fromString(context.getString(R.string.descriptor_base_uuid)));
            if(enabled)
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            else
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bleGatt.writeDescriptor(descriptor);
            flag = true;
        }
        return flag;
    }

    public boolean setCharacteristicValueNoRsp(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
            return false;

        characteristic.setValue(value);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }
}
