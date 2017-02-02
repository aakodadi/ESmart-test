package test.esmart.com.esmart_test;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import test.esmart.com.esmart_test.model.Device;
import test.esmart.com.esmart_test.model.WifiSignal;

public interface RemoteInterface {

    @GET("devices/{device_id}")
    Call<Device> getDevice(@Path("device_id") String deviceId);

    @GET("devices")
    Call<List<Device>> getDevices();

    @POST("devices")
    Call<Device> createDevice(@Body Device device);

    @GET("wifi_signals/{id}")
    Call<WifiSignal> getWifiSignal(@Path("id") long id);

    @GET("wifi_signals")
    Call<List<WifiSignal>> getWifiSignals();

    @GET("devices/{device_id}/wifi_signals")
    Call<List<WifiSignal>> getDeviceWifiSignals(@Path("device_id") String deviceId);

    @POST("devices/{device_id}/wifi_signals")
    Call<WifiSignal> createDeviceWifiSignal(@Path("device_id") String deviceId,
                                            @Body WifiSignal wifiSignal);
}
