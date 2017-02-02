package test.esmart.com.esmart_test;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import test.esmart.com.esmart_test.model.Device;
import test.esmart.com.esmart_test.model.WifiSignal;
import test.esmart.com.esmart_test.model.WifiSignalSerializer;

public class WifiScanService extends Service {

    private static final String TAG = "WifiScanService";
    private static final String BASE_URL = "https://esmart-test-api.herokuapp.com";

    private IBinder mBinder;
    private WifiManager mWifiManager;
    private Thread mThread;
    private boolean running;

    /*
     * permits clients to listen to scan finished event (Observer pattern)
     */
    private List<OnScanFinishedListener> onScanFinishedListenerList;

    /* a data structure holding wifi signal history
     * key = SSID (String)
     * value = list of Entry(counter, RSSI)
     */
    private Map<String, List<Entry>> wifiData;

    private static int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        wifiData = new HashMap<>();

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mThread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    scan();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Counter: " + counter);
                }
            }
        };

        onScanFinishedListenerList = new ArrayList<>();

        mBinder = new WifiScanBinder();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void start() {
        running = true;
        mThread.start();
    }

    public void stop() {
        running = false;
        while (mThread.isAlive()) {
            // Does nothing just waits for the thread to die
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void scan() {

        if (mWifiManager.isWifiEnabled()) {

            List<ScanResult> wifiList = mWifiManager.getScanResults();
            List<WifiSignal> signals = new ArrayList<>();

            for (ScanResult scanResult : wifiList) {

                int RSSI = scanResult.level;
                String SSID = scanResult.SSID;

                WifiSignal signal = new WifiSignal(RSSI, SSID);

                Entry entry = new Entry(counter * 5, RSSI);

                if (wifiData.containsKey(SSID)) {

                    wifiData.get(SSID).add(entry);

                } else {

                    List<Entry> list = new ArrayList<>();
                    list.add(entry);

                    wifiData.put(SSID, list);

                }
                signals.add(signal);

            }
            runOnScanFinishedListenerList(signals);
        }

        counter++;

    }

    public Map<String, List<Entry>> getWifiData() {
        return wifiData;
    }

    public void setOnScanFinishedListener(@Nullable OnScanFinishedListener listener) {
        if (listener != null) {
            this.onScanFinishedListenerList.add(listener);
        }
    }

    private void runOnScanFinishedListenerList(List<WifiSignal> signals) {
        for (OnScanFinishedListener l : onScanFinishedListenerList) {
            l.onScanFinished(wifiData, signals);
        }
    }

    public class WifiScanBinder extends Binder {
        WifiScanService getService() {
            return WifiScanService.this;
        }
    }

    public interface OnScanFinishedListener {
        void onScanFinished(Map<String, List<Entry>> wifiData, List<WifiSignal> signals);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }
}
