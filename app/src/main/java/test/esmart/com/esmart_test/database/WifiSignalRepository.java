package test.esmart.com.esmart_test.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import test.esmart.com.esmart_test.model.WifiSignal;

public class WifiSignalRepository {

    private static final String TAG = "WifiSignalRepository";

    private Context context;

    private static WifiSignalDbHelper mWifiSignalDbHelper = null;

    private static SQLiteDatabase readableDatabase = null;
    private static SQLiteDatabase writableDatabase = null;

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    private static final String[] projection = {
            WifiSignalDatabase.WifiSignalColumns._ID,
            WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_RSSI,
            WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_SSID
    };

    // How you want the results sorted in the resulting Cursor
    private static final String sortOrder =
            WifiSignalDatabase.WifiSignalColumns._ID + " DESC";

    // Select by id
    private static final String selection = WifiSignalDatabase.WifiSignalColumns._ID + " = ?";

    public WifiSignalRepository(Context context) {
        this.context = context;
        if (mWifiSignalDbHelper == null) {
            mWifiSignalDbHelper = new WifiSignalDbHelper(context);
            readableDatabase = mWifiSignalDbHelper.getReadableDatabase();
            writableDatabase = mWifiSignalDbHelper.getWritableDatabase();
        }
    }

    public List<WifiSignal> getAllWifiSignals() {
        List<WifiSignal> list = new ArrayList<>();
        Cursor cursor = cursorForAll();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(WifiSignalDatabase.WifiSignalColumns._ID)
            );
            String SSID = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_SSID)
            );
            float RSSI = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(
                            WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_RSSI)
            );
            WifiSignal wifiSignal = new WifiSignal(id, RSSI, SSID);
            list.add(wifiSignal);
        }
        return list;
    }

    public void saveWifiSignal(WifiSignal wifiSignal) {

        ContentValues values = new ContentValues();

        values.put(WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_RSSI, wifiSignal.getRSSI());
        values.put(WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_SSID, wifiSignal.getSSID());

        writableDatabase.insert(WifiSignalDatabase.WifiSignalColumns.TABLE_NAME, null, values);
    }

    public void deleteById (long id) {
        String[] selectionArgs = {String.valueOf(id)};
        writableDatabase.delete(WifiSignalDatabase.WifiSignalColumns.TABLE_NAME,
                selection,
                selectionArgs);
    }

    public static void close() {
        if (mWifiSignalDbHelper != null) {
            mWifiSignalDbHelper.close();
            mWifiSignalDbHelper = null;
        }
    }

    private static Cursor cursorForAll() {
        return readableDatabase.query(
                WifiSignalDatabase.WifiSignalColumns.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }
}
