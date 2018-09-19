package com.alfaloop.insoleble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alfaloop.insoleble.ble.AlfaoneBleConnection;
import com.alfaloop.insoleble.ble.BleConnectionOperator;
import com.alfaloop.insoleble.ble.BleConnectionScope;
import com.alfaloop.insoleble.ble.NearBluetoothDevice;
import com.alfaloop.insoleble.ble.NikeBleConnection;
import com.alfaloop.insoleble.ble.config.BleConnConfig;
import com.alfaloop.insoleble.ble.support.device.Alfaone;
import com.alfaloop.insoleble.ble.support.device.NikePlus;
import com.alfaloop.insoleble.fragment.ConnectedFragment;
import com.alfaloop.insoleble.fragment.InitViewFragment;
import com.alfaloop.insoleble.fragment.ScanFragment;
import com.alfaloop.insoleble.utils.FileUtils;
import com.alfaloop.insoleble.utils.PermissionManager;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final int REQUEST_ENABLE_BT = 1;
    private final int SHOW_SHORT = Toast.LENGTH_SHORT;
    private final int SHOW_LONG = Toast.LENGTH_LONG;

    private Button connectionButton = null;
    private Button recordingButton = null;

    private Handler handler = null;
    private Context context = this;
    private Vector nearBleDevicesBox = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner mLEScanner = null;
    private ScanSettings settings = null;
    private List<ScanFilter> filters = null;
    private BleConnectionScope connectionScope1 = null;
    private BleConnectionScope connectionScope2 = null;
    private FileUtils.CsvWriter csw = null;
    private RefreshLayout refreshLayout = null;
    private InitViewFragment initViewFragment = null;
    private ScanFragment scanViewFragment = null;
    private ConnectedFragment connectedViewFragment = null;
    private boolean isConnected = false;
    private boolean isScanning = false;
    private boolean isRecording = false;
    private byte interactiveDeviceType = 0;
    private SharedPreferences sharedPref = null;
    private SharedPreferences.Editor editor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        // Check permissions are enabled.
        if(!PermissionManager.allPermissionsGranted(this))
            PermissionManager.getRuntimePermissions(this);
        initPreferencesShared();
        launchStartFragment();
        initMainActivityViewComponents();
        initBleModule();
        checkPreferencesExisted();
        loadPreferencesAutoScanSet();
    }

    @Override
    public void onBackPressed() {
        if(!isConnected)
            MainActivity.this.finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(isScanning)
            return false;
        if(isConnected)
            return false;

        if(id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void initMainActivityViewComponents() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        refreshLayout = (RefreshLayout)findViewById(R.id.refresh_layout);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableAutoLoadMore(false);
        refreshLayout.setEnableOverScrollDrag(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if(!initViewFragment.isProcessing()) {
                    MainActivity.this.onRefresh();
                    refreshlayout.finishRefresh(800);
                } else {
                    refreshlayout.finishRefresh(50);
                }
            }
        });

        connectionButton = (Button) findViewById(R.id.connection_btn);
        connectionButton.setEnabled(false);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnected) {
                    connectSelectDevices();
                } else if(isConnected) {
                    destroyConnectionsScope();
                    handler.post(disconnectButtonTask);
                }
            }
        });

        recordingButton = (Button) findViewById(R.id.recording_btn);
        recordingButton.setEnabled(false);
        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.post(recordingButtonOnClick);
            }
        });
    }

    private void initBleModule() {
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            makeToastMessage(R.string.ble_donot_support, SHOW_SHORT);
            finish();
        }

        nearBleDevicesBox = new Vector(2);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        filters = new ArrayList<ScanFilter>();
    }

    private void initPreferencesShared() {
        sharedPref = context.getSharedPreferences(getString(R.string.pref_file_key),
                Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    private void checkPreferencesExisted() {
        int result = sharedPref.getInt(getString(R.string.pref_auto_scan), -1);
        // No setting saved, create.
        if(result == -1) {
            editor.putInt(getString(R.string.pref_scan_time), BleConnConfig.SCAN_TIME_S);
            editor.putInt(getString(R.string.pref_auto_scan), 0);
            editor.commit();
        }
    }

    private void loadPreferencesAutoScanSet() {
        int result = sharedPref.getInt(getString(R.string.pref_auto_scan), 0);
        if(result > 0)
            onRefresh();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            int scanTimeMs = sharedPref.getInt(getString(R.string.pref_scan_time),
                    BleConnConfig.SCAN_TIME_S) * 1000;
            mLEScanner.startScan(filters, settings, mScanCallback);
            handler.postDelayed(whenScanEnd, scanTimeMs);
            isScanning = true;
        } else {
            mLEScanner.stopScan(mScanCallback);
            isScanning = false;
        }
    }

    private void startSensorTrans(boolean isFinalConnection, byte insoleSide) {
        // Start both connection's sensor work when all connections established.
        if(!isFinalConnection)
            return;
        // Connection 1 can't be null.
        if(connectionScope1 == null)
            return;

        connectionScope1.startWithDelay(50);
        if(insoleSide == 1) {
            try {
                Thread.sleep(BleConnConfig.CONTROL_POINT_WRITE_DELAY_S);
            } catch (Exception e) {
                e.printStackTrace();
            }
            connectionScope2.startWithDelay(50);
        }
    }

    private void startConnectedFragment(String[] addresses, byte insoleSide, boolean isFinalConnection) {
        // Start both connection's sensor work when all connections established.
        if(!isFinalConnection)
            return;
        // Connection 1 can't be null.
        if(connectionScope1 == null)
            return;

        final int showMsgId = (insoleSide == 0) ? R.string.start_listening_1_insole_msg : R.string.start_listening_2_insoles_msg;
        lunchConnectedFragment(addresses);
        handler.post(new Runnable() {
            @Override
            public void run() {
                makeToastMessage(showMsgId, SHOW_SHORT);
                connectionButton.setText(R.string.btn_disconnect);
                connectionButton.setEnabled(true);
                isConnected = true;
                recordingButton.setEnabled(true);
            }
        });
    }

    private BleConnectionOperator getDeviceConnection(String address) {
        BluetoothDevice bleDevice = mBluetoothAdapter.getRemoteDevice(address);
        BleConnectionOperator conn = null;
        if(bleDevice.getName().equals(getString(R.string.ble_alfaone_device_name))) {
            conn = new AlfaoneBleConnection(MainActivity.this, mBluetoothAdapter, address);
        } else if(bleDevice.getName().equals(getString(R.string.ble_nsensor_device_name))) {
            conn = new NikeBleConnection(MainActivity.this, mBluetoothAdapter, address);
        }
        return conn;
    }

    private byte getDeviceType(String deviceAddress) {
        BluetoothDevice bleDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        byte devTypeTmp = 0;
        if(bleDevice.getName().equals(getString(R.string.ble_alfaone_device_name))) {
            devTypeTmp = Alfaone.DEVICE_TYPE;
        } else if(bleDevice.getName().equals(getString(R.string.ble_nsensor_device_name))) {
            devTypeTmp = NikePlus.DEVICE_TYPE;
        }
        return devTypeTmp;
    }

    private void startConnectionScope1(String[] deviceAddresses, byte connCount) {
        boolean isFinalConnection = (connCount == 1) ? true : false;
        BleConnectionOperator conn = getDeviceConnection(deviceAddresses[0]);
        final byte devType = getDeviceType(deviceAddresses[0]);

        connectionScope1 = new BleConnectionScope(MainActivity.this, handler, mBluetoothAdapter,
                deviceAddresses[0], conn);
        connectionScope1.registDiscoveryProfileCompleteCallback(result -> {
            makeToastMessageWithHandler(R.string.discoveried_1_insole_msg, SHOW_SHORT);
        });
        connectionScope1.registEnabledNotifyCompleteCallback(result -> {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    interactiveDeviceType = devType;
                    startSensorTrans(isFinalConnection, (byte)0);
                    startConnectedFragment(deviceAddresses, (byte)0, isFinalConnection);
                    if(!isFinalConnection)
                        startConnectionScope2(deviceAddresses, connCount);
                    if(connectedViewFragment != null)
                        connectedViewFragment.updatePowerView((byte)0,
                                connectionScope1.getDeviceBatteryPower());
                }
            }, 1000);
        });
        connectionScope1.registSensorRawCallback((seq, pressure, accelerate, gyro, wasLoss) -> {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    int[] pressureInt = null;
                    float[] accelerateFloat = null;
                    float[] gyroFloat = null;
                    if(devType == Alfaone.DEVICE_TYPE) {
                        pressureInt = Alfaone.parsePressure(pressure);
                        accelerateFloat = Alfaone.parseAccel(accelerate);
                        gyroFloat = Alfaone.parseGyro(gyro);
                    } else if(devType == NikePlus.DEVICE_TYPE) {
                        pressureInt = NikePlus.parsePressure(pressure);
                        accelerateFloat = NikePlus.parseAccel(accelerate);
                    }

                    connectedViewFragment.updateSensorView(devType, (byte)0, pressureInt, accelerateFloat, gyroFloat);
                    if(isRecording)
                        recordSensorData(devType, (byte)0, connCount, seq, pressureInt, accelerateFloat, gyroFloat);
                }
            });
        });
        connectionScope1.excute();
    }

    private void startConnectionScope2(String[] deviceAddresses, byte connCount) {
        BleConnectionOperator conn = getDeviceConnection(deviceAddresses[1]);
        final byte devType = getDeviceType(deviceAddresses[1]);

        connectionScope2 = new BleConnectionScope(MainActivity.this, handler, mBluetoothAdapter,
                deviceAddresses[1], conn);
        connectionScope2.registDiscoveryProfileCompleteCallback(result -> {
            if (result == BleConnectionScope.RESULT_SUCCESS) {
                makeToastMessageWithHandler(R.string.discoveried_2_insole_msg, SHOW_SHORT);
            } else if (result == BleConnectionScope.RESULT_FAIL) {
                destroyConnectionsScope();
                handler.post(disconnectButtonTask);
                makeToastMessageWithHandler(R.string.discoveried_2_insole_fail_msg, SHOW_SHORT);
            }
        });
        connectionScope2.registEnabledNotifyCompleteCallback(result -> {
            final boolean isFinalConnection = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startSensorTrans(isFinalConnection, (byte)1);
                    startConnectedFragment(deviceAddresses, (byte)1, isFinalConnection);
                    connectedViewFragment.updatePowerView((byte)0,
                            connectionScope1.getDeviceBatteryPower());
                    connectedViewFragment.updatePowerView((byte)1,
                            connectionScope2.getDeviceBatteryPower());
                }
            }, 1000);
        });
        connectionScope2.registSensorRawCallback((seq, pressure, accelerate, gyro, wasLoss) -> {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    int[] pressureInt = null;
                    float[] accelFloat = null;
                    float[] gyroFloat = null;
                    if(devType == Alfaone.DEVICE_TYPE) {
                        pressureInt = Alfaone.parsePressure(pressure);
                        accelFloat = Alfaone.parseAccel(accelerate);
                        gyroFloat = Alfaone.parseGyro(gyro);
                    } else if(devType == NikePlus.DEVICE_TYPE) {
                        pressureInt = NikePlus.parsePressure(pressure);
                        accelFloat = NikePlus.parseAccel(accelerate);
                    }

                    connectedViewFragment.updateSensorView(devType, (byte)1, pressureInt, accelFloat, gyroFloat);
                    if(isRecording)
                        recordSensorData(devType, (byte)1, connCount, seq, pressureInt, accelFloat, gyroFloat);
                }
            });
        });
        connectionScope2.excute();
    }

    private void connectSelectDevices() {
        byte count = scanViewFragment.getSelectedDevicesCount();
        if(count > 0 && count <= 2) {
            connectionButton.setEnabled(false);
            String[] addresses = scanViewFragment.getSelectedDevices();
            if(isCorrectInsolePair(count, addresses)) {
                Log.e(TAG, Arrays.toString(addresses));
                startConnectionScope1(addresses, count);
            } else {
                makeToastMessageWithHandler(R.string.select_wrong_device_pair_msg, SHOW_SHORT);
                connectionButton.setEnabled(true);
            }
        } else {
            makeToastMessageWithHandler(R.string.select_wrong_device_qty_msg, SHOW_SHORT);
        }
    }

    private boolean isContainDevice(BluetoothDevice bleDevice) {
        boolean result = false;
        for(int i = 0; i < nearBleDevicesBox.size(); i++) {
            NearBluetoothDevice nearBleDevice = (NearBluetoothDevice)nearBleDevicesBox.get(i);
            if(nearBleDevice.getDevice().equals(bleDevice)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice bleDevice = result.getDevice();
            NearBluetoothDevice nearBleDevice = new NearBluetoothDevice(bleDevice,
                    result.getRssi());
            //Log.e(TAG, " " + result.getRssi());
            if (bleDevice.getName() != null) {
                if (!isContainDevice(bleDevice)) {
                    String devName = bleDevice.getName().toString();
                    if (devName.equals(getString(R.string.ble_alfaone_device_name)) ||
                            devName.equals(getString(R.string.ble_nsensor_device_name))) {
                        nearBleDevicesBox.add(nearBleDevice);
                    }
                }
            }
        }
    };

    private void stopRecording() {
        recordingButton.setText(R.string.btn_start_recording);
        try {
            isRecording = false;
            csw.killWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable disconnectButtonTask = new Runnable() {
        @Override
        public void run() {
            makeToastMessage(R.string.disconnect_msg, SHOW_SHORT);
            connectionButton.setText(R.string.btn_connect);
            connectionButton.setEnabled(true);
            isConnected = false;
            if(isRecording)
                stopRecording();
            recordingButton.setEnabled(false);
        }
    };

    private Runnable recordingButtonOnClick = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                stopRecording();
            } else {
                recordingButton.setText(R.string.btn_stop_recording);
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                csw = FileUtils.CsvWriter.initWriter(ts, FileUtils.CsvWriter.DELETE_FILE_IF_EXIST);

                String title = Alfaone.FIELD_TITLE_RAW;
                if(scanViewFragment.getSelectedDevicesCount() == 1) {
                    if (interactiveDeviceType == Alfaone.DEVICE_TYPE)
                        title = Alfaone.FIELD_TITLE_RAW;
                    else if (interactiveDeviceType == NikePlus.DEVICE_TYPE)
                        title = NikePlus.FIELD_TITLE_RAW;
                } else {
                    if (interactiveDeviceType == Alfaone.DEVICE_TYPE)
                        title = Alfaone.FIELD_TITLE_RAW.concat(",").concat(Alfaone.FIELD_TITLE_RAW);
                    else if (interactiveDeviceType == NikePlus.DEVICE_TYPE)
                        title = NikePlus.FIELD_TITLE_RAW.concat(",").concat(NikePlus.FIELD_TITLE_RAW);
                }

                try {
                    csw.insertDateRow(title);
                    isRecording = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable whenScanEnd = new Runnable() {
        @Override
        public void run() {
            scanLeDevice(false);
            setupScanResultView();
            connectionButton.setEnabled(true);
            isScanning = false;
        }
    };

    private Runnable onRefreshTask = new Runnable() {
        @Override
        public void run() {
            makeToastMessage(R.string.started_scan_msg, SHOW_SHORT);
            destroyConnectionsScope();
            connectionButton.setText(R.string.btn_connect);
            connectionButton.setEnabled(false);
            recordingButton.setEnabled(false);
            launchStartFragment();
            initViewFragment.startProcessing();
        }
    };

    private void launchStartFragment() {
        int scanTimeS = sharedPref.getInt(getString(R.string.pref_scan_time), BleConnConfig.SCAN_TIME_S);

        initViewFragment = new InitViewFragment();
        initViewFragment.setScanTimeS(scanTimeS);
        initViewFragment.setHandler(handler);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.test_fragment_container, initViewFragment);
        ft.commit();
    }

    private void launchScanResultFragment(NearBluetoothDevice[] nearDevices) {
        scanViewFragment = ScanFragment.newInstance(nearDevices);
        scanViewFragment.setHandler(handler);
        refreshLayout.setEnableRefresh(false);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.test_fragment_container, scanViewFragment);
        ft.commit();
    }

    private void lunchConnectedFragment(String[] addresses) {
        refreshLayout.setEnableRefresh(true);
        connectedViewFragment = ConnectedFragment.newInstance(addresses);
        connectedViewFragment.setHandler(handler);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.test_fragment_container, connectedViewFragment);
        ft.commit();
    }

    private void setupScanResultView() {
        String[] addressesList = new String[nearBleDevicesBox.size()];
        String[] nameList = new String[nearBleDevicesBox.size()];
        String[] rssiStrengthList = new String[nearBleDevicesBox.size()];

        for(int i = 0; i < nearBleDevicesBox.size(); i++) {
            NearBluetoothDevice nearBleDevice = (NearBluetoothDevice) nearBleDevicesBox.get(i);
            addressesList[i] = nearBleDevice.getDevice().getAddress();
            nameList[i] = nearBleDevice.getDevice().getName();
            rssiStrengthList[i] = String.valueOf(nearBleDevice.getRSSI());
        }

        NearBluetoothDevice[] nearDevices = new NearBluetoothDevice[nearBleDevicesBox.size()];
        for(int i = 0; i < nearDevices.length; i++) {
            nearDevices[i] = (NearBluetoothDevice) nearBleDevicesBox.get(i);
        }

        // Sort
        for(int i = 0; i < nearDevices.length; i++) {
            for(int j = i + 1; j < nearDevices.length; j++) {
                if(nearDevices[j].getRSSI() > nearDevices[i].getRSSI()) {
                    NearBluetoothDevice temp;
                    temp = nearDevices[i];
                    nearDevices[i] = nearDevices[j];
                    nearDevices[j] = temp;
                }
            }
        }

        launchScanResultFragment(nearDevices);
    }

    private void destroyConnectionsScope() {
        if (connectionScope1 != null)
            connectionScope1.destroy();
        if (connectionScope2 != null)
            connectionScope2.destroy();
        connectionScope1 = null;
        connectionScope2 = null;
    }

    private void recordSensorData(byte devType, byte side, byte count, short seq, int[] pressure, float[] accelerate, float[] gyro) {
        String out = null;
        if(devType == Alfaone.DEVICE_TYPE)
            out = Alfaone.sensorDataToString(count, side, seq, pressure, accelerate, gyro);
        else if(devType == NikePlus.DEVICE_TYPE)
            out = NikePlus.sensorDataToString(count, side, seq, pressure, accelerate);

        if(out != null) {
            try {
                csw.insertDateRow(out);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isCorrectInsolePair(int count, String[] addresses) {
        if(count == 1)
            return true;
        else if(count == 2) {
            BluetoothDevice bleDeviceLeft = mBluetoothAdapter.getRemoteDevice(addresses[0]);
            BluetoothDevice bleDeviceRight = mBluetoothAdapter.getRemoteDevice(addresses[1]);

            if(bleDeviceLeft.getName().equals(bleDeviceRight.getName()))
                return true;
            else
                return false;
        } else {
            return  true;
        }
    }

    private void makeToastMessage(final int msg, final int duration) {
        Toast.makeText(MainActivity.this, msg, duration).show();
    }

    private void makeToastMessageWithHandler(final int msg, final int duration) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                makeToastMessage(msg, duration);
            }
        });
    }

    public void onRefresh() {
        if(!isConnected) {
            nearBleDevicesBox.removeAllElements();
            scanLeDevice(true);
            handler.post(onRefreshTask);
        }
    }
}
