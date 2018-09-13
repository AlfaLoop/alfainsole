package com.alfaloop.insoleble.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.alfaloop.insoleble.ble.support.device.NikePlus;

import java.util.List;

import static com.alfaloop.insoleble.ble.support.device.NikePlus.AUTH_CHALLENGE_CMD;
import static com.alfaloop.insoleble.ble.support.device.NikePlus.RATE_8_HZ;
import static com.alfaloop.insoleble.ble.support.device.NikePlus.SET_SAMPLING_RATE_CMD;
import static com.alfaloop.insoleble.ble.support.device.NikePlus.START_INIT_CMD_1;
import static com.alfaloop.insoleble.ble.support.device.NikePlus.START_INIT_CMD_2;
import static com.alfaloop.insoleble.ble.support.device.NikePlus.START_INIT_CMD_3;
import static com.alfaloop.insoleble.ble.support.device.NikePlus.START_INIT_CMD_4;
import static com.alfaloop.insoleble.ble.support.device.NikePlus.parsePower;

public class NikeBleConnection extends BleConnection {
    private static final String TAG = NikeBleConnection.class.getSimpleName();

    private BluetoothGattCharacteristic mNikeCharacteristic1 = null;
    private BluetoothGattCharacteristic mNikeCharacteristic2 = null;
    private BluetoothGattCharacteristic mNikeCharacteristic3 = null;
    private BluetoothGattCharacteristic mNikeCharacteristic4 = null;
    private BluetoothGattCharacteristic mNikeCharacteristic5 = null;
    private byte initState = 0;
    private byte[] challengeCmd = null;
    private int batterPower = 0;

    public NikeBleConnection(Context context, BluetoothAdapter mBluetoothAdapter, String addr) {
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

                if(characteristic.equals(mNikeCharacteristic2)) {
                    int power = 0;

                    if(initState == 1) {
                        Log.e(TAG, "Send challege command.");
                        challengeCmd = NikePlus.buileCommand(AUTH_CHALLENGE_CMD, data);
                        bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);
                        initState = 2;
                        callback.onEvent(CONNECTION_ACTION_SEGMENT);
                    }

                    if((power = parsePower(data)) != -1) {
                        //Log.e(TAG, Arrays.toString(data));
                        batterPower = power;
                        Log.e(TAG, String.format("Power %d", batterPower));
                    }
                } else if(characteristic.equals(mNikeCharacteristic4)) {
                    if(initState == 2) {
                        short seq = (short)((((data[1] & 0x0f) << 8) + (data[0] & 0xff)) & 0xfff);

                        short s1H = (short)(((data[12] << 4) + ((data[11] & 0xf0) >> 4)) & 0xfff);
                        s1H = NikePlus.overflowHandle(s1H);
                        short s1L = (short)(((data[3] << 4) + ((data[2] & 0xf0) >> 4) & 0xfff));
                        int s1 = (((int)s1H << 12) + s1L) & 0xffffff;

                        short s2H = (short)(((data[2] << 4) + ((data[1] & 0xf0) >> 4)) & 0xfff);
                        s2H = NikePlus.overflowHandle(s2H);
                        short s2L = (short)(((data[11] & 0x0f) << 8) + ((data[10] & 0xff) & 0xfff));
                        int s2 = (((int)s2H << 12) + s2L) & 0xffffff;

                        short s3H = (short)((((data[5] & 0x0f) << 8) + (data[4] & 0xff)) & 0xfff);
                        s3H = NikePlus.overflowHandle(s3H);
                        short s3L = (short)(((data[13] << 4) + ((data[12] & 0xf0) >> 4)) & 0xfff);
                        int s3 = (((int)s3H << 12) + s3L) & 0xffffff;

                        short s4H = (short)((((data[15] & 0x0f) << 8) + (data[14] & 0xff)) & 0xfff);
                        s4H = NikePlus.overflowHandle(s4H);
                        short s4L = (short)(((data[6] & 0x0f) << 8) + ((data[5] & 0xff) & 0xfff));
                        int s4 = (((int)s4H << 12) + s4L) & 0xffffff;

                        byte[] pressure = new byte[] {
                                (byte)(s1 & 0x0000ff), (byte)((s1 & 0x00ff00) >> 8), (byte)((s1 & 0xff0000) >> 16),
                                (byte)(s2 & 0x0000ff), (byte)((s2 & 0x00ff00) >> 8), (byte)((s2 & 0xff0000) >> 16),
                                (byte)(s3 & 0x0000ff), (byte)((s3 & 0x00ff00) >> 8), (byte)((s3 & 0xff0000) >> 16),
                                (byte)(s4 & 0x0000ff), (byte)((s4 & 0x00ff00) >> 8), (byte)((s4 & 0xff0000) >> 16),
                        };

                        byte[] accel = new byte[6];
                        accel[0] = (byte)(((data[16] & 0x0f) << 4) + ((data[15] & 0xf0) >> 4));
                        accel[1] = (byte)(((data[7] & 0x0f) << 4) + ((data[6] & 0xf0) >> 4));
                        accel[2] = (byte)(data[8] & 0xff);
                        accel[3] = (byte)(((data[17] & 0x0f) << 4) + ((data[16] & 0xf0) >> 4));
                        accel[4] = (byte)(data[18] & 0xff);
                        accel[5] = (byte)(data[9] & 0xff);

                        sensorRawCallback.onComplete(seq, pressure, accel, null, false);
                    }
                }
            }
        };
    }

    @Override
    public void setupNotityControl() {
        setCharacteristicNotification(mBluetoothGatt, mNikeCharacteristic4, true);
        bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);
    }

    @Override
    public void setupNotifications() {
        boolean result = false;
        result = setCharacteristicNotification(mBluetoothGatt, mNikeCharacteristic2, true);
        bleConnectionSleepSlot(NikePlus.DELAY_OF_ENABLED_NOTIFY_MS);
        bleConnectionSleepSlot(NikePlus.DELAY_OF_ENABLED_NOTIFY_MS);
        if(result)
            Log.e(TAG, "Notify enabled.");
        else
            Log.e(TAG, "Notify not enabled.");

        setCharacteristicValueNoRsp(mNikeCharacteristic1,
                NikePlus.buileCommand(START_INIT_CMD_1, null));
        //bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);
        Log.e(TAG, "Start challege..");

        initState = (byte)1;
    }

    @Override
    public void keep() {
        switch (initState) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                setCharacteristicValueNoRsp(mNikeCharacteristic1, challengeCmd);
                bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);
                setCharacteristicValueNoRsp(mNikeCharacteristic1,
                        NikePlus.buileCommand(START_INIT_CMD_2, null));
                bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);
                setCharacteristicValueNoRsp(mNikeCharacteristic1,
                        NikePlus.buileCommand(START_INIT_CMD_3, null));
                bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);
                setCharacteristicValueNoRsp(mNikeCharacteristic1,
                        NikePlus.buileCommand(START_INIT_CMD_4, null));
                bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);

                setCharacteristicValueNoRsp(mNikeCharacteristic1,
                        NikePlus.buileCommand(SET_SAMPLING_RATE_CMD,
                                new byte[] {RATE_8_HZ}));
                bleConnectionSleepSlot(NikePlus.DELAY_OF_WRITE_CMD_MS);

                callback.onEvent(CONNECTION_NOTIFICATION_ENABLED);

                initState = 2;
                break;
        }
    }

    @Override
    protected boolean fetchGattServices(List<BluetoothGattService> gattServices) {
        boolean result = true;
        for (BluetoothGattService gattService : gattServices) {
            for (BluetoothGattCharacteristic character : gattService.getCharacteristics()) {
                if (character.getUuid().equals(NikePlus.UUID_NIKE_MAIN_CHARACTER_1)) {
                    mNikeCharacteristic1 = character;
                } else if (character.getUuid().equals(NikePlus.UUID_NIKE_MAIN_CHARACTER_2)) {
                    mNikeCharacteristic2 = character;
                } else if (character.getUuid().equals(NikePlus.UUID_NIKE_MAIN_CHARACTER_3)) {
                    mNikeCharacteristic3 = character;
                } else if (character.getUuid().equals(NikePlus.UUID_NIKE_MAIN_CHARACTER_4)) {
                    mNikeCharacteristic4 = character;
                } else if (character.getUuid().equals(NikePlus.UUID_NIKE_MAIN_CHARACTER_5)) {
                    mNikeCharacteristic5 = character;
                }
            }
        }

        if(mNikeCharacteristic1 == null)
            result = false;
        if(mNikeCharacteristic2 == null)
            result = false;
        if(mNikeCharacteristic3 == null)
            result = false;
        if(mNikeCharacteristic4 == null)
            result = false;
        if(mNikeCharacteristic5 == null)
            result = false;

        return result;
    }

    @Override
    public int getBatteryPower() {
        return batterPower;
    }
}
