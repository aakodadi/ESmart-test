package test.esmart.com.esmart_test.model;

import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("id")
    long id;

    @SerializedName("device_id")
    String deviceId;

    public Device(String deviceId) {
        this.deviceId = deviceId;
    }

    public Device(long id, String deviceId) {
        this.id = id;
        this.deviceId = deviceId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
