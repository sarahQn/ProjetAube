package com.aykow.aube.ble.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aykow.aube.ble.DatabaseHelper;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.UserDevicesDB;
import com.aykow.aube.ble.UserDevicesDBDataSource;
import com.koushikdutta.ion.Ion;
import java.util.ArrayList;
import java.util.List;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SearchNewDevicesActivity extends AppCompatActivity {

    ImageView imageView;
    ProgressBar progressBar;

    BluetoothAdapter bluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    static final long SCAN_PERIOD = 30000;
    IntentFilter intentFilter;

    ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    Button btnScan;
    TextView tvScanBlink;
    ObjectAnimator txtScanAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        imageView = (ImageView)findViewById(R.id.image_gif);
        progressBar = (ProgressBar)findViewById(R.id.pb_search);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        btnScan = (Button)findViewById(R.id.btnScan);

        tvScanBlink = (TextView) findViewById(R.id.tv_scan_blink);

        txtScanAnim = ObjectAnimator.ofInt(tvScanBlink,"textColor", Color.GRAY, Color.TRANSPARENT);
        txtScanAnim.setDuration(2000);
        txtScanAnim.setEvaluator(new ArgbEvaluator());
        txtScanAnim.setRepeatCount(ValueAnimator.INFINITE);
        txtScanAnim.setRepeatMode(ValueAnimator.REVERSE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null)
        {
            new AlertDialog.Builder(this).setTitle("Not compatible").setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else{

            btnScan.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        checkLocationPermission();
                    }
                    else {
                        loadGifCenter();
                        bluetoothAdapter.startDiscovery();
                    }


                }
            });

            if (!bluetoothAdapter.isEnabled())
            {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            }
            else{

                if (Build.VERSION.SDK_INT >= 21) {
                    checkLocationPermission();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    loadGifCenter();
                    bluetoothAdapter.startDiscovery();
                }



            }
        }


        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(receiver, intentFilter);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
            else if (resultCode == Activity.RESULT_OK)
            {
                if (Build.VERSION.SDK_INT >= 21) {
                    checkLocationPermission();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    loadGifCenter();
                    bluetoothAdapter.startDiscovery();
                }
            }
        }

    }

    public void loadGifCenter(){
       Ion.with(imageView).fitCenter().load("android.resource://" + getPackageName() + "/" + R.drawable.aube_360);//.fitCenter().load("android.resource://" + getPackageName() + "/" + R.drawable.aube_optimize);
   }

    public void startBlinkingText()
    {
        tvScanBlink.setVisibility(View.VISIBLE);
        txtScanAnim.start();
    }

    public void stopBlinkingText()
    {
        tvScanBlink.setVisibility(View.INVISIBLE);
        if(txtScanAnim!=null)
        {
            txtScanAnim.cancel();

        }
    }


    @Override
    protected void onPause() {
        if (bluetoothAdapter != null)
        {
            if(bluetoothAdapter.isDiscovering())
            {
                bluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if(state == BluetoothAdapter.STATE_ON)
                {
                    //showEnabled();
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                progressBar.setVisibility(View.VISIBLE);
                startBlinkingText();
                deviceList = new ArrayList<BluetoothDevice>();
                btnScan.setVisibility(View.INVISIBLE);

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Ion.with(imageView).fitCenter().load("android.resource://" + getPackageName() + "/" + R.drawable.ic_aube);
                progressBar.setVisibility(View.INVISIBLE);
                stopBlinkingText();
                if(deviceList.size()>0)
                {
                    btnScan.setVisibility(View.INVISIBLE);
                    Intent newIntent = new Intent(SearchNewDevicesActivity.this, DeviceListActivity.class);
                    newIntent.putParcelableArrayListExtra("device.list", deviceList);
                    startActivity(newIntent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"No device founded! try to scan again.",Toast.LENGTH_SHORT).show();
                    btnScan.setVisibility(View.VISIBLE);
                }

            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action))
            {

                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName()!= null  && !isInDeviceList(device,deviceList) &&  !isAlreadyRecorded(device)) {
                    if (device.getName().equals("AUBE"))
                    {
                        deviceList.add(device);
                    }
                }
            }
        }
    };

    private boolean isAlreadyRecorded(BluetoothDevice device) {
        UserDevicesDBDataSource dataSource;
        DatabaseHelper dbHelper = DatabaseHelper.getDbHelperInstance(this);
        List<UserDevicesDB> userDevicesDBList = new ArrayList<UserDevicesDB>();
        userDevicesDBList = dbHelper.getAllDevicesInDbSQLite();
        for (UserDevicesDB userDevicesDB:userDevicesDBList)
        {
            if(userDevicesDB.getAddr().equals(device.getAddress())){
                return true;
            }
        }
        return false;
    }

    private boolean isInDeviceList(BluetoothDevice device, ArrayList<BluetoothDevice> deviceList) {

        for( BluetoothDevice dev:deviceList)
        {
            if(dev.getAddress().equals(device.getAddress())){
                return true;
            }
        }
        return false;

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkLocationPermission(){
        if (Build.VERSION.SDK_INT >= 21)
        {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if (permissionCheck != 0) {

                Log.e("PERMISSION", "requestPermission : " + permissionCheck);
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1001: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSION", "coarse location permission granted");
                    progressBar.setVisibility(View.VISIBLE);
                    loadGifCenter();
                    bluetoothAdapter.startDiscovery();

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover Aube.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Log.e("PERMISSION"," close windows and show again the permission");
                            checkLocationPermission();
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
