package test.esmart.com.esmart_test.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class WifiSignalSerializer implements JsonSerializer<WifiSignal> {

    @Override
    public JsonElement serialize(WifiSignal src, Type typeOfSrc,
                                 JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        JsonObject wifiSignal = new JsonObject();
        wifiSignal.addProperty("SSID", src.getSSID());
        wifiSignal.addProperty("RSSI", src.getRSSI());

        obj.add("wifi_signal", wifiSignal);

        return obj;
    }
}
