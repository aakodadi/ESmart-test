package test.esmart.com.esmart_test.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WifiSignalDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_WIFI_SIGNALS =
            "CREATE TABLE " + WifiSignalDatabase.WifiSignalColumns.TABLE_NAME + " (" +
                    WifiSignalDatabase.WifiSignalColumns._ID + " INTEGER PRIMARY KEY," +
                    WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_RSSI + " REAL," +
                    WifiSignalDatabase.WifiSignalColumns.COLUMN_NAME_SSID + " TEXT)";

    private static final String SQL_DELETE_WIFI_SIGNALS =
            "DROP TABLE IF EXISTS " + WifiSignalDatabase.WifiSignalColumns.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WifiSignal.db";

    public WifiSignalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_WIFI_SIGNALS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_WIFI_SIGNALS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
