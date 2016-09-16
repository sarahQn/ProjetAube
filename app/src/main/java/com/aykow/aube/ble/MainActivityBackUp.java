package com.aykow.aube.ble;

/**
 * Created by sarah on 18/05/2016.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;

import com.aykow.aube.ble.Fragments.FragmentDevices;
import com.aykow.aube.ble.Fragments.FragmentHelp;
import com.aykow.aube.ble.Fragments.FragmentHistory;
import com.aykow.aube.ble.Fragments.FragmentHome;
import com.aykow.aube.ble.Fragments.FragmentOutdoor;
import com.aykow.aube.ble.Fragments.FragmentSettings;
import com.aykow.aube.ble.Fragments.FragmentVehicle;
import com.aykow.aube.ble.activities.LoginActivity;

public class MainActivityBackUp extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    UserLocalStore userLocalStore;

    boolean aubeAdded = false;

    DrawerLayout drawerLayout;

    MenuItem homeMenu, vehicleMenu, devicesMenu, outdoorMenu, historyMenu, settingsMenu, helpMenu, logoutMenu;
    Fragment fragment;
    FragmentHome fragmentHome;
    FragmentVehicle fragmentVehicle;
    FragmentOutdoor fragmentOutdoor;
    FragmentHistory fragmentHistory;
    FragmentDevices fragmentDevices;
    FragmentSettings fragmentSettings;
    FragmentHelp fragmentHelp;
    FragmentManager fragmentManager;

    NavigationView navigationView;

    DatabaseHelper dbHelper;
    UserDevicesDBDataSource dataSource;
    List<UserDevicesDB> userDevicesDBList;

    private final static String TAG = "BLUETOOTH DATA";
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;

    boolean isAubeConnected = false;
    boolean isBluetoothAdapterInitialized = false;

    int nbNotification = 0;
    boolean mConnected = false;
    String CONNECTED = "connected";
    String DISCONNECTED = "disconnected";

    int REQUEST_ENABLE_BT = 1;

    //ServiceConnection mServiceConnection;

    //public void setmServiceConnection() {
    //----------------------------------
    //Code to manage Service lifecycle
    //----------------------------------
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("---> SERVICE_CONNECTION", "onServiceConnected mServiceConnection initialisation");
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            mBluetoothLeService = binder.getService();

            enableBtIfNotAndConnect();
            //initializeAndConnect(mBluetoothLeService);


            //Automatically connect to the device upon successful start_up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("---> SERVICE_CONNECTION", "onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    public void initializeAndConnect(BluetoothLeService mBluetoothLeService){
        if(mBluetoothLeService != null) {
            isBluetoothAdapterInitialized = mBluetoothLeService.initialize();
            if (!isBluetoothAdapterInitialized) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            if (mBluetoothLeService != null && !isAubeConnected && mDeviceAddress != null) {
                Log.e(TAG, "Trying to connect to device : " + mDeviceAddress);
                connectToDevice(mDeviceAddress);
            }
        }
    }
    // }

    public void connectToDevice(String deviceAddress){
        mDeviceAddress = deviceAddress;
        Log.e(TAG, "Bluetooth device : "+ deviceAddress);
        if(isBluetoothAdapterInitialized) {
            if (mBluetoothLeService != null) {
                Log.e(TAG, "Ble Adapter is initialized and mBluetoothLeService is not null");
                mBluetoothLeService.connect(deviceAddress);
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
        mBluetoothLeService.disconnect();
    }

    //BroadcastReceicer
    //---------------------------------------------------------------------------------------------------------------
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE_TEMP: received data from the Temperature sensor.  This can be a result of read or notification operations.
    // ACTION_DATA_AVAILABLE_AIR: received data from the Air Quality sensor.  This can be a result of read or notification operations.
    //-----------------------------------------------------------------------------------------------------------------
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                mConnected = true;
                //updateConnectionState(0);//connected");
                //----> invalideOptionMenu();
                //fragmentHome.displayStateConnection(CONNECTED);
                isAubeConnected = true;
                Log.d("BROADCAST_RECEIVER", "-------> actionIntent CONNECTED");
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                //updateConnectionState(1);//"disconnected");
                //----> invalideOptionMenu();
                //clearUI();

                //fragmentHome.displayStateConnection(DISCONNECTED);
                isAubeConnected = false;
                Log.d("BROADCAST_RECEIVER", "-------> DISCONNECTED - 2");

            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                //Show all the supported services and characteristics on the user interface.
                //--------> displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.d("BROADCAST_RECEIVER", "-------> SERVICES_DISCOVERED");
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE_TEMP.equals(action)){
                nbNotification++;
                //fragmentHome.displayTemperature(intent.getDoubleExtra(BluetoothLeService.EXTRA_DATA_TEMP,0));
                Log.d("BROADCAST_RECEIVER", "-------> DATA_AVAILABLE from TEMP");
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE_AIR.equals(action)){
                fragmentHome.displayDataAir(intent.getDoubleExtra(BluetoothLeService.EXTRA_DATA_AIR,0));
                Log.d("BROADCAST_RECEIVER", "-------> DATA_AVAILABLE from AIR");
            }

            //Bundle  bundle = new Bundle();
            //bundle.putString("DATA",action);
            //fragmentHome.setArguments(bundle);
            //---------------------fragmentHome.getView().findViewById(R.id.tv_status_aube);
        }
    };

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                // finish();
                //return;
            }
            else if (resultCode == Activity.RESULT_OK)
            {
                initializeAndConnect(mBluetoothLeService);
            }
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d("BluetoothLeSerice", "bind service");
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        userLocalStore = new UserLocalStore(this);

        //dataSource = new UserDevicesDBDataSource(this);
        dbHelper = DatabaseHelper.getDbHelperInstance(this);
        userDevicesDBList = dbHelper.getAllDevicesInDbSQLite();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        fragmentManager = getSupportFragmentManager();

        fragmentHome = new FragmentHome();
        fragmentVehicle = new FragmentVehicle();
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


    }

    public List<UserDevicesDB> getUserDevicesDBList(){
        return userDevicesDBList;
    }
    private boolean isAubeRecorded() {
        if(userDevicesDBList.size()>0){
            return true;
        }
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();
       /* outdoorMenu.setEnabled(false);
        historyMenu.setEnabled(false);
        settingsMenu.setEnabled(false);
        helpMenu.setEnabled(false);

        if(aubeAdded){
            homeMenu.setChecked(true);
        }else{
            devicesMenu.setChecked(true);
        }*/
    }

    @Override
    public void onBackPressed() {
      /*  DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_home) {
            fragment = fragmentHome;
        /*} else if (id == R.id.nav_vehicle) {
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
            Intent loginIntent = new Intent(MainActivityBackUp.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //finish();
            startActivity(loginIntent);
        }else{
            fragment = fragmentDevices;
        }


        fragmentManager.beginTransaction().replace(R.id.rlContent,fragment).commit();


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

        unregisterReceiver(mReceiver);
        unregisterReceiver(mGattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_TEMP);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_AIR);
        return intentFilter;
    }


    //Bluetooth adapter Listener , receive notification when Bluetooth is enabled or diseabled
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e("Bt_Adapter_Listener", "State OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e("Bt_Adapter_Listener", "State ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
}
