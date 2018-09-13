package com.alfaloop.insoleble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.alfaloop.insoleble.ble.support.device.Alfaone;

import java.util.List;

public class AlfaoneBleConnection extends BleConnection {
    private static final String TAG = AlfaoneBleConnection.class.getSimpleName();

    private BluetoothGattCharacteristic mAlfaAlfaOneFootPreCharacteristic = null;
    private BluetoothGattCharacteristic mAlfaAlfaOneAccelCharacteristic = null;
    private BluetoothGattCharacteristic mAlfaAlfaOneGyroCharacteristic = null;
    private BluetoothGattCharacteristic mAlfaAlfaOneNootifyControlCharacteristic = null;

    private byte[] colletedSeq = null;
    private byte colletedCount = 0;
    private byte[][] rawBytes = new byte[3][];

    public AlfaoneBleConnection(Context context, BluetoothAdapter mBluetoothAdapter, String addr) {
        super(context, mBluetoothAdapter, addr);

        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                connectionOnConnectionStateChange(gatt, status, newState);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                connectionOnServicesDiscovered(gatt, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                final byte[] data = characteristic.getValue();
                if(characteristic.equals(mAlfaAlfaOneFootPreCharacteristic)) {
                    rawBytesCollect(0, data[12], data);
                } else if(characteristic.equals(mAlfaAlfaOneAccelCharacteristic)) {
                    rawBytesCollect(1, data[16], data);
                } else if(characteristic.equals(mAlfaAlfaOneGyroCharacteristic)) {
                    rawBytesCollect(2, data[16], data);
                }
            }
        };
    }

    private void rawBytesCollect(int kind, byte seq, byte[] bytes) {
        if(colletedCount == 0) {
            colletedSeq = new byte[] {seq};
            rawBytes[kind] = bytes;
            colletedCount += 1;
        } else {
            if(colletedSeq[0] == seq) {
                rawBytes[kind] = bytes;
                colletedCount += 1;

                if(colletedCount == 3) {
                    sensorRawCallback.onComplete(seq, rawBytes[0], rawBytes[1], rawBytes[2], false);
                    colletedCount = 0;
                    rawBytes = new byte[3][];
                    colletedSeq = null;
                }
            } else {
                sensorRawCallback.onComplete(colletedSeq[0], rawBytes[0], rawBytes[1], rawBytes[2], true);
                colletedCount = 1;
                rawBytes = new byte[3][];
                rawBytes[kind] = bytes;
                colletedSeq = new byte[] {seq};
            }
        }
    }

    @Override
    protected boolean fetchGattServices(List<BluetoothGattService> gattServices) {
        boolean result = true;
        for (BluetoothGattService gattService : gattServices) {
            for (BluetoothGattCharacteristic character : gattService.getCharacteristics()) {
                if (character.getUuid().equals(
                        Alfaone.UUID_ALFA_ALFAONE_CHARACTER_FOOTPRE)) {
                    mAlfaAlfaOneFootPreCharacteristic = character;
                } else if (character.getUuid().equals(
                        Alfaone.UUID_ALFA_ALFAONE_CHARACTER_ACCEL)) {
                    mAlfaAlfaOneAccelCharacteristic = character;
                } else if (character.getUuid().equals(
                        Alfaone.UUID_ALFA_ALFAONE_CHARACTER_GYRO)) {
                    mAlfaAlfaOneGyroCharacteristic = character;
                } else if (character.getUuid().equals(
                        Alfaone.UUID_ALFA_ALFAONE_CHARACTER_NOTIFY_CONTROL)) {
                    mAlfaAlfaOneNootifyControlCharacteristic = character;
                }
            }
        }

        if(mAlfaAlfaOneFootPreCharacteristic == null)
            result = false;
        if(mAlfaAlfaOneAccelCharacteristic == null)
            result = false;
        if(mAlfaAlfaOneGyroCharacteristic == null)
            result = false;
        if(mAlfaAlfaOneNootifyControlCharacteristic == null)
            result = false;

        return result;
    }

    @Override
    public void setupNotifications() {
        boolean result;
        bleConnectionSleepSlot(Alfaone.DELAY_OF_ENABLED_NOTIFY_MS);
        result = setCharacteristicNotification(mBluetoothGatt, mAlfaAlfaOneFootPreCharacteristic, true);
        if(!result) {
            callback.onEvent(CONNECTION_NOTIFICATION_NOT_ENABLED);
            return;
        }
        bleConnectionSleepSlot(Alfaone.DELAY_OF_ENABLED_NOTIFY_MS);
        result = setCharacteristicNotification(mBluetoothGatt, mAlfaAlfaOneAccelCharacteristic, true);
        if(!result) {
            callback.onEvent(CONNECTION_NOTIFICATION_NOT_ENABLED);
            return;
        }
        bleConnectionSleepSlot(Alfaone.DELAY_OF_ENABLED_NOTIFY_MS);
        result = setCharacteristicNotification(mBluetoothGatt, mAlfaAlfaOneGyroCharacteristic, true);
        if(!result) {
            callback.onEvent(CONNECTION_NOTIFICATION_NOT_ENABLED);
            return;
        }
        bleConnectionSleepSlot(Alfaone.DELAY_OF_ENABLED_NOTIFY_MS);
        callback.onEvent(CONNECTION_NOTIFICATION_ENABLED);
    }

    @Override
    public void keep() {

    }

    public void setupNotityControl() {
        byte[] startCmd = Alfaone.buileNotifyControlCommand();
        boolean result = setCharacteristicValue(mBluetoothGatt, mAlfaAlfaOneNootifyControlCharacteristic, startCmd);
        if(result)
            callback.onEvent(CONNECTION_WRITE_SUCCESS);
        else
            callback.onEvent(CONNECTION_WRITE_FAIL);
    }
}
