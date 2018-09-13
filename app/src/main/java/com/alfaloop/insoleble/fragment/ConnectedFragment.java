package com.alfaloop.insoleble.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alfaloop.insoleble.R;
import com.alfaloop.insoleble.visualization.FootPressureView;
import com.alfaloop.insoleble.visualization.InsoleSensor;
import com.alfaloop.insoleble.visualization.SensorDataGetter;

public class ConnectedFragment extends Fragment {
    private static final String TAG = ConnectedFragment.class.getSimpleName();

    private Context mainContext = null;
    private Handler handler = null;
    private TextView lDevNameView = null;
    private TextView lPressureView = null;
    private TextView lGyroView = null;
    private TextView rDevNameView = null;
    private TextView rPressureView = null;
    private TextView rGyroView = null;
    private String[] devicesName = null;
    private SensorDataGetter leftSensorDataGetter = null;
    private SensorDataGetter rightSensorDataGetter = null;
    private boolean switchSide = false;

    public static ConnectedFragment newInstance(String[] devicesName) {
        ConnectedFragment fragment = new ConnectedFragment();
        Bundle args = new Bundle();
        args.putStringArray("names", devicesName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devicesName = getArguments().getStringArray("names");
        switchSide = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.connected_couple_view_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        lDevNameView = (TextView)view.findViewById(R.id.c_left_device_name);
        lPressureView = (TextView)view.findViewById(R.id.c_left_pressure);
        lGyroView = (TextView)view.findViewById(R.id.c_left_gyro);
        rDevNameView = (TextView)view.findViewById(R.id.c_right_device_name);
        rPressureView = (TextView)view.findViewById(R.id.c_right_pressure);
        rGyroView = (TextView)view.findViewById(R.id.c_right_gyro);

        lDevNameView.setText(devicesName[0]);
        if(devicesName.length >1)
            rDevNameView.setText(devicesName[1]);

        InsoleSensor.loadData(getResources(), 0.6f, 1.2f);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.pressure_sensor_vis_view);
        leftSensorDataGetter = new SensorDataGetter(SensorDataGetter.Direction.LEFT, false);
        rightSensorDataGetter = new SensorDataGetter(SensorDataGetter.Direction.RIGHT, false);
        FootPressureView leftView = new FootPressureView(mainContext, leftSensorDataGetter);
        FootPressureView rightView = new FootPressureView(mainContext, rightSensorDataGetter);

        leftView.setLayoutParams(
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        rightView.setLayoutParams(
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        leftView.setOnClickListener(pressureViewClickListener);
        rightView.setOnClickListener(pressureViewClickListener);
        layout.addView(leftView);
        layout.addView(rightView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainContext = context;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void updateSensorView(byte deviceType, byte side, int[] pressure, float[] accel, float[] gyro) {
        String pressureStr = null;
        String gyroStr = null;
        float[] data = null;

        if(pressure != null) {
            for (int i = 0; i < pressure.length; i++)
                if (pressure[i] < 3) pressure[i] = 0;
        }
        if(pressure != null && accel != null) {
            data = new float[]{accel[0], accel[1], accel[2],
                    (pressure[0]), (pressure[1]),
                    (pressure[2]), (pressure[3])};
        } else {
            if(pressure == null && accel != null)
                data = new float[]{accel[0], accel[1], accel[2],
                        0, 0, 0, 0};
            else if(pressure != null && accel == null)
                data = new float[]{0, 0, 0,
                        (pressure[0]), (pressure[1]),
                        (pressure[2]), (pressure[3])};
        }

        if(pressure != null)
            pressureStr = String.format("%d %d %d %d", pressure[0], pressure[1], pressure[2], pressure[3]);
        if(gyro != null)
            gyroStr = String.format("x: %.2f\ny: %.2f\nz: %.2f", gyro[0], gyro[1], gyro[2]);

        if((!switchSide && side == 0) || (switchSide && side == 1)) {
            if(gyroStr != null)
                lGyroView.setText(gyroStr);
            if(pressureStr != null)
                lPressureView.setText(pressureStr);
            if(data != null)
                leftSensorDataGetter.addSensorData(data);
        } else if((!switchSide && side == 1) || (switchSide && side == 0)) {
            if(gyroStr != null)
                rGyroView.setText(gyroStr);
            if(pressureStr != null)
                rPressureView.setText(pressureStr);
            if(data != null)
                rightSensorDataGetter.addSensorData(data);
        }
    }

    public void updatePowerView(byte side, int batteryPower) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (batteryPower > 0)
                        if (side == 0) {
                            lGyroView.setText(String.format("Power %d %%", batteryPower));
                        } else if (side == 1) {
                            rGyroView.setText(String.format("Power %d %%", batteryPower));
                        }
                } catch(NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }, 1000);
    }

    private View.OnClickListener pressureViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switchSide = !switchSide;
            if(handler != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String tmp = lDevNameView.getText().toString();
                        lDevNameView.setText(rDevNameView.getText().toString());
                        rDevNameView.setText(tmp);

                        tmp = lPressureView.getText().toString();
                        lPressureView.setText(rPressureView.getText().toString());
                        rPressureView.setText(tmp);

                        tmp = lGyroView.getText().toString();
                        lGyroView.setText(rGyroView.getText().toString());
                        rGyroView.setText(tmp);

                        Toast.makeText(mainContext, R.string.pressure_side_switch_msg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        }
    };
}
