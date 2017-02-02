package test.esmart.com.esmart_test.model;

import com.google.gson.annotations.SerializedName;

public class WifiSignal {

    @SerializedName("id")
    long id;

    @SerializedName("RSSI")
    float RSSI;

    @SerializedName("SSID")
    String SSID;

    public WifiSignal(long id, float RSSI, String SSID) {
        this.id = id;
        this.RSSI = RSSI;
        this.SSID = SSID;
    }

    public WifiSignal(float RSSI, String SSID) {
        this.RSSI = RSSI;
        this.SSID = SSID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getRSSI() {
        return RSSI;
    }

    public void setRSSI(float RSSI) {
        this.RSSI = RSSI;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }
}
