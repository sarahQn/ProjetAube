package com.aykow.aube.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aykow.aube.ble.activities.MainActivity;

import java.util.List;
import java.util.UUID;


/**
 * Created by sarah on 09/05/2016.
 */
public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    public static final int MSG_ON_OFF = 0; ;

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    String mBluetoothDeviceAddress;
    BluetoothGatt mBluetoothGatt;
    int mConnectionState = STATE_DISCONNECTED;



    private static final int STATE_CONNECTING = 0;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_DISCONNECTING = 2;
    private static final int STATE_DISCONNECTED = 3;

    public final static String ACTION_GATT_CONNECTED =  "com.aykow.aube.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING =  "com.aykow.aube.ble.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED =   "com.aykow.aube.ble.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCONNECTING =   "com.aykow.aube.ble.ACTION_GATT_DISCONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =    "com.aykow.aube.ble.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE_TEMP =  "com.aykow.aube.ble.ACTION_DATA_AVAILABLE_TEMP";
    public final static String ACTION_DATA_AVAILABLE_AIR =  "com.aykow.aube.ble.ACTION_DATA_AVAILABLE_AIR";
    public final static String EXTRA_DATA_TEMP = "com.aykow.aube.ble.EXTRA_DATA_TEMP";
    public final static String EXTRA_DATA_AIR = "com.aykow.aube.ble.EXTRA_DATA_AIR";

    public static final UUID UUID_DATA_SERVICE = UUID.fromString("000018ff-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_TEMPERATURE_CHAR = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_AIR_CHAR = UUID.fromString("00001824-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_ONOFF_CHAR = UUID.fromString("00001826-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_GET_STATE_CHAR = UUID.fromString("00001828-0000-1000-8000-00805f9b34fb");

    public static final String PREFERENCES = "MyPrefs" ;
    public static final String BtState = "btState";
    public static final String PrevBtState = "prevBtState";
    String DISCONNECTED = "disconnected";


    /* Client Configuration Descriptor */
    private static final UUID UUID_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private int cursorSensor = 0;

    private void reset() {
        cursorSensor = 0;
    }

    private void advance() {
        cursorSensor++;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
            String intentAction;

            if (newState == BluetoothProfile.STATE_CONNECTING){

            }
            else if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "----> sending intentAction for CONNECTED....");
                Log.i(TAG," Attempting to start service discovery: "+mBluetoothGatt.discoverServices());
            }

            else if(newState == BluetoothProfile.STATE_DISCONNECTING){
            }

            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "sending intentAction for DISCONNECTED....");
                broadcastUpdate(intentAction);
            }

        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                reset();
                readNextSensor();


            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(characteristic.getUuid().equals(UUID_TEMPERATURE_CHAR))
                {
                    Log.d(TAG, " ----------------> onCharacteristicRead Temperature");
                    broadcastUpdate(ACTION_DATA_AVAILABLE_TEMP, characteristic);
                    setNotifyNextSensor();
                }
                else if (characteristic.getUuid().equals(UUID_AIR_CHAR)){
                    Log.d(TAG, " ----------------> onCharacteristicRead Air");
                    //--broadcastUpdate(ACTION_DATA_AVAILABLE_AIR, characteristic);
                    setNotifyNextSensor();
                }
                else if( characteristic.getUuid().equals(UUID_GET_STATE_CHAR)){

                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(characteristic.getUuid().equals(UUID_TEMPERATURE_CHAR)) {
                Log.d(TAG, "------> Received a notification From Temperature !!");
                broadcastUpdate(ACTION_DATA_AVAILABLE_TEMP, characteristic);
            }
            else if(characteristic.getUuid().equals(UUID_AIR_CHAR))
            {
                Log.d(TAG, "------> Received a notification From Air quality !!");
                broadcastUpdate(ACTION_DATA_AVAILABLE_AIR, characteristic);
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            advance();
            readNextSensor();
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if(characteristic.getUuid().equals(UUID_TEMPERATURE_CHAR)){
            double temperature = SensorExtractData.extractTemperature(characteristic);
            intent.putExtra(EXTRA_DATA_TEMP, temperature);
        }
        else if(characteristic.getUuid().equals(UUID_AIR_CHAR)){
            double air = SensorExtractData.extractAir(characteristic);
            //Log.d("BluetoothLeService AIR"," value of air quality : "+air);
            intent.putExtra(EXTRA_DATA_AIR, air);
        }
        sendBroadcast(intent);
    }

    @Override
    public boolean stopService(Intent name) {
        mBluetoothGatt.disconnect();
        mBluetoothGatt=null;
        return super.stopService(name);
    }


    public class LocalBinder extends Binder{
        public BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("BtLeSERVICE", "OnBind <------");
        return mBinder;
    }


    @Override
    public boolean onUnbind(Intent intent){
        //close() is invoked when the UI is disconnected from the Service.
        Log.d("BtLeSERVICE", "OnUnbind <------");
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    //--------------------------------------------------------------------------------------
    //Initializes a reference to the local Bluetooth adapter, return true if successful
    //--------------------------------------------------------------------------------------
    public boolean initialize(){
        if(mBluetoothManager == null){
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null){
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null)
        {Log.e(TAG, "Unable to obtain a BluetoothAdapter");
            return false;
        }

        return true;
    }

    //--------------------------------------------------------------------------------------------
    // Connect to the Gatt server on the Bluetooth le device
    // @param : The device address of the destination device
    // @return : Return true if the connection is initiated successfully.
    // The connection result is reported asynchronously through the
    // {@code BluetoothGattCallback#onConnectionState(android.bluetooth.BluetoothGatt, int, int)}
    // callback
    //--------------------------------------------------------------------------------------------
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter is null.");
            return false;
        }
        else {
            Log.w(TAG, "BluetoothAdapter is NOT null.");
            if (address == null) {
                Log.w(TAG, "unspecified address.");
                return false;
            }
            else{
                Log.w(TAG, "address is oki");
            }
        }

        //--------------------------------------------------
        //Previously connected device. Try to reconnect.
        //--------------------------------------------------
        if(mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt !=null){
            Log.d(TAG, "Trying to use an existing mBluetoothGatt.");
            boolean result=false;
            try {
                result = mBluetoothGatt.connect();
            }
            catch (Exception ex){}

            if(result){
                Log.d(TAG, "----> Connection is OK");
                mConnectionState = STATE_CONNECTING;
                return true;
            }
            else{
                Log.e(TAG, "connection didn't success");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if(device == null){
            Log.w(TAG, "Device not found, Unable to connect.");
            return false;
        }
        else{
            Log.w(TAG, "Device found");
        }

        //We want to directly connect to the device, so we are setting the autoConnect parameter to false.

        Log.d(TAG, "Trying to create a new connection And State is Connecting.");
        mBluetoothGatt = device.connectGatt(this,false, mGattCallback);


        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    //-------------------------------------------------------------------------------------------------
    //Disconnects an existing connection or cancel a pending connection.
    // The disconnection result is reported asynchronously through the
    //{@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
    //callback.
    //-------------------------------------------------------------------------------------------

    public void disconnect(){
        if(mBluetoothAdapter == null || mBluetoothGatt == null){
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        try {
            mBluetoothGatt.disconnect();
        }catch (Exception e){}

        mBluetoothGatt=null;
    }
    public void resetGatt(){
        mBluetoothGatt=null;
    }
    //------------------------------------------------------------------------------------------------------
    //After using a given BLE device, the app must call this method to ensure resources are released properly.
    //------------------------------------------------------------------------------------------------------
    public void close(){
        if(mBluetoothGatt == null){
            return;
        }

        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    //-----------------------------------------------------------------------------------------------
    // Request a read on a given {@code BluetoothGattCharacteristic}.
    // The read result is reported asynchronously through the
    // {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
    // callback
    // @param characteristic : The characteristic to read from.
    //-----------------------------------------------------------------------------------------------
    private void readNextSensor(){
        BluetoothGattCharacteristic characteristic;
        switch (cursorSensor){
            case 0:
                if(BuildConfig.DEBUG)
                {
                    Log.d("Temp", "Reading Temperatue");
                }
                characteristic = mBluetoothGatt.getService(UUID_DATA_SERVICE)
                        .getCharacteristic(UUID_TEMPERATURE_CHAR);

                break;
            case 1:
                if(BuildConfig.DEBUG)
                {
                    Log.d("Air", "Reading Air quality");
                }
                characteristic = mBluetoothGatt.getService(UUID_DATA_SERVICE)
                        .getCharacteristic(UUID_AIR_CHAR);
                break;
           /* case 2:
                if(BuildConfig.DEBUG)
                {
                    Log.d("State", "Reading Aube State");
                }
                characteristic = mBluetoothGatt.getService(UUID_DATA_SERVICE)
                        .getCharacteristic(UUID_STATE_ONOFF_CHAR);
                break;*/

            default:
                return;
        }

        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            Boolean succes = mBluetoothGatt.setCharacteristicNotification(characteristic, false);
            Log.d("notification", "result : " + succes);
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    //--------------------------------------------------------------------------------------
    // Enables notification on a the next characteristic.
    // @param characteristic : Characteristic to act on.
    // @param enable : If true, enable notification, false otherwise.
    //--------------------------------------------------------------------------------------

    private void setNotifyNextSensor(){
        BluetoothGattCharacteristic characteristic;
        //--BluetoothGattDescriptor desc;

        switch (cursorSensor) {

            case 0:
                if(BuildConfig.DEBUG)
                {
                    Log.d("BLE", "Set notify temperature");
                }

                characteristic = mBluetoothGatt.getService(UUID_DATA_SERVICE)
                        .getCharacteristic(UUID_TEMPERATURE_CHAR);

                break;
            case 1:
                if(BuildConfig.DEBUG)
                {
                    Log.d("BLE", "Set notify air quality");
                }
                characteristic = mBluetoothGatt.getService(UUID_DATA_SERVICE)
                        .getCharacteristic(UUID_AIR_CHAR);

                break;
            /*case 2:
                if(BuildConfig.DEBUG)
                {
                    Log.d("BLE", "Set notify aube state char");
                }
                characteristic = mBluetoothGatt.getService(DATA_SERVICE)
                        .getCharacteristic(STATE_CHAR);
                break;*/
            default:
                if(BuildConfig.DEBUG)
                {
                    Log.i("BLE", "All Sensors Enabled");
                }
                return;
        }
        int prop = characteristic.getProperties();
        if(mBluetoothGatt.setCharacteristicNotification(characteristic, true) == true)
        {
            Log.d("notification to true", "SETING NOTIFICATION : SUCCESS !");
        }
        else
        {
            Log.d("notification to true", "SETING NOTIFICATION : FAILURE !");
        }


        for(BluetoothGattDescriptor descriptor : characteristic.getDescriptors())
        {
            descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        try{
            Thread.sleep(200);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }

    //-----------------------------------------------------------------------------------------
    // Retrieves a list of supported GATT services on the connected device.
    // This should be invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
    // @return A {@code List} of supported services.
    //-----------------------------------------------------------------------------------------
    public List<BluetoothGattService> getSupportedGattServices(){
        if(mBluetoothGatt == null)
        {
            return null;
        }
        return mBluetoothGatt.getServices();
    }


    public void onOffAube() {
        final BluetoothGattCharacteristic characteristic_on = mBluetoothGatt.getService(UUID_DATA_SERVICE)
                .getCharacteristic(UUID_ONOFF_CHAR);
        final byte[] valueF = new byte[2];
        valueF[0] = (byte) (0xff);
        valueF[1] = (byte) (0xff);

        final byte[] valueO = new byte[2];
        valueO[0] = (byte) (0x00);
        valueO[1] = (byte) (0x00);

        Runnable r0 = new Runnable() {
            @Override
            public void run() {
                writeChangeChar(characteristic_on, valueO);
            }
        };
        Runnable rF = new Runnable() {
            @Override
            public void run() {
                writeChangeChar(characteristic_on, valueF);
            }
        };

        if (BuildConfig.DEBUG) {
            Log.d("BLE", "turning on/off..");
        }
        Handler h_on = new Handler();
        h_on.postDelayed(rF, 100);
        h_on.postDelayed(r0, 500);
    }

    private void writeChangeChar(BluetoothGattCharacteristic characteristic, byte[] value) {
        characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public boolean isAubeOn(){
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID_DATA_SERVICE)
                .getCharacteristic(UUID_GET_STATE_CHAR);
        mBluetoothGatt.readCharacteristic(characteristic);

        return false;
    }

}
