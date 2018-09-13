package com.alfaloop.insoleble.ble.support.device;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class Alfaone {
    private static final String TAG = Alfaone.class.getSimpleName();

    public static final byte DEVICE_TYPE = (byte)1;
    public static final short DELAY_OF_DISCOVERIED_MS = 1500;
    public static final short DELAY_OF_ENABLED_NOTIFY_MS = 100;

    public static final UUID UUID_ALFA_ALFAONE_SERVICE = UUID.fromString("79430001-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_FOOTPRE = UUID.fromString("79430002-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_ACCEL = UUID.fromString("79430003-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_GYRO = UUID.fromString("79430004-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_COMPASS = UUID.fromString("79430005-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_QUAT = UUID.fromString("79430006-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_EULER = UUID.fromString("79430007-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_LINEARACCEL = UUID.fromString("79430008-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_GRAVITY = UUID.fromString("79430009-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_HEADING = UUID.fromString("7943000a-f6c2-09a3-e9f9-128abca31297");
    public static final UUID UUID_ALFA_ALFAONE_CHARACTER_NOTIFY_CONTROL = UUID.fromString("7943000b-f6c2-09a3-e9f9-128abca31297");

    public static final String FIELD_TITLE_RAW =
            "seq,pressure 1,pressure 2,pressure 3,pressure 4,accel X,accel Y, accel Z,gyro X,gyro Y,gyro Z";

    private static byte lastSide = (byte)-1;
    private static short tmpSeq = 0;
    private static int[] tmpPressure = null;
    private static float[] tmpAccel = null;
    private static float[] tmpGyro = null;
    private static boolean isConflict = false;
    private static byte sideBk = (byte)-1;
    private static short tmpSeqBk = 0;
    private static int[] tmpPressureBk = null;
    private static float[] tmpAccelBk = null;
    private static float[] tmpGyroBk = null;

    private static float PRESSURE_MAX_VALUE = 1024f;
    private static final float PRESSURE_MIN_VALUE = 0;

    public static int[] parsePressure(byte[] raw) {
        if(raw == null)
            return  null;

        int heel = ByteBuffer.wrap(new byte[]{raw[0], raw[1]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort();
        int outerBall = ByteBuffer.wrap(new byte[]{raw[2], raw[3]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort();
        int innerBall = ByteBuffer.wrap(new byte[]{raw[4], raw[5]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort();
        int thumb = ByteBuffer.wrap(new byte[]{raw[6], raw[7]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort();

        if(thumb > PRESSURE_MAX_VALUE)
            PRESSURE_MAX_VALUE = thumb;
        if(innerBall > PRESSURE_MAX_VALUE)
            PRESSURE_MAX_VALUE = innerBall;
        if(outerBall > PRESSURE_MAX_VALUE)
            PRESSURE_MAX_VALUE = outerBall;
        if(heel > PRESSURE_MAX_VALUE)
            PRESSURE_MAX_VALUE = heel;

        thumb = (int)Math.ceil(((float)thumb - PRESSURE_MIN_VALUE) / (PRESSURE_MAX_VALUE - PRESSURE_MIN_VALUE) * 256.f);
        innerBall = (int)Math.ceil(((float)innerBall - PRESSURE_MIN_VALUE) / (PRESSURE_MAX_VALUE - PRESSURE_MIN_VALUE) * 256.f);
        outerBall = (int)Math.ceil(((float)outerBall - PRESSURE_MIN_VALUE) / (PRESSURE_MAX_VALUE - PRESSURE_MIN_VALUE) * 256.f);
        heel = (int)Math.ceil(((float)heel - PRESSURE_MIN_VALUE) / (PRESSURE_MAX_VALUE - PRESSURE_MIN_VALUE) * 256.f);

        int[] result = new int[] {thumb, outerBall, innerBall, heel};
        return result;
    }

    public static float[] parseAccel(byte[] raw) {
        if(raw == null)
            return  null;

        float accX = (float)ByteBuffer.wrap(new byte[]{raw[0], raw[1], raw[2], raw[3]})
                .order(ByteOrder.LITTLE_ENDIAN).getInt() / 65536f;
        float accY = (float)ByteBuffer.wrap(new byte[]{raw[4], raw[5], raw[6], raw[7]})
                .order(ByteOrder.LITTLE_ENDIAN).getInt() / 65536f;
        float accZ = (float)ByteBuffer.wrap(new byte[]{raw[8], raw[9], raw[10], raw[11]})
                .order(ByteOrder.LITTLE_ENDIAN).getInt() / 65536f;
        float[] result = new float[] {accX, accY, accZ};
        return result;
    }

    public static float[] parseGyro(byte[] raw) {
        if(raw == null)
            return  null;

        float gyroX = ByteBuffer.wrap(new byte[]{raw[0], raw[1], raw[2], raw[3]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort() / 65536f;
        float gyroY = ByteBuffer.wrap(new byte[]{raw[4], raw[5], raw[6], raw[7]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort() / 65536f;
        float gyroZ = ByteBuffer.wrap(new byte[]{raw[8], raw[9], raw[10], raw[11]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort() / 65536f;
        float[] result = new float[] {gyroX, gyroY, gyroZ};
        return result;
    }

    public static byte[] buileNotifyControlCommand() {
        // Needs parameters to control notification switch.
        byte[] cmd = new byte[] {1, 1, 1, 0, 0, 0, 0, 0, 0};
        return cmd;
    }

    public static String sensorDataToString(byte count, byte side, short seq, int[] pressure, float[] accel, float[] gyro) {
        StringBuilder output = new StringBuilder();

        if(count == 1) {
            short sSeq = seq;
            if(sSeq < 0)
                sSeq += 256;
            output.append(sSeq).append(",");

            if(pressure != null) {
                output.append(pressure[0]).append(",");
                output.append(pressure[1]).append(",");
                output.append(pressure[2]).append(",");
                output.append(pressure[3]).append(",");
            } else {
                output.append("null,null,null,null,");
            }

            if(accel != null) {
                output.append(accel[0]).append(",");
                output.append(accel[1]).append(",");
                output.append(accel[2]).append(",");
            } else {
                output.append("null,null,null,");
            }

            if(gyro != null) {
                output.append(gyro[0]).append(",");
                output.append(gyro[1]).append(",");
                output.append(gyro[2]);
            } else {
                output.append("null,null,null");
            }

            return output.toString();
        } else {
            if(lastSide == (byte)-1) {
                lastSide = side;
                tmpSeq = seq;
                tmpPressure = pressure;
                tmpAccel = accel;
                tmpGyro = gyro;

                return null;
            } else {
                if(lastSide != side) {
                    if(side == 1) {
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)tmpSeq, tmpPressure, tmpAccel, tmpGyro)).append(",");
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)seq, pressure, accel, gyro));
                    } else {
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)seq, pressure, accel, gyro)).append(",");
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)tmpSeq, tmpPressure, tmpAccel, tmpGyro));
                    }
                    lastSide = (byte)-1;
                    tmpSeq = 0;
                    tmpPressure = null;
                    tmpAccel = null;
                    tmpGyro = null;

                    if(isConflict) {
                        lastSide = sideBk;
                        tmpSeq = tmpSeqBk;
                        tmpPressure = tmpPressureBk;
                        tmpAccel = tmpAccelBk;
                        tmpGyro = tmpGyroBk;
                        sideBk = (byte)-1;
                        tmpSeqBk = 0;
                        tmpPressureBk = null;
                        tmpAccelBk = null;
                        tmpGyroBk = null;
                        isConflict = false;
                    }

                    return output.toString();
                } else {
                    //Log.e(TAG, "Buffer conflict");
                    isConflict = true;
                    sideBk = side;
                    tmpSeqBk = seq;
                    tmpPressureBk = pressure;
                    tmpAccelBk = accel;
                    tmpGyroBk = gyro;

                    return null;
                }
            }
        }
    }
}
