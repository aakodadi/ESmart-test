package test.esmart.com.esmart_test.database;

import android.provider.BaseColumns;

public class WifiSignalDatabase {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private WifiSignalDatabase() {}

    /* Inner class that defines the table contents */
    public static class WifiSignalColumns implements BaseColumns {
        public static final String TABLE_NAME = "wifi_signal";
        public static final String COLUMN_NAME_SSID = "SSID";
        public static final String COLUMN_NAME_RSSI = "RSSI";
    }

}
