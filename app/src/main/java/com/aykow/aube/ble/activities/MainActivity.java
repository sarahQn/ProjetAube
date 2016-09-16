package com.aykow.aube.ble.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aykow.aube.ble.BluetoothLeService;
import com.aykow.aube.ble.DatabaseHelper;
import com.aykow.aube.ble.Fragments.FragmentDevices;
import com.aykow.aube.ble.Fragments.FragmentHelp;
import com.aykow.aube.ble.Fragments.FragmentHistory;
import com.aykow.aube.ble.Fragments.FragmentHome;
import com.aykow.aube.ble.Fragments.FragmentOutdoor;
import com.aykow.aube.ble.Fragments.FragmentSettings;
import com.aykow.aube.ble.NetworkStateHandler;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.UserDevicesDB;
import com.aykow.aube.ble.UserLocalStore;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    UserLocalStore userLocalStore;
    DatabaseHelper dbHelper;
    List<UserDevicesDB> userDevicesDBList;
    List<UserDevicesDB> userHomeDevicesDBList;
    List<UserDevicesDB> userCarDevicesDBList;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FragmentManager fragmentManager;
    Fragment fragment;
    Fragment prevFragment;
    FragmentHome fragmentHome;
    FragmentOutdoor fragmentOutdoor;
    FragmentHistory fragmentHistory;
    FragmentDevices fragmentDevices;
    FragmentSettings fragmentSettings;
    FragmentHelp fragmentHelp;
    MenuItem homeMenu, devicesMenu, outdoorMenu, historyMenu, settingsMenu, helpMenu, logoutMenu;
    private BluetoothLeService mBluetoothLeService;

    boolean mConnected = false;
    boolean isAubeConnected = false;
    String CONNECTED = "connected";
    String CONNECTING = "connecting";
    String DISCONNECTING = "disconnecting";
    String DISCONNECTED = "disconnected";
    int nbNotification = 0;
    String TAG = "MainActivityLog";
    int REQUEST_ENABLE_BT = 1;
    boolean isBluetoothAdapterInitialized = false;
    private String mDeviceAddress;

    NetworkStateHandler networkStateHandler;

    String idUser;
    SharedPreferences spUserLocalDB;
    public static final String SP_NAME="userDetails";

    public double currentTemperature=20;
    public int currentAirQ;

    public static final String PREFERENCES = "MyPrefs" ;
    public static SharedPreferences sharedpreferences;
    public static final String AirQuality = "airQuality";
    public static final String IdSelectedAube = "idSelectedAube";

    public static final String BtState = "btState";
    public static final String PrevBtState = "prevBtState";
    public static final String BtAddress ="btAddress";

   //FragmentVehicle fragmentVehicle;
    //MenuItem vehicleMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("MAIN ACTIVITY", "-------> OnCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //----------------------------------------------------------------------------
        //---------  Added from back up ----------------------------------------------
        //----------------------------------------------------------------------------
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d("MainActivity", "bind service");
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //---------------------------------------------------------------------------
        sharedpreferences = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        spUserLocalDB = getApplicationContext().getSharedPreferences(SP_NAME, 0);
        idUser = spUserLocalDB.getString("idUser", "");
        networkStateHandler = NetworkStateHandler.getInstance(this);

        userLocalStore = new UserLocalStore(this);

        dbHelper = DatabaseHelper.getDbHelperInstance(this);

        userDevicesDBList = dbHelper.getAllDevicesInDbSQLite();
        userHomeDevicesDBList = dbHelper.getAllHomeDevicesInDdSQLite();
        userCarDevicesDBList = dbHelper.getAllCarDevicesInDbSQLite();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new android.support.v7.app.ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        fragmentManager = getSupportFragmentManager();

        fragmentHome = new FragmentHome();
        //fragmentVehicle = new FragmentVehicle();
        fragmentOutdoor = new FragmentOutdoor();
        fragmentHistory = new FragmentHistory();
        fragmentDevices = new FragmentDevices();
        fragmentSettings = new FragmentSettings();
        fragmentHelp = new FragmentHelp();

        if(isAubeRecorded()){
            navigationView.setCheckedItem(R.id.nav_home);
            fragment = fragmentHome;

        }else{
            navigationView.setCheckedItem(R.id.nav_devices);
            fragment = fragmentDevices;
        }

        fragmentManager.beginTransaction().replace(R.id.rlContent,fragment).commit();


        homeMenu = (MenuItem) findViewById(R.id.nav_home);
        //vehicleMenu = (MenuItem) findViewById(R.id.nav_vehicle);
        outdoorMenu = (MenuItem) findViewById(R.id.nav_outdoor);
        devicesMenu = (MenuItem) findViewById(R.id.nav_devices);
        historyMenu = (MenuItem) findViewById(R.id.nav_history);
        settingsMenu = (MenuItem) findViewById(R.id.nav_settings);
        helpMenu = (MenuItem) findViewById(R.id.nav_help);
        logoutMenu = (MenuItem) findViewById(R.id.nav_logout);


        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //Register Receiver for when Bluetooth enabled or disabled
        //
        //   =====> commented (using filter from backup)
        //
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        /*IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);*/
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //Register Receiver for the Service
        //
        //  ======> Commented (not used on backup)
        //
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        /*final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_TEMP);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_AIR);
        registerReceiver(mGattUpdateReceiver, intentFilter);*/
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // Bind to the Service
        //
        //  ====> Commented (using gattServiceIntent from backup
        //
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        /*Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d("BluetoothLeSerice", "bind service");
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);*/
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    }

    //----------------------------------------------------------------------------------------------
    //
    //   ===> Ok , added from back up
    //
    //----------------------------------------------------------------------------------------------
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_TEMP);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_AIR);
        return intentFilter;
    }

    //--------------------------------------------------------------------------
    //
    // OKI , the same one that in backup
    //
    //--------------------------------------------------------------------------
    private boolean isAubeRecorded() {
        if(userDevicesDBList.size()>0){
            return true;
        }
        return false;
    }

    //----------------------------------------------------------------------------------
    //  handle which fragment is shown
    //
    //-----------------------------------------------------------------------------------
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

        if (id == R.id.nav_home) {
                fragment = fragmentHome;
           /* } else if (id == R.id.nav_vehicle) {
                fragment = fragmentVehicle;*/
        } else if (id == R.id.nav_outdoor) {
                fragment = fragmentOutdoor;
        } else if (id == R.id.nav_history) {
                fragment = fragmentHistory;
        } else if (id == R.id.nav_settings) {
                fragment = fragmentSettings;
        } else if (id == R.id.nav_help) {
                fragment = fragmentHelp;
        }else if(id== R.id.nav_logout){
                userLocalStore.setIsUserLoggedIn(false);
                Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //finish();
                startActivity(loginIntent);
        }else{
                fragment = fragmentDevices;
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.rlContent,fragment).commit();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
        }

    //-----------------------------------------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

        unregisterReceiver(mReceiver);
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    //Used in Fragment Home
    public List<UserDevicesDB> getUserDevicesDBList(){
        return userDevicesDBList;
    }

    //Used in Fragment Home
    public List<UserDevicesDB> getHomeUserDevicesDBList(){
        return userHomeDevicesDBList;
    }

    //Used in Fragment Home
    public List<UserDevicesDB> getCarUserDevicesDBList(){
        return userCarDevicesDBList;
    }


    //-----------------------------------------------------------------------------------------
    //BroadcastReceiver to receive notification when Bluetooth on PDA is enable or disable
    //
    //
    //  ===> OK
    //------------------------------------------------------------------------------------------
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.e("BluetoothAdapter", "State ON try to connect to : "+ sharedpreferences.getString(BtAddress,null)+" and id item selected is : "+ sharedpreferences.getInt(IdSelectedAube,-1));
                        connectToDevice(sharedpreferences.getString(BtAddress,null));

                        break;
                    case BluetoothAdapter.STATE_OFF:
                        resetGatt();
                        Log.e("BluetoothAdapter", "State OFF");
                        break;
                }
            }
        }
    };


    //BroadcastReceicer
    //---------------------------------------------------------------------------------------------------------------
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE_TEMP: received data from the Temperature sensor.  This can be a result of read or notification operations.
    // ACTION_DATA_AVAILABLE_AIR: received data from the Air Quality sensor.  This can be a result of read or notification operations.
    //
    //
    //  =====> OKI (added from backup)
    //
    //-----------------------------------------------------------------------------------------------------------------
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(BluetoothLeService.ACTION_GATT_CONNECTING.equals(action)){
            }

            else if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                mConnected = true;
                String prevState = sharedpreferences.getString(BtState,null);
                String currentState =  CONNECTED;
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(PrevBtState, prevState);
                editor.putString(BtState, currentState);
                editor.commit();
                try {
                    fragmentHome.displayStateConnection(prevState,currentState);
                }catch(Exception e){}

                isAubeConnected = true;
                Log.d("BROADCAST_RECEIVER", "-------> actionIntent CONNECTED");
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTING.equals(action)){

            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                String prevState = sharedpreferences.getString(BtState,null);
                String currentState =  DISCONNECTED;
                sharedpreferences = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(PrevBtState, prevState);
                editor.putString(BtState, currentState);
                editor.putInt(AirQuality, -1);
                editor.commit();
                try {
                    fragmentHome.displayStateConnection(prevState, currentState);
                }catch (Exception e){}
                isAubeConnected = false;
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                //Show all the supported services and characteristics on the user interface.
                //--------> displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.d("BROADCAST_RECEIVER", "-------> SERVICES_DISCOVERED");
            }

            else if (BluetoothLeService.ACTION_DATA_AVAILABLE_TEMP.equals(action)){
                currentTemperature= intent.getDoubleExtra(BluetoothLeService.EXTRA_DATA_TEMP,0);
                try {
                    fragmentHome.displayTemperature(currentTemperature);
                }catch(Exception e){}
                //Log.d("BROADCAST_RECEIVER", "-------> DATA_AVAILABLE from TEMP");


            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE_AIR.equals(action)){

                currentAirQ = (int) intent.getDoubleExtra(BluetoothLeService.EXTRA_DATA_AIR,0);
                try{
                    currentAirQ =(int) (currentAirQ /(1.9732-(81.8*0.001*currentTemperature)+(2*0.001*currentTemperature*currentTemperature)-(20*0.000001*currentTemperature*currentTemperature*currentTemperature)));
                }
                catch(Exception e){}

                int aqValue = (int)currentAirQ;


                if(currentAirQ>=0 && currentAirQ <= 78)
                {
                    aqValue = (int) ((0.0007 * currentAirQ * currentAirQ * currentAirQ) - (0.0105 * currentAirQ * currentAirQ) + (0.7426 * currentAirQ) - 0.2621);//(air*3.85);

                }
                else if (currentAirQ>78)
                {
                    aqValue = (int) ((currentAirQ *0.83)+261.3);
                }
                sharedpreferences = getApplicationContext().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(AirQuality, aqValue);
                editor.commit();
                try {
                    fragmentHome.displayDataAir(aqValue);
                }catch (Exception e){};
                long tsLong = System.currentTimeMillis();
                int ts = (int)(tsLong/1000);
                //Log.d("BROADCAST_RECEIVER", "-------> DATA_AVAILABLE from AIR");
                //----------------------------------------------------------------------------------
                //Check if network available ,
                //      if yes, insert data into DB SQL ,
                //          if success, insert data in DB SQLITE with sync => yes
                //          else, insert data in DB SQLITE with sync => non
                //      else, insert data into DB SQLITE with syn => no
                //----------------------------------------------------------------------------------
                if(networkStateHandler.isNetworkAvailable(getApplicationContext())) {
                    Log.w("MainActivity", "insert AQ - device address: "+mDeviceAddress);
                    final int finalAqValue = aqValue;
                    final int finalTs = ts;


                    dbHelper.insertAQintoDbSQL(mDeviceAddress, aqValue, ts, idUser, new DatabaseHelper.InsertAQinSqlDbCallback() {
                        final
                        @Override
                        public void onFinishInsertAQinSQL(boolean result) {
                            boolean isInserted = result;
                            if(isInserted){
                                Log.d("INSERT AQ DB", "-----> SUCCESSFUL!!!");
                                dbHelper.insertAQData(mDeviceAddress, finalAqValue,finalTs,idUser,"yes");
                            }
                            else{
                                Log.d("INSERT AQ DB", "-----> FAILED!!!");
                                dbHelper.insertAQData(mDeviceAddress, finalAqValue,finalTs,idUser,"no");//<-----------------------------
                            }
                        }
                    });


                }
                else{
                    dbHelper.insertAQData(mDeviceAddress,aqValue,ts,idUser,"no");
                }
            }

            //Bundle  bundle = new Bundle();
            //bundle.putString("DATA",action);
            //fragmentHome.setArguments(bundle);
            //---------------------fragmentHome.getView().findViewById(R.id.tv_status_aube);
        }
    };

    //--------------------------------------------------------------------------------------------
    // To manage Service lifecycle
    //
    //  ======> OK (the same from backup and from this one)
    //
    //--------------------------------------------------------------------------------------------
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("---> SERVICE_CONNECTION", "onServiceConnected mServiceConnection initialisation");
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            mBluetoothLeService = binder.getService();


            enableBtIfNotAndConnect();
            //initializeAndConnect(mBluetoothLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("---> SERVICE_CONNECTION", "onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    //-------------------------------------------------------------------
    // Check first if Bluetooth is enabled with onActivityResult
    //
    //  ======> OKI ( added from backup)
    //
    //---------------------------------------------------------------------
    public void enableBtIfNotAndConnect(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }
        else{
            initializeAndConnect(mBluetoothLeService);
        }
    }

    //---------------------------------------------------------------------------
    //  Initialize Service
    //
    //  ===> Oki (added from back up)
    //
    //---------------------------------------------------------------------------
    public void initializeAndConnect(BluetoothLeService mBluetoothLeService){
        if(mBluetoothLeService != null) {
            isBluetoothAdapterInitialized = mBluetoothLeService.initialize();
            if (!isBluetoothAdapterInitialized) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            else {
                Log.d(TAG, "mBluetoothLeService is initialized");
            }

            if (mBluetoothLeService != null && !isAubeConnected && mDeviceAddress != null) {
                Log.e(TAG, "Trying to connect to device : " + mDeviceAddress);
                connectToDevice(mDeviceAddress);
            }
            else{

                Log.d(TAG, "can not to connect to device");
                if(mBluetoothLeService == null){
                    Log.d(TAG," mBluetoothLeService is null");
                }
                if(mDeviceAddress == null){
                    Log.d(TAG," mDeviceAddress is null");
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    //Connect to Device address and disconnect from a device address
    //
    //  ====> OK (added from back up)
    //
    //---------------------------------------------------------------------------
    public void connectToDevice(String deviceAddress){
        String prevState = sharedpreferences.getString(BtState,null);
        String currentState =  CONNECTING;
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PrevBtState, prevState);
        editor.putString(BtState, currentState);
        editor.commit();
        try{
            Log.e("MainActivity", "display connection on Fragment Home");
            fragmentHome.displayStateConnection(prevState, currentState);
        }
        catch (Exception e){}
        mDeviceAddress = deviceAddress;
        Log.e(TAG, "Bluetooth device : "+ deviceAddress);
        if(isBluetoothAdapterInitialized){
            if (mBluetoothLeService != null) {
                Log.e(TAG, "Ble Adapter is initialized and mBluetoothLeService is not null");
                boolean result = false;
                if (mBluetoothLeService== null){
                    Log.d("MainActivity", "mBluetoothLeService is null");
                }
                else{
                    Log.d("MainActivity", "mBluetoothLeService is not  null");
                }
                try {
                    result = mBluetoothLeService.connect(deviceAddress);
                }catch(Exception e){}
                if(!result)
                {
                    Log.e("MAINACTIVITY", "Connect didn't success! ");
                    prevState = sharedpreferences.getString(BtState,null);
                    currentState = DISCONNECTED;

                    sharedpreferences.edit().putString(PrevBtState, prevState);
                    sharedpreferences.edit().putString(BtState, currentState);
                    sharedpreferences.edit().commit();
                    try{
                        fragmentHome.displayStateConnection(prevState,currentState);
                    }catch (Exception e){};
                }
            } else {
                Log.e(TAG, " ----> mBluetoothLeService is null");
            }
        }
        else{
            Log.e(TAG, "---> Bluetooth Adapter is not initialized!!!");
            initializeAndConnect(mBluetoothLeService);
        }
    }

    public void disconnectToDevice (String mDeviceAddress){

        String prevState = sharedpreferences.getString(BtState,null);
        String currentState =  DISCONNECTING;
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PrevBtState, prevState);
        editor.putString(BtState, currentState);
        editor.commit();
        try{
            fragmentHome.displayStateConnection(prevState, currentState);
        }catch(Exception e){}

        mBluetoothLeService.disconnect();
    }

    public void resetGatt(){
        mBluetoothLeService.resetGatt();
    }
    public void sendOnOffToService() {
        if(mBluetoothLeService != null)
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    try{
                        Message msg = Message.obtain(null, BluetoothLeService.MSG_ON_OFF, 0);
                        mBluetoothLeService.onOffAube();
                    }catch (Exception e){}
                }
            });

        }
    }

    public static boolean isNetworkAvailable(Context context)
    {

        try {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (int i = 0; i<netInfo.length; i++) {
                if (netInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {

            return false;
        }
        return false;

    }
}