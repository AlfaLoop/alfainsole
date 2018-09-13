package com.alfaloop.insoleble.ble.support.device;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class NikePlus {
    private static final String TAG = NikePlus.class.getSimpleName();

    public static final byte DEVICE_TYPE = (byte)200;
    public static final short DELAY_OF_ENABLED_NOTIFY_MS = 100;
    public static final short DELAY_OF_WRITE_CMD_MS = 30;

    public static final UUID UUID_NIKE_MAIN_SERVICE = UUID.fromString("f1d30565-c50e-c833-e608-f2d491a8928c");
    public static final UUID UUID_NIKE_MAIN_CHARACTER_1 = UUID.fromString("f1fe195a-9067-2481-848d-3d7cf2a45191");
    public static final UUID UUID_NIKE_MAIN_CHARACTER_2 = UUID.fromString("f230df48-9067-2481-848d-bb04b2b0f6c0");
    public static final UUID UUID_NIKE_MAIN_CHARACTER_3 = UUID.fromString("f230df4d-d97d-66b3-9173-b185eefe84fe");
    public static final UUID UUID_NIKE_MAIN_CHARACTER_4 = UUID.fromString("f230df4f-b00b-ed7d-92c7-a4c6d31444e3");
    public static final UUID UUID_NIKE_MAIN_CHARACTER_5 = UUID.fromString("f230df58-d1b0-8edd-3327-28b3c5a7bac3");

    public static final String FIELD_TITLE_RAW =
            "seq,pressure 1,pressure 2,pressure 3,pressure 4,accel X,accel Y, accel Z";

    private static byte lastSide = (byte)-1;
    private static short tmpSeq = 0;
    private static int[] tmpPressure = null;
    private static float[] tmpAccel = null;
    private static boolean isConflict = false;
    private static byte sideBk = (byte)-1;
    private static short tmpSeqBk = 0;
    private static int[] tmpPressureBk = null;
    private static float[] tmpAccelBk = null;

    public static final byte RATE_8_HZ = (byte)0x43;
    public static final byte RATE_16_HZ = (byte)0x54;
    public static final byte RATE_32_HZ = (byte)0x63;
    public static final byte RATE_64_HZ = (byte)0x72;

    public static final byte START_INIT_CMD_1 = 0;
    public static final byte AUTH_CHALLENGE_CMD = 1;
    public static final byte START_INIT_CMD_2 = 2;
    public static final byte START_INIT_CMD_3 = 3;
    public static final byte START_INIT_CMD_4 = 4;
    public static final byte SET_SAMPLING_RATE_CMD = 5;

    private static float PRESSURE_MAX_VALUE = 1200000f;
    private static final float PRESSURE_MIN_VALUE = 0;

    public static byte[] buileCommand(byte cmdCode, byte[] ref) {
        byte[] cmd;
        switch (cmdCode) {
            case START_INIT_CMD_1:
                cmd = new byte[] {0x0C, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case AUTH_CHALLENGE_CMD:
                cmd = new byte[] {0x0C, 0x00, 0x01, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                cmd[3] = (byte)(ref[10] ^ 0xE3);
                cmd[4] = (byte)(ref[6] ^ 0x78);
                cmd[5] = (byte)(ref[4] ^ 0x9F);
                cmd[6] = (byte)(ref[8] ^ 0xB9);
                cmd[7] = (byte)(ref[11] ^ 0x20);
                cmd[8] = (byte)(~ref[5]);
                cmd[9] = (byte)(ref[7] ^ 0xAA);
                cmd[10] = (byte)(ref[9] ^ 0x55);
                break;
            case START_INIT_CMD_2:
                cmd = new byte[] {0x06, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case START_INIT_CMD_3:
                cmd = new byte[] {0x10, 0x00, 0x02, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case START_INIT_CMD_4:
                cmd = new byte[] {0x0B, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            case SET_SAMPLING_RATE_CMD:
                cmd = new byte[] {0x02, 0x00, 0x01, ref[0], 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
            default:
                cmd = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                break;
        }
        return cmd;
    }

    public static int parsePower(byte[] data) {
        if(data[0] == 0x00 && data[1] == 0x06 && data[2] == 0x01 && data[3] == 0x02  && data[9] == 0x02) {
            return (short)data[8];
        }
        return -1;
    }

    public static int[] parsePressure(byte[] raw) {
        if(raw == null)
            return  null;

        int thumb = ByteBuffer.wrap(new byte[]{raw[0], raw[1], raw[2], 0})
                .order(ByteOrder.LITTLE_ENDIAN).getInt();
        int innerBall = ByteBuffer.wrap(new byte[]{raw[3], raw[4], raw[5], 0})
                .order(ByteOrder.LITTLE_ENDIAN).getInt();
        int outerBall = ByteBuffer.wrap(new byte[]{raw[6], raw[7], raw[8], 0})
                .order(ByteOrder.LITTLE_ENDIAN).getInt();
        int heel = ByteBuffer.wrap(new byte[]{raw[9], raw[10], raw[11], 0})
                .order(ByteOrder.LITTLE_ENDIAN).getInt();

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

        float accX = (float)ByteBuffer.wrap(new byte[]{raw[0], raw[1]})
                .order(ByteOrder.BIG_ENDIAN).getShort() / 4096f;
        float accY = (float)ByteBuffer.wrap(new byte[]{raw[2], raw[3]})
                .order(ByteOrder.BIG_ENDIAN).getShort() / 4096f;
        float accZ = (float)ByteBuffer.wrap(new byte[]{raw[4], raw[5]})
                .order(ByteOrder.BIG_ENDIAN).getShort() / 4096f;
        float[] result = new float[] {accX, accY, accZ};
        return result;
    }

    public static short overflowHandle(short sH) {
        if (sH >= 0xc00) {
            sH -= 0xc00;
        } else {
            if (sH >= 0x800) {
                sH -= 0x800;
            } else {
                if (sH >= 0x400) {
                    sH -= 0x400;
                }
            }
        }
        return sH;
    }

    public static String sensorDataToString(byte count, byte side, short seq, int[] pressure, float[] accel) {
        StringBuilder output = new StringBuilder();

        if(count == 1) {
            short sSeq = seq;
            if(sSeq < 0)
                sSeq += 65536;
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
                output.append(accel[2]);
            } else {
                output.append("null,null,null,");
            }

            return output.toString();
        } else {
            if(lastSide == (byte)-1) {
                lastSide = side;
                tmpSeq = seq;
                tmpPressure = pressure;
                tmpAccel = accel;

                return null;
            } else {
                if(lastSide != side) {
                    if(side == 1) {
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)tmpSeq, tmpPressure, tmpAccel)).append(",");
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)seq, pressure, accel));
                    } else {
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)seq, pressure, accel)).append(",");
                        output.append(sensorDataToString((byte) 1, (byte) 0, (short)tmpSeq, tmpPressure, tmpAccel));
                    }
                    lastSide = (byte)-1;
                    tmpSeq = 0;
                    tmpPressure = null;
                    tmpAccel = null;

                    if(isConflict) {
                        lastSide = sideBk;
                        tmpSeq = tmpSeqBk;
                        tmpPressure = tmpPressureBk;
                        tmpAccel = tmpAccelBk;
                        sideBk = (byte)-1;
                        tmpSeqBk = 0;
                        tmpPressureBk = null;
                        tmpAccelBk = null;
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

                    return null;
                }
            }
        }
    }
}
