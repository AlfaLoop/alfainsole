package com.alfaloop.insoleble.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.alfaloop.insoleble.MainActivity;
import com.alfaloop.insoleble.R;
import com.alfaloop.insoleble.ble.config.BleConnConfig;

import java.util.Timer;
import java.util.TimerTask;

public class InitViewFragment extends Fragment {
    private static final String TAG = InitViewFragment.class.getSimpleName();

    private Context mainContext = null;
    private int scanTimeS = BleConnConfig.SCAN_TIME_S;
    private ProgressBar psb = null;
    private Handler handler = null;
    private int progressValue = 0;
    private boolean isProcessing = false;
    private Timer timer = null;

    public InitViewFragment() {
        super();
        timer = new Timer();
    }

    public void setScanTimeS(int s) {
        this.scanTimeS = s;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.init_view_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        progressValue = 0;

        psb = (ProgressBar)view.findViewById(R.id.progress_bar);
        psb.setMax(scanTimeS);
        handler.post(new Runnable() {
            @Override
            public void run() {
                psb.setProgress(progressValue);
            }
        });
        psb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isProcessing)
                    ((MainActivity)mainContext).onRefresh();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainContext = context;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void startProcessing() {
        isProcessing = true;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(isProcessing) {
                                progressValue++;
                                psb.setProgress(progressValue);

                                if(progressValue >= scanTimeS) {
                                    isProcessing = false;
                                    timer.cancel();
                                }
                            }
                        }
                    });
                }
              },
            1000, 1000);
    }

    public void stopProcessing() {
        progressValue = 0;
        isProcessing = false;
        timer.cancel();
    }

    public boolean isProcessing() {
        return isProcessing;
    }
}
