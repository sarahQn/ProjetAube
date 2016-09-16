package com.aykow.aube.ble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by sarah on 20/06/2016.
 */
public class NetworkStateHandler extends BroadcastReceiver {
    public String TAG ="NETWORK";
    public static boolean isConnected = false;

    private static NetworkStateHandler networkStateHandlerInstance = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received notification about network status");
        isConnected = isNetworkAvailable(context);
    }

    public NetworkStateHandler(Context context){    }

    public static synchronized NetworkStateHandler getInstance(Context context){
        if(networkStateHandlerInstance == null){
            networkStateHandlerInstance = new NetworkStateHandler(context.getApplicationContext());
        }
        return networkStateHandlerInstance;
    }

    public boolean isNetworkAvailable(Context context){

        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                       /* if(!isConnected){

                        }*/
                        Log.v(TAG, "You are connected to Internet!");
                        return true;
                    }
                }
            }
        }
        Log.v(TAG, "You are not connected to Internet!");
        return false;
    }
}
