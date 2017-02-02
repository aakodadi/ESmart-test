package test.esmart.com.esmart_test;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ColorFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import test.esmart.com.esmart_test.model.Device;
import test.esmart.com.esmart_test.model.WifiSignal;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private static final int[] mColors = ColorTemplate.VORDIPLOM_COLORS;

    private LineChart mChart;

    private WifiScanService mWifiScanService;
    private boolean mWifiScanServiceBound = false;

    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 0;
    /*
     * List of required permissions
     */
    private static String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkRequiredPermissions()) {
            requestRequiredPermissions();
        }

        mChart = (LineChart) findViewById(R.id.chart);

        // enable logs
        mChart.setLogEnabled(true);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable hardware acceleration
        mChart.setHardwareAccelerationEnabled(true);

        // set empty data to chart
        mChart.setData(new LineData());
        mChart.notifyDataSetChanged();
        mChart.invalidate();

    }


    private void requestRequiredPermissions() {
        ActivityCompat.requestPermissions(this,
                REQUIRED_PERMISSIONS,
                REQUIRED_PERMISSIONS_REQUEST_CODE);
        //TODO Check if explanation is needed
    }

    /*
     * Check for required permissions
     */
    private boolean checkRequiredPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this,
                    permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUIRED_PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    bindWifiScanService();
                    startRemoteAPIService();

                } else {
                    // TODO
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkRequiredPermissions()) {
            bindWifiScanService();
            startRemoteAPIService();
        }
    }

    // If a new SSID has appeared this method adds it to the chart
    private void updateLineData(Map<String, List<Entry>> wifiData) {
        LineData data = mChart.getLineData();
        for (Map.Entry<String, List<Entry>> element : wifiData.entrySet()) {
            String SSID = element.getKey();
            List<Entry> list = element.getValue();
            LineDataSet oldLineDataSet = (LineDataSet) data.getDataSetByLabel(SSID, false);
            if (oldLineDataSet == null) {
                int color = mColors[data.getDataSetCount() % mColors.length];
                LineDataSet newLineDataSet = new LineDataSet(list, SSID);
                newLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                newLineDataSet.setDrawCircles(false);
                newLineDataSet.setColor(color);
                data.addDataSet(newLineDataSet);
            } else {
                oldLineDataSet.notifyDataSetChanged();
            }
        }
        data.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        private static final String TAG = "ServiceConnection";

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WifiScanService.WifiScanBinder mWifiScanBinder =
                    (WifiScanService.WifiScanBinder) service;
            mWifiScanService = mWifiScanBinder.getService();
            mWifiScanService.setOnScanFinishedListener(new WifiScanService
                    .OnScanFinishedListener() {
                @Override
                public void onScanFinished(final Map<String, List<Entry>> wifiData, List<WifiSignal> signals) {
                    final Map<String, List<Entry>> data = wifiData;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLineData(data);
                        }
                    });
                    Log.d(TAG, "Data: " + wifiData);
                }
            });
            mWifiScanService.start();
            mWifiScanServiceBound = true;
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWifiScanServiceBound = false;
            Log.e(TAG, "onServiceDisconnected");
        }
    };

    private void bindWifiScanService() {
        Intent intent = new Intent(this, WifiScanService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Service bound");
    }

    private void unBindWifiScanService() {
        if (mWifiScanServiceBound) {
            unbindService(mConnection);
            mWifiScanServiceBound = false;
            Log.d(TAG, "Service unbound");
        }
    }

    private void startRemoteAPIService() {
        Intent intent = new Intent(this, RemoteAPIService.class);
        startService(intent);
    }

    private void stopRemoteAPIService() {
        Intent intent = new Intent(this, RemoteAPIService.class);
        stopService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unBindWifiScanService();
        stopRemoteAPIService();
    }
}
