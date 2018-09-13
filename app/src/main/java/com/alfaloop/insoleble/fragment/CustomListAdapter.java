package com.alfaloop.insoleble.fragment;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.alfaloop.insoleble.R;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<NearBleDeviceItem> {
    private static final String TAG = CustomListAdapter.class.getSimpleName();

    private ArrayList<NearBleDeviceItem> nearBleDeviceItemList = null;
    private Context context = null;
    private Handler handler = null;

    public CustomListAdapter(Context context, int textViewResourceId, ArrayList<NearBleDeviceItem> nearBleDeviceItemList) {
        super(context, textViewResourceId, nearBleDeviceItemList);
        this.nearBleDeviceItemList = new ArrayList<NearBleDeviceItem>();
        this.nearBleDeviceItemList.addAll(nearBleDeviceItemList);
        this.context = context;
    }

    private class ViewHolder {
        public TextView addrTitle;
        public TextView rssiTitle;
        public CheckBox checkbox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_item, null);

            final ViewHolder localHolder = new ViewHolder();
            localHolder.addrTitle = (TextView) convertView.findViewById(R.id.item_sub_title_addr);
            localHolder.rssiTitle = (TextView) convertView.findViewById(R.id.item_sub_title_rssi);
            localHolder.checkbox = (CheckBox) convertView.findViewById(R.id.item_check_box);
            convertView.setTag(localHolder);

            localHolder.checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    NearBleDeviceItem nearBleDeviceItem = (NearBleDeviceItem) cb.getTag();
                    nearBleDeviceItem.setSelected(cb.isChecked());
                }
            });
            localHolder.addrTitle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(handler != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                localHolder.checkbox.setChecked(!localHolder.checkbox.isChecked());
                                NearBleDeviceItem nearBleDeviceItem = (NearBleDeviceItem) localHolder.checkbox.getTag();
                                nearBleDeviceItem.setSelected(localHolder.checkbox.isChecked());
                            }
                        });
                }
            });
            holder = localHolder;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NearBleDeviceItem nearBleDeviceItem = nearBleDeviceItemList.get(position);
        holder.addrTitle.setText(String.format(" %s", nearBleDeviceItem.getAddrStr()));
        holder.rssiTitle.setText(String.format(" rssi %s", nearBleDeviceItem.getRSSI()));
        holder.checkbox.setText(String.format(" %s", nearBleDeviceItem.getDeviceName()));
        holder.checkbox.setChecked(nearBleDeviceItem.isSelected());
        holder.checkbox.setTag(nearBleDeviceItem);

        return convertView;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}