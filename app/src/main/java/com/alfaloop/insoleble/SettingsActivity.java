package com.alfaloop.insoleble;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.alfaloop.insoleble.ble.config.BleConnConfig;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private Context context = this;
    private SharedPreferences sharedPref = null;
    private SharedPreferences.Editor editor = null;
    private EditText scanTimeEditText = null;
    private Switch autoScanSwitch = null;
    private Button saveButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sharedPref = context.getSharedPreferences(getString(R.string.pref_file_key),
                Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        initViewComponents();
    }

    @Override
    public void onBackPressed() {
        SettingsActivity.this.finish();
    }

    private void initViewComponents() {
        scanTimeEditText = (EditText)findViewById(R.id.scan_time_s_edittext);
        autoScanSwitch = (Switch)findViewById(R.id.auto_scan_switch);
        saveButton = (Button)findViewById(R.id.save_setting_button);

        scanTimeEditText.setText(String.valueOf(getScanTimeS()));
        autoScanSwitch.setChecked(isAutoScanSet());
        saveButton.setOnClickListener(saveButtonTask);
    }

    private View.OnClickListener saveButtonTask = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveScanTimeS();
            saveAutoScan();
            commit();
            toastResult();
        }
    };

    private void saveScanTimeS() {
        int result = Integer.valueOf(scanTimeEditText.getText().toString());
        editor.putInt(getString(R.string.pref_scan_time), result);
    }

    private void saveAutoScan() {
        boolean isChecked = autoScanSwitch.isChecked();
        int result = 0;
        if(isChecked)
            result = 1;
        editor.putInt(getString(R.string.pref_auto_scan), result);
    }

    private void commit() {
        editor.commit();
    }

    private void toastResult() {
        Toast.makeText(SettingsActivity.this, getString(R.string.title_save_success),
                Toast.LENGTH_SHORT).show();
    }

    private boolean isAutoScanSet() {
        int result = sharedPref.getInt(getString(R.string.pref_auto_scan), 0);
        if(result > 0)
            return true;
        else
            return false;
    }

    private int getScanTimeS() {
        return sharedPref.getInt(getString(R.string.pref_scan_time), BleConnConfig.SCAN_TIME_S);
    }
}
