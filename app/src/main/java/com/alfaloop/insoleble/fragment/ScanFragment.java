package com.alfaloop.insoleble.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alfaloop.insoleble.MainActivity;
import com.alfaloop.insoleble.R;
import com.alfaloop.insoleble.ble.NearBluetoothDevice;

import java.util.ArrayList;
import java.util.Arrays;

public class ScanFragment extends Fragment {
    private static final String TAG = ScanFragment.class.getSimpleName();

    private Context mainContext = null;
    private Handler handler = null;
    private ListView listView = null;

    private String[] addrList = null;
    private String[] nameList = null;
    private String[] rssiList = null;

    public static ScanFragment newInstance(NearBluetoothDevice[] nearDevices) {
        int length = nearDevices.length;
        String[] addressesList = new String[length];
        String[] nameList = new String[length];
        String[] rssiStrengthList = new String[length];

        for(int i = 0; i < length; i++) {
            NearBluetoothDevice nearBleDevice = nearDevices[i];
            addressesList[i] = nearBleDevice.getDevice().getAddress();
            nameList[i] = nearBleDevice.getDevice().getName();
            rssiStrengthList[i] = String.valueOf(nearBleDevice.getRSSI());
        }

        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putStringArray("addrs", addressesList);
        args.putStringArray("names", nameList);
        args.putStringArray("rssis", rssiStrengthList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addrList = getArguments().getStringArray("addrs");
        nameList = getArguments().getStringArray("names");
        rssiList = getArguments().getStringArray("rssis");
        Log.e(TAG, Arrays.toString(addrList));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scaned_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)mainContext).onRefresh();
            }
        });
        listView = (ListView) view.findViewById(R.id.listView1);
        if(addrList.length > 0) {
            ArrayList<NearBleDeviceItem> nearBleDeviceItemList = new ArrayList<NearBleDeviceItem>();
            for(int i = 0; i < addrList.length; i++) {
                NearBleDeviceItem nearBleDeviceItem = new NearBleDeviceItem(addrList[i], nameList[i], rssiList[i], false);
                nearBleDeviceItemList.add(nearBleDeviceItem);
            }

            CustomListAdapter dataAdapter = new CustomListAdapter(mainContext, R.layout.list_item, nearBleDeviceItemList);
            listView.setAdapter(dataAdapter);
            dataAdapter.setHandler(handler);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    NearBleDeviceItem nearBleDeviceItem = (NearBleDeviceItem) parent.getItemAtPosition(position);
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainContext = context;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public byte getSelectedDevicesCount() {
        byte count = 0;
        for(int i = 0; i < listView.getAdapter().getCount(); i++) {
            NearBleDeviceItem nearBleDeviceItem = (NearBleDeviceItem)listView.getAdapter().getItem(i);
            if(nearBleDeviceItem.isSelected()) {
                count += 1;
            }
        }
        return count;
    }

    public String[] getSelectedDevices() {
        int count = getSelectedDevicesCount();
        String[] addrs = new String[count];
        int k = 0;
        for(int i = 0; i < listView.getAdapter().getCount(); i++) {
            NearBleDeviceItem nearBleDeviceItem = (NearBleDeviceItem)listView.getAdapter().getItem(i);
            if(nearBleDeviceItem.isSelected()) {
                addrs[k] = nearBleDeviceItem.getAddrStr();
                k += 1;
            }
        }
        return addrs;
    }
}
