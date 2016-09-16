package com.aykow.aube.ble.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.aykow.aube.ble.DatabaseHelper;
import com.aykow.aube.ble.NetworkStateHandler;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.SyncController;
import com.aykow.aube.ble.UserLocalStore;

public class InterMainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PREFERENCES = "MyPrefs" ;
    public static final String SPUSERDETAILS = "userDetails";
    public static final String CheckForDeviceImport = "checkForDeviceImport";
    public static final String CheckForDeviceExport = "checkForDeviceExport";
    public static final String CheckForAQImport = "checkForAQImport";
    public static final String CheckForAQExport = "checkForAQExport";
    public static final String IdSelectedAube = "idSelectedAube";
    public static final String AirQuality ="airQuality";
    public static final String BtState = "btState";
    public static final String PrevBtState = "prevBtState";
    public static final String BtAddress ="btAddress";

    public static final String DISCONNECTED = "disconnected";

    SharedPreferences sharedpreferences;
    SharedPreferences spUserDetails;

    public String idUser;


    UserLocalStore userLocalStore;
    EditText etHello;
    Button btnLogout;
    Context context;
    SyncController syncController;
    DatabaseHelper dbHelper;
    NetworkStateHandler networkStateHandler;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("INTERMAINACTIVITY", "onCreate");
        setContentView(R.layout.activity_inter_main);
        //------------------------------------------------------------------------------------
        // When application open we make false all checks for DB import and export data
        // Will be used to wait until synchronizing finish then we will show the main activity
        //-------------------------------------------------------------------------------------
        sharedpreferences = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        spUserDetails = getApplicationContext().getSharedPreferences(SPUSERDETAILS, MODE_PRIVATE);
        idUser = spUserDetails.getString("idUser",null);
        Log.d("INTERMAINACTIVITY", "Shared preferences idUser :"+idUser );
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(CheckForDeviceImport, false);
        editor.putBoolean(CheckForDeviceExport, false);
        editor.putInt(IdSelectedAube,0);
        editor.putInt(AirQuality, -1);
        editor.putString(BtState, DISCONNECTED);
        editor.putString(PrevBtState, DISCONNECTED);
        editor.putString(BtAddress,null);
        //---editor.putBoolean(CheckForAQImport, false);
        //---editor.putBoolean(CheckForAQExport, false);
        editor.commit();

        //progressBar = (ProgressBar)findViewById(R.id.pb_search);
        //progressBar.getIndeterminateDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        userLocalStore = new UserLocalStore(this);
        //btnLogout.setOnClickListener(this);
        context = getApplicationContext();
        dbHelper= DatabaseHelper.getDbHelperInstance(context);
        networkStateHandler = NetworkStateHandler.getInstance(context);
        //syncController = new SyncController(dbHelper);
        syncController = SyncController.getInstance(this,dbHelper);

    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("INTERMAINACTIVITY", "onStart");
        if(authenticate()){

            if(networkStateHandler.isNetworkAvailable(context))
            {
                SharedPreferences.Editor editor = sharedpreferences.edit();

                Log.d("INTERMAINACTIVITY","INTERNET STATUS ------------> CONNECTED" );

                //------------------ Devices --------------------

                if(syncController.isDeviceNeedExport()){
                    Log.d("INTERMAINACTIVITY", "SYNC DEVICE STATUS ---------------> 1- Devices table need to be export to SQL DB");
                    //------------------------>  dbHelper.exportDevicesFromSQLiteToSQLDB();
                    dbHelper.exportDevicesFromSQLiteToSQLDB(new DatabaseHelper.DeviceExportCallback() {
                        @Override
                        public void onFinishExportDevice(boolean isFinished) {
                            if(isFinished)
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForDeviceExport, true);
                                editor.commit();
                                Log.d("EXPORT DEVICE", "finished");
                            }
                            else
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForDeviceExport, false);
                                editor.commit();
                                Log.d("EXPORT DEVICE", "finished");
                            }
                        }
                    });
                }
                else
                {
                    Log.d("SYNC DEVICE STATUS", "---------------> 1- Devices table DON'T need to be export to SQL DB");
                    editor.putBoolean(CheckForDeviceExport, true);
                }

                if(syncController.isNeedImportDevices())
                {
                    Log.d("SYNC DEVICE STATUS", "---------------> 2- Devices table need to be import from SQL DB ");
                    //------------------------>  dbHelper.importDevicesFromSQLDBToSQLite();

                    dbHelper.importDevicesFromSQLDBToSQLite(new DatabaseHelper.DeviceImportCallback() {
                        @Override
                        public void onFinishImportDevice(boolean isFinished) {
                            if(isFinished)
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForDeviceImport, true);
                                editor.commit();
                                Log.d("IMPORT DEVICE", "finished");
                            }
                            else
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForDeviceImport, false);
                                editor.commit();
                                Log.d("IMPORT DEVICE", "finished");
                            }
                        }
                    });
                }
                else
                {
                    Log.d("SYNC DEVICE STATUS", "---------------> 2- Devices table DON'T need to be import from DB SQL ");
                    editor.putBoolean(CheckForDeviceImport, true);
                }
                //----------------------------------------------------
                //-------------- AQ Data -----------------------------
                //----------------------------------------------------

               if(syncController.isAQDataNeedExport())
                {
                    Log.d("SYNC AQ STATUS", "---------------> 3- AQ table need to be exported to SQL DB");
                    //------------------------>  dbHelper.exportAQdataFromSQLiteToSQLDB();
                    dbHelper.exportAQdataFromSQLiteToSQLDB(new DatabaseHelper.AqExportCallback(){
                        @Override
                        public void onFinishExportAq(boolean isFinished) {
                            if(isFinished)
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForAQExport, true);
                                editor.commit();
                                Log.d("EXPORT AQ", "finished");
                            }
                            else
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForAQExport, false);
                                editor.commit();
                                Log.d("EXPORT AQ", "finished");
                            }
                        }
                    });
                }
               else
                {
                    Log.d("SYNC AQ STATUS", "---------------> 3- AQ DON'T table need to be exported to SQL DB");
                    editor.putBoolean(CheckForAQExport, true);
                }
                if(syncController.isAQDataNeedImport())
                {
                    Log.d("SYNC AQ STATUS", "---------------> 3- AQ table need to be imported from SQL DB");
                    //------------------------>  dbHelper.importAQdataFromSQLDbToSQLite();
                    dbHelper.importAQdataFromSQLDbToSQLite(new DatabaseHelper.AqImportCallback(){
                        @Override
                        public void onFinishImportAq(boolean isFinished) {
                            if(isFinished)
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForAQImport, true);
                                editor.commit();
                                Log.d("IMPORT AQ", "finished");
                            }
                            else
                            {
                                sharedpreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putBoolean(CheckForAQImport, false);
                                editor.commit();
                                Log.d("IMPORT AQ", "finished");
                            }
                        }
                    });
                }
                else
                {
                    Log.d("SYNC AQ STATUS", "---------------> 3- AQ DON'T table need to be imported from SQL DB");
                    editor.putBoolean(CheckForAQImport, true);
                }

                editor.commit();

                new Thread(new Runnable() {
                    public void run() {
                        int i=0;

                        //Log.e("SHARED PREFERENCES","import devices done: " + sharedpreferences.getBoolean(CheckForDeviceImport,false) + " export devices done: " + sharedpreferences.getBoolean(CheckForDeviceExport,false));// +" import AQ: " + sharedpreferences.getBoolean(CheckForAQImport,false) + "export AQ: " + sharedpreferences.getBoolean(CheckForAQExport,false));

                        while (!sharedpreferences.getBoolean(CheckForDeviceImport,false) || !sharedpreferences.getBoolean(CheckForDeviceExport,false) || !sharedpreferences.getBoolean(CheckForAQExport,false)|| !sharedpreferences.getBoolean(CheckForAQImport,false))
                        {
                            sharedpreferences = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                            i++;
                            //Log.e("SHARED PREFERENCES", i +" import devices done : " + sharedpreferences.getBoolean(CheckForDeviceImport,false) + " export devices done : " + sharedpreferences.getBoolean(CheckForDeviceExport,false) +" import AQ done: " + sharedpreferences.getBoolean(CheckForAQImport,false) + " export AQ done : " + sharedpreferences.getBoolean(CheckForAQExport,false));
                            try {
                                //Thread.sleep(1000);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        Log.e("WAIT SYNC", "import device finished!");

                        displayApplication();

                    }
                }).start();


            }
            else{
                Log.d("INTERMAINACTIVITY","INTERNET STATUS------------> DISCONNECTED" );
                displayApplication();
            }



        }
        else{
            startActivity(new Intent(InterMainActivity.this, LoginActivity.class));
        }
    }

    private boolean authenticate(){
        return userLocalStore.getIsUserLoggedIn();
    }

    private void displayApplication(){
        startActivity(new Intent(InterMainActivity.this,MainActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            /*case R.id.btnLogout:
                userLocalStore.setIsUserLoggedIn(false);
                startActivity(new Intent(InterMainActivity.this,LoginActivity.class));
                break;*/
        }
    }



}
