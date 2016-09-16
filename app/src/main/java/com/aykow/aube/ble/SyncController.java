package com.aykow.aube.ble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by sarah on 01/06/2016.
 */
public class SyncController{

    boolean isConnected = false;
    String TAG = "CheckNetworkStatus";

    DatabaseHelper dbHelper;// = new DatabaseHelper(context);



    private static  SyncController syncControllerInstance;

    public static synchronized SyncController getInstance(Context context, DatabaseHelper dbHelper){
        if(syncControllerInstance == null){
            syncControllerInstance = new SyncController(dbHelper);
        }
        return syncControllerInstance;
    }

    public SyncController(DatabaseHelper dbHelper){
        this.dbHelper = dbHelper;
    }


    public void synchRecordedDevices()
    {
        if(NetworkStateHandler.isConnected)
        {
            //String jsonFromSQLite = dbHelper.composeDeviceJSONfromSQLite();
            //Log.d("JSON SQLite", jsonFromSQLite);
            Log.d("SYNCH DEVICES", "network is oki and we will sync ");
        }
        else
        {
            Log.d("SYNCH DEVICES", "network is NOT oki and we will  NOT sync ");

        }

    }

    public boolean isDeviceNeedExport(){
        if(dbHelper.dbDeviceNeedExportCount()==0){
            return false;
        }
        else{
            return true;
        }
    }
    public boolean isAQDataNeedExport(){
        if(dbHelper.dbAQDataNeedExportCount()==0){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean isNeedImportDevices(){
        if(dbHelper.getCountDeviceSQLite() == 0){
            Log.d("SYNCONTROLLER", "SQLite is empty, need to import devices");
            return true;
        }
        else{
            return false;
        }

    }

    public boolean isAQDataNeedImport(){
        if(dbHelper.getCountAQdataSQLite() == 0){
            Log.d("SYNCONTROLLER", "SQLite is empty, need to import AQ Data");
            return true;
        }
        else{
            return false;
        }

    }




}
