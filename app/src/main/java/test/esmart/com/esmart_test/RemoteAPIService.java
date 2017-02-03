package test.esmart.com.esmart_test;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import test.esmart.com.esmart_test.database.WifiSignalRepository;
import test.esmart.com.esmart_test.model.Device;
import test.esmart.com.esmart_test.model.WifiSignal;
import test.esmart.com.esmart_test.model.WifiSignalSerializer;

public class RemoteAPIService extends Service {

    private static final String TAG = "RemoteAPIService";
    private static final String BASE_URL = "https://esmart-test-api.herokuapp.com";

    private Retrofit mRetrofit;
    private RemoteInterface mRemoteInterface;
    private Device mDevice;

    private WifiSignalRepository mWifiSignalRepository;

    private ConnectivityManager mConnectivityManager;

    private boolean mWifiScanServiceBound = false;
    private WifiScanService mWifiScanService;

    private Thread mThread;
    private boolean running = false;

    @Override
    public void onCreate() {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(WifiSignal.class, new WifiSignalSerializer())
                .create();

        mConnectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mRemoteInterface = mRetrofit.create(RemoteInterface.class);

        String deviceId = Settings.Secure.getString(
                getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mDevice = new Device(deviceId);

        mWifiSignalRepository = new WifiSignalRepository(this);

        mThread = new Thread() {
            @Override
            public void run() {
                while (running) {
                    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnectedOrConnecting();
                    if (isConnected) {
                        saveToRemoteFromLocal();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

    }

    public void start() {
        if(!running) {
            running = true;
            mThread.start();
        }
    }

    public void stop() {
        if (running) {
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        bindWifiScanService();
        start();
        return START_STICKY;
    }

    private void saveLocale(List<WifiSignal> signals) {
        for (WifiSignal signal : signals) {
            saveLocale(signal);
        }
    }

    private void saveLocale(WifiSignal signal) {
        mWifiSignalRepository.saveWifiSignal(signal);
    }

    private void saveRemote(final List<WifiSignal> signals) {

        Call<Device> createDeviceCall = mRemoteInterface.createDevice(mDevice);

        createDeviceCall.enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                Log.d(TAG, "saveRemote:createDeviceCall:onResponse");
                for (final WifiSignal signal : signals) {
                    Call<WifiSignal> createSignalCall =
                            mRemoteInterface.createDeviceWifiSignal(mDevice.getDeviceId(), signal);
                    createSignalCall.enqueue(new Callback<WifiSignal>() {
                        @Override
                        public void onResponse(Call<WifiSignal> call, Response<WifiSignal> response) {
                            // Do nothing
                            Log.d(TAG, "saveRemote:createSignalCall:onResponse");
                        }

                        @Override
                        public void onFailure(Call<WifiSignal> call, Throwable t) {
                            saveLocale(signal);
                            Log.e(TAG, "saveRemote:createSignalCall:onFailure", t);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                saveLocale(signals);
                Log.e(TAG, "saveRemote:createDeviceCall:onFailure", t);
            }
        });

    }

    private void saveToRemoteFromLocal() {
        final List<WifiSignal> signals = mWifiSignalRepository.getAllWifiSignals();
        if(signals.size() > 0) {

            Call<Device> createDeviceCall = mRemoteInterface.createDevice(mDevice);

            createDeviceCall.enqueue(new Callback<Device>() {
                @Override
                public void onResponse(Call<Device> call, Response<Device> response) {
                    Log.d(TAG, "saveToRemoteFromLocal:createDeviceCall:onResponse");
                    for (final WifiSignal signal : signals) {
                        Call<WifiSignal> createSignalCall =
                                mRemoteInterface.createDeviceWifiSignal(mDevice.getDeviceId(), signal);
                        createSignalCall.enqueue(new Callback<WifiSignal>() {
                            @Override
                            public void onResponse(Call<WifiSignal> call, Response<WifiSignal> response) {
                                Log.d(TAG, "saveToRemoteFromLocal:createSignalCall:onResponse");
                                mWifiSignalRepository.deleteById(signal.getId());
                            }

                            @Override
                            public void onFailure(Call<WifiSignal> call, Throwable t) {
                                // Do nothing
                                Log.e(TAG, "saveToRemoteFromLocal:createSignalCall:onFailure", t);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<Device> call, Throwable t) {
                    // Do nothing
                    Log.e(TAG, "saveToRemoteFromLocal:createDeviceCall:onFailure", t);
                }
            });

        }
    }

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
                    NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnectedOrConnecting();
                    if (isConnected) {
                        saveRemote(signals);
                    } else {
                        saveLocale(signals);
                    }
                }
            });
            mWifiScanServiceBound = true;
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWifiScanServiceBound = false;
            Log.e(TAG, "onServiceDisconnected");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unBindWifiScanService();
        stop();
        mWifiSignalRepository.close();
    }
}
