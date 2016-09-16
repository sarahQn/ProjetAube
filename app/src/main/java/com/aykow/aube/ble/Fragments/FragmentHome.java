package com.aykow.aube.ble.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.BoringLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aykow.aube.ble.SingleShotLocationProvider;
import com.aykow.aube.ble.activities.MainActivity;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.SpinnerActionBarNavItem;
import com.aykow.aube.ble.TitleActionBarNavigationAdapter;
import com.aykow.aube.ble.UserDevicesDB;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by sarah on 23/03/2016.
 */
public class FragmentHome extends Fragment {

    Context context;
    private final int PERMISSION_REQUEST_COARSE_LOCATION= 0;
    private static final int REQUEST_ENABLE_BT = 1 ;
    List<UserDevicesDB> userDevicesDBList;
    List<UserDevicesDB> userHomeDevicesDBList;
    List<UserDevicesDB> userCarDevicesDBList;
    ActionBar actionBar;
    ArrayList<SpinnerActionBarNavItem> navSpiner;
    TitleActionBarNavigationAdapter adapter;

    private final static String TAG = "--FRAGMENT_HOME--";

    private String mDeviceName;
    private String mDeviceAddress;
    int nbNotification= 0;

    boolean isAubeSelected = false;


   // public TextView tvConnectionStatus, tvTemperature, tvAirQuality, tvNbNotification;
    public TextView tvAirQuality,tvAqDesc;//;
    public ImageView imgIndoor;
    public ImageView btnOnOff;

    public TextView tvCity, tvAqi, tvAqiDesc;
    public ImageView imgOutdoor;

    public static String CONNECTED = "connected";
    public static String CONNECTING = "connecting";
    public static String DISCONNECTING = "disconnecting";
    public static String DISCONNECTED = "disconnected";

    public static final String BtState = "btState";
    public static final String PrevBtState = "prevBtState";

    public int currentTemperature=20;
    public int currentAirQ;
    public FloatingActionButton fab;
    public ImageView btnLoc;
    public ProgressBar progressBarConnecting;
    public ProgressBar progressBarLocation;

    public static final String PREFERENCES = "MyPrefs" ;
    public static final String AirQuality = "airQuality";
    public static final String IdSelectedAube = "idSelectedAube";
    public static final String BtAddress ="btAddress";
    public static final String Lat ="lat";
    public static final String Lng ="lng";

    public LocationManager locationManager;
    public LocationListener locationListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FRAGMENT HOME", "=====> OnCreate");
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        context = getActivity().getApplicationContext();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        Log.d("FRAGMENT HOME", "=====> OnCreateView");
        MainActivity activity = (MainActivity) getActivity();
        userDevicesDBList = activity.getUserDevicesDBList();
        userHomeDevicesDBList = activity.getHomeUserDevicesDBList();
        userCarDevicesDBList = activity.getCarUserDevicesDBList();
        actionBar = ((MainActivity) getActivity()).getSupportActionBar();//.getActionBar();



        View rootView = inflater.inflate(R.layout.fragment_home,container, false);
        imgIndoor = (ImageView) rootView.findViewById(R.id.imgv_indoor);
        tvAirQuality = (TextView) rootView.findViewById(R.id.tv_qa_value);
        tvAqDesc = (TextView) rootView.findViewById(R.id.tv_qa_desc);
        btnOnOff = (ImageView) rootView.findViewById(R.id.btn_on_off);
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).sendOnOffToService();
            }
        });

        //tvCity = (TextView) rootView.findViewById(R.id.tv_outdoor_city);
        imgOutdoor = (ImageView) rootView.findViewById(R.id.imgv_outdoor);
        tvAqi = (TextView) rootView.findViewById(R.id.tv_qa_outdoor_value);
        tvAqiDesc = (TextView) rootView.findViewById(R.id.tv_qa_aqi_desc);

        btnLoc = (ImageView) rootView.findViewById(R.id.btn_loc);
        progressBarConnecting = (ProgressBar) rootView.findViewById(R.id.pb_connecting);
        progressBarLocation = (ProgressBar) rootView.findViewById(R.id.pb_location);
        displayPbLocation(false);
        getAQI();
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAQI();
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        //fab.setLayoutParams(new RelativeLayout.LayoutParams(fab.getMeasuredHeight(), fab.getMeasuredHeight()));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(getContext(),"Fab clicked", Toast.LENGTH_SHORT).show();
                //check if Blutooth is on or off, iff off, request for activating bluetooth when user click on fab
                //if bluetooth is on, means that we lost connexion from the Aube, so we tell to the user

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(!bluetoothAdapter.isEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_ENABLE_BT);
                }
                else{
                    // Start building the AlertDialog
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    //b.setTitle("Connection lost");
                    b.setMessage(R.string.check_aube_connection);
                    b.setIcon(android.R.drawable.ic_dialog_alert);
                    b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            //-----displayStateConnection(CONNECTING);
                            try {
                                ((MainActivity) getActivity()).connectToDevice(mDeviceAddress);
                                dialog.dismiss();
                            }catch (Exception e){}


                        }
                    });
                    b.show();

                    Log.e("FRAGMENT HOME", "Device Address: "+mDeviceAddress+" and Device Name: "+mDeviceName);
                    //((MainActivity) getActivity()).disconnectToDevice(mDeviceAddress);




                }
            }
        });

       /* tvConnectionStatus = (TextView) rootView.findViewById(R.id.tv_data_status_aube);
        tvTemperature = (TextView) rootView.findViewById(R.id.tv_data_temperature);

        tvNbNotification = (TextView) rootView.findViewById(R.id.tv_data_nb_notification);*/

        if(actionBar!= null) {
           // Hide The action bar title
            actionBar.setDisplayShowTitleEnabled(false);
            // Enabling Spinner dropdown navigation
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            // Spinner title navigation data
            navSpiner = new ArrayList<SpinnerActionBarNavItem>();
            for (UserDevicesDB userDevicesDB : userDevicesDBList) {
                navSpiner.add(new SpinnerActionBarNavItem(userDevicesDB.getNameDev()));
            }

            //title drop down adapter
            adapter = new TitleActionBarNavigationAdapter(getContext(), navSpiner);

            //----------------------------------------------------------------
            //assigning the spinner navigation
            // if selected device is a home device show house imageview in @+id/imgv_indoor
            //else if selected device is a car device, show car imageview in @+id/imgv_indoor
            actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                    isAubeSelected = true;
                    //((MainActivity)getActivity()).sharedpreferences = context.getSharedPreferences(PREFERENCES, context.MODE_PRIVATE);

                    UserDevicesDB userDevicesDB = userDevicesDBList.get(itemPosition);

                    SharedPreferences.Editor editor = ((MainActivity)getActivity()).sharedpreferences.edit();
                    editor.putInt(IdSelectedAube, itemPosition);
                    editor.putString(BtAddress, userDevicesDB.getAddr());
                    editor.commit();

                    Log.d("ACTIONBAR ITEM", "----> Item at position: " + itemPosition + " selected");

                    if( userCarDevicesDBList.contains(userDevicesDB))
                    {
                        imgIndoor.setImageResource(R.drawable.ic_indoor_car);
                    }
                    else
                    {
                        imgIndoor.setImageResource(R.drawable.ic_indoor_house);
                    }

                    String connState = ((MainActivity)getActivity()).sharedpreferences.getString(BtState, null);

                    if(connState!=null)
                    {
                        Log.e("FrHome", " bt State:" + connState);
                        if(mDeviceAddress!= null){
                            if (!(((MainActivity)getActivity()).sharedpreferences.getString(BtState, null).equals(DISCONNECTED))) {
                                ((MainActivity) getActivity()).disconnectToDevice(mDeviceAddress);
                            }
                        }

                        mDeviceName = userDevicesDB.getNameDev();
                        mDeviceAddress = userDevicesDB.getAddr();

                        Log.d("Fragment home", "Bt address : "+ mDeviceAddress+ "<-----");

                        Log.e("FrHOME", " will start connect action");

                        new Thread(new Runnable() {
                            public void run() {
                                Log.e("FrHOME Thread"," when disconnected, connect to selected item");
                                int i= 0;
                                while(!((MainActivity)getActivity()).sharedpreferences.getString(BtState, null).equals(DISCONNECTED) && i<=10){
                                    try {
                                        i++;
                                        Log.e("WAIT SYNC", "total wait : " + i +" state:"+((MainActivity)getActivity()).sharedpreferences.getString(BtState, null));
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        Log.e("Exception", " exception in wait 1000");
                                        e.printStackTrace();
                                    }
                                }
                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                if(bluetoothAdapter.isEnabled()) {
                                    Log.d("FragHome", "--->it's OKI, disconnection done!! and now we will connect ");
                                    try {
                                        ((MainActivity) getActivity()).connectToDevice(mDeviceAddress);
                                    }catch (Exception e){}
                                }
                                else{
                                    Log.d("FragHome", "----> bt Adapter is not enabled!!");
                                }

                            }
                        }).start();

                    }
                    else{
                        Log.e("FrHome", " connState is null");
                    }
                   /* if(mDeviceAddress!=null){
                        Log.d("Item SELECTED FHome","start to disconnect from prev item ------------ 1");
                        ((MainActivity) getActivity()).disconnectToDevice(mDeviceAddress);
                        //ry{
                        //    Thread.sleep(1000);
                        //}catch (Exception e){}

                        mDeviceName = userDevicesDB.getNameDev();
                        mDeviceAddress = userDevicesDB.getAddr();

                        //-----displayStateConnection(CONNECTING);
                        Log.d("Item SELECTED FHome", "-------> WAITING");
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                Log.d("Item SELECTED FHome", "-------> END WAITING");
                                Log.d("Item SELECTED FHome","start to connect to selected item ----------- 2");
                                ((MainActivity) getActivity()).connectToDevice(mDeviceAddress);
                            }
                        }, 4000);

                    }
                    else {

                        mDeviceName = userDevicesDB.getNameDev();
                        mDeviceAddress = userDevicesDB.getAddr();

                        //-----displayStateConnection(CONNECTING);
                        Log.d("Item SELECTED FHome", "start to connect to selected item ----------- 2");
                        ((MainActivity) getActivity()).connectToDevice(mDeviceAddress);
                    }*/

                    return false;
                }
            });
        }
        else{
           Log.d("ACTIONBAR", "actionbar is NULL");
        }

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSION", "coarse location permission granted");
                    requestSingleLocationUpdate(getContext());
                } else {
                    final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.functionality_limited);
                    builder.setMessage(R.string.error_grant_location);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void checkAndEnableGPS(){
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),6);
        }
    }

    public void requestSingleLocationUpdate(Context context) {
        SingleShotLocationProvider.requestSingleUpdate(context,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        double lat = location.latitude;
                        double lng = location.longitude;
                        Log.d("Location", "lat is: " + location.latitude + " and lng is: "+location.longitude);
                        //update lat an lng on shared preferences
                        SharedPreferences.Editor editor =  MainActivity.sharedpreferences.edit();
                        editor.putFloat("lat", (float)lat);
                        editor.putFloat("lng", (float)lng);
                        editor.commit();

                        //get data about aqi
                        String url = "http://aykow.fr/Aube/Application/Bdd/getAQICN.php";
                        //TO DO..

                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestParams params = new RequestParams();
                        params.put("lat",lat);
                        params.put("lng",lng);
                        client.post("http://aykow.fr/Aube/Application/Bdd/getAQICN.php", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                try {
                                    //Log.d("RESULT AQI", "" + new String(responseBody));
                                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        Log.d("JSON", jsonObject.toString());
                                        String aqiValueString = jsonObject.get("aqiValue").toString();
                                        String aqiCity = jsonObject.get("location").toString();
                                        Log.d("aqi value", "aqiValue: "+ aqiValueString);
                                        if(aqiValueString.equals("null"))
                                        {
                                            clearAqi();
                                        }
                                        else
                                        {
                                            int aqiValue = Integer.valueOf(aqiValueString);
                                            tvAqi.setText(aqiValueString);
                                            //------tvCity.setText(aqiCity);

                                            displayAQI(aqiValue);
                                        }
                                        displayPbLocation(false);

                                    }
                                    //int aqiValue = Integer.getInteger();
                                    //String aqiInfo = jsonObject.get("aqiInfo").toString();
                                    //Log.d("GETTING AQI", "finished => aqi value: "+ aqiValue );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                //Log.d("RESULT AQI", "result failure : " + responseBody.toString());
                                Log.d("GETTING AQI", "finished");
                                displayPbLocation(false);
                            }
                        });
                    }
                });
    }
    @Override
    public void onStart(){
        super.onStart();
        int id = ((MainActivity)getActivity()).sharedpreferences.getInt(IdSelectedAube,0);
        actionBar.setSelectedNavigationItem(id);
    }
    @Override
    public void onPause() {
        Log.d("FRAGMENT HOME", "=====> OnPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("FRAGMENT HOME", "=====> OnStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d("FRAGMENT HOME", "=====> OnDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d("FRAGMENT HOME", "=====> OnDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d("FRAGMENT HOME", "=====> OnDetach");
        super.onDetach();
    }


    /*public void displayStateConnection(String connectionState) {
        if (connectionState.equals(CONNECTING)){
            fab.setVisibility(View.INVISIBLE);
            btnOnOff.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            Log.d("FRAGMENT HOME", "CONNECTING");
        }
        else if(connectionState.equals(CONNECTED)){
            progressBar.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
            btnOnOff.setVisibility(View.VISIBLE);

            Log.d("FRAGMENT HOME", "CONNECTED");

        }
        else if(connectionState.equals(DISCONNECTING))
        {
            Log.d("FRAGMENT HOME", "DISCONNECTing");
        }

        else if(connectionState.equals(DISCONNECTED))
        {
            btnOnOff.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            clear();
            Log.d("FRAGMENT HOME", "DISCONNECTED");
        }
        else {
            Log.d("FRAGMENT HOME", "ELSE : "+ connectionState);
        }
    }*/

    public void displayStateConnection(String prevState, String currentState) {
        Log.d("-----> FRAGMENT HOME", "prev state : "+ prevState+" and current state : "+currentState);

        if (prevState.equals(DISCONNECTED) && currentState.equals(DISCONNECTING)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    progressBarConnecting.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.INVISIBLE);
                    btnOnOff.setVisibility(View.INVISIBLE);
                }});
        }
        else if(prevState.equals(DISCONNECTED) && currentState.equals(DISCONNECTED)) {
            btnOnOff.setVisibility(View.INVISIBLE);
            progressBarConnecting.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);
        }
        else if(prevState.equals(DISCONNECTED) && currentState.equals(CONNECTING)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    fab.setVisibility(View.INVISIBLE);
                    progressBarConnecting.setVisibility(View.VISIBLE);
                    btnOnOff.setVisibility(View.INVISIBLE);
                } });


        }
        else if(prevState.equals(CONNECTING) && currentState.equals(CONNECTED)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    progressBarConnecting.setVisibility(View.INVISIBLE);
                    fab.setVisibility(View.INVISIBLE);
                    btnOnOff.setVisibility(View.VISIBLE);
                    Log.d("------> FRAGMENT HOME", "BTN ONOFF BAR VISIBLE <------------");
                }});
        }
        else if(prevState.equals(CONNECTING) && currentState.equals(DISCONNECTED)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    progressBarConnecting.setVisibility(View.INVISIBLE);
                    btnOnOff.setVisibility(View.INVISIBLE);
                    clear();
                    fab.setVisibility(View.VISIBLE);
                    Log.d("------> FRAGMENT HOME", " ! VISIBLE <------------");
                }});

        }
        else if(prevState.equals(CONNECTED) && currentState.equals(DISCONNECTING)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    btnOnOff.setVisibility(View.INVISIBLE);
                    fab.setVisibility(View.INVISIBLE);
                    clear();
                    progressBarConnecting.setVisibility(View.VISIBLE);
                    Log.d("------> FRAGMENT HOME", "PROGRESSBAR VISIBLE <------------");
                }});
        }
        else if(prevState.equals(CONNECTED) && currentState.equals(CONNECTED)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    fab.setVisibility(View.INVISIBLE);
                    progressBarConnecting.setVisibility(View.INVISIBLE);
                    btnOnOff.setVisibility(View.VISIBLE);
                    Log.d("------> FRAGMENT HOME", "ON OFF VISIBLE <------------");
                }});
        }
        else if(prevState.equals(CONNECTED) && currentState.equals(DISCONNECTED)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    btnOnOff.setVisibility(View.INVISIBLE);
                    progressBarConnecting.setVisibility(View.INVISIBLE);
                    clear();
                    fab.setVisibility(View.VISIBLE);
                    Log.d("------> FRAGMENT HOME", "! VISIBLE <------------");
                }});
        }
        else if(prevState.equals(DISCONNECTING) && currentState.equals(DISCONNECTED)){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    btnOnOff.setVisibility(View.INVISIBLE);
                    progressBarConnecting.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.INVISIBLE);
                    clear();

                    Log.d("------> FRAGMENT HOME", "! VISIBLE <------------");
                }});
        }

        else{
            boolean value2 = prevState.equals(DISCONNECTED) && currentState.equals(CONNECTING);
            Log.d("----> FRAGMENT HOME", "is prev disconnected and current connecting: "+ value2 );
            Log.d("----> FRAGMENT HOME", "else -> prev: "+prevState+" and current state: "+ currentState);
        }
    }

    public void displayTemperature(double dataTempExtra) {
        currentTemperature = (int) dataTempExtra;

    }

    public void displayDataAir(double dataAirExtra) {
        /*----currentAirQ = (int) dataAirExtra;
        try{
            currentAirQ =(int) (currentAirQ /(1.9732-(81.8*0.001*currentTemperature)+(2*0.001*currentTemperature*currentTemperature)-(20*0.000001*currentTemperature*currentTemperature*currentTemperature)));
        }
        catch(Exception e){}

        int value = (int)currentAirQ;


        if(currentAirQ>=0 && currentAirQ <= 78)
        {
            value = (int) ((0.0007 * currentAirQ * currentAirQ * currentAirQ) - (0.0105 * currentAirQ * currentAirQ) + (0.7426 * currentAirQ) - 0.2621);//(air*3.85);

        }
        else if (currentAirQ>78)
        {
            value = (int) ((currentAirQ *0.83)+261.3);
        }

        tvAirQuality.setText(String.format("%d",currentAirQ)); //Double.toString(currentAirQ));----*/
        int value = (int)dataAirExtra;
        currentAirQ = value;
        tvAirQuality.setText(String.format("%d",value));

        if(value<=50)
        {
            tvAirQuality.setTextColor(getResources().getColor(R.color.greenAQ));
            imgIndoor.setBackgroundColor(getResources().getColor(R.color.greenAQ));
            tvAqDesc.setText(getResources().getString(R.string.good));
            tvAqDesc.setTextColor(getResources().getColor(R.color.greenAQ));
        }
        else if(value>50 && value<=100)
        {
            tvAirQuality.setTextColor(getResources().getColor(R.color.yellowAQ));
            imgIndoor.setBackgroundColor(getResources().getColor(R.color.yellowAQ));
            tvAqDesc.setText(getResources().getString(R.string.moderate));
            tvAqDesc.setTextColor(getResources().getColor(R.color.yellowAQ));
        }
        else if(value>100 && value<=150)
        {
            tvAirQuality.setTextColor(getResources().getColor(R.color.orangeAQ));
            imgIndoor.setBackgroundColor(getResources().getColor(R.color.orangeAQ));
            tvAqDesc.setText(getResources().getString(R.string.unhealthy_sensitive_groups));
            tvAqDesc.setTextColor(getResources().getColor(R.color.orangeAQ));
        }
        else if(value>150 && value<=200)
        {
            tvAirQuality.setTextColor(getResources().getColor(R.color.redAQ));
            imgIndoor.setBackgroundColor(getResources().getColor(R.color.redAQ));
            tvAqDesc.setText(getResources().getString(R.string.unhealthy));
            tvAqDesc.setTextColor(getResources().getColor(R.color.redAQ));
        }
        else if(value>200 && value<=300)
        {
            tvAirQuality.setTextColor(getResources().getColor(R.color.purpleAQ));
            imgIndoor.setBackgroundColor(getResources().getColor(R.color.purpleAQ));
            tvAqDesc.setText(getResources().getString(R.string.very_unhealthy));
            tvAqDesc.setTextColor(getResources().getColor(R.color.purpleAQ));
        }
        else
        {
            tvAirQuality.setTextColor(getResources().getColor(R.color.garnetAQ));
            imgIndoor.setBackgroundColor(getResources().getColor(R.color.garnetAQ));
            tvAqDesc.setText(getResources().getString(R.string.hazardous));
            tvAqDesc.setTextColor(getResources().getColor(R.color.garnetAQ));
        }
        Log.d(TAG, "Air Quality : "+ currentAirQ);
    }



    public void clear(){
        //btnOnOff.setVisibility(View.INVISIBLE);
        tvAirQuality.setText("---");
        tvAirQuality.setTextColor(getResources().getColor(R.color.grayAQ));
        imgIndoor.setBackgroundColor(getResources().getColor(R.color.grayAQ));
        tvAqDesc.setText("---");
        tvAqDesc.setTextColor(getResources().getColor(R.color.grayAQ));
    }


    /*public void commit(){
        Bundle bundle = getArguments();
        String data = bundle.getString("DATA");
        tvConnectionStatus.setText(data);
    }*/


    //-----------------------------------------------------------------------------------------------------------------
    //
    //  Outdoor
    //
    //------------------------------------------------------------------------------------------------------------------

    public  void getAQI()
    {

        if(MainActivity.isNetworkAvailable(context))// connexion internet oki
        {
            displayPbLocation(true);
            //clearAqi();
            Log.i("Fragment", "getLoc clicked");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android M Permission check 
                if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("PERMISSION", "permission is NOT enabled yet");
                    final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.need_location_access);
                    builder.setMessage(R.string.ask_grant_location);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    });
                    builder.show();
                }
                else{
                    Log.e("PERMISSION", "permission is enabled");
                    checkAndEnableGPS();
                    requestSingleLocationUpdate(getContext());
                }
            }
            else{
                checkAndEnableGPS();
                requestSingleLocationUpdate(getContext());
            }
        }
        else
        {
            Toast.makeText(context,R.string.need_internet_aqi, Toast.LENGTH_SHORT).show();
        }

    }



    public void displayPbLocation(boolean display)
    {
        if(display)
        {
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    progressBarLocation.setVisibility(View.VISIBLE);
                    btnLoc.setVisibility(View.INVISIBLE);
                }});
        }
        else
        {
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    progressBarLocation.setVisibility(View.INVISIBLE);
                    btnLoc.setVisibility(View.VISIBLE);
                }});
        }
    }

    public void displayAQI(int value)
    {
        if(value<=50)
        {
            tvAqi.setTextColor(getResources().getColor(R.color.greenAQ));
            imgOutdoor.setBackgroundColor(getResources().getColor(R.color.greenAQ));
            tvAqiDesc.setText(getResources().getString(R.string.good));
            tvAqiDesc.setTextColor(getResources().getColor(R.color.greenAQ));
        }
        else if(value>50 && value<=100)
        {
            tvAqi.setTextColor(getResources().getColor(R.color.yellowAQ));
            imgOutdoor.setBackgroundColor(getResources().getColor(R.color.yellowAQ));
            tvAqiDesc.setText(getResources().getString(R.string.moderate));
            tvAqiDesc.setTextColor(getResources().getColor(R.color.yellowAQ));
        }
        else if(value>100 && value<=150)
        {
            tvAqi.setTextColor(getResources().getColor(R.color.orangeAQ));
            imgOutdoor.setBackgroundColor(getResources().getColor(R.color.orangeAQ));
            tvAqiDesc.setText(getResources().getString(R.string.unhealthy_sensitive_groups));
            tvAqiDesc.setTextColor(getResources().getColor(R.color.orangeAQ));
        }
        else if(value>150 && value<=200)
        {
            tvAqi.setTextColor(getResources().getColor(R.color.redAQ));
            imgOutdoor.setBackgroundColor(getResources().getColor(R.color.redAQ));
            tvAqiDesc.setText(getResources().getString(R.string.unhealthy));
            tvAqiDesc.setTextColor(getResources().getColor(R.color.redAQ));
        }
        else if(value>200 && value<=300)
        {
            tvAqi.setTextColor(getResources().getColor(R.color.purpleAQ));
            imgOutdoor.setBackgroundColor(getResources().getColor(R.color.purpleAQ));
            tvAqiDesc.setText(getResources().getString(R.string.very_unhealthy));
            tvAqiDesc.setTextColor(getResources().getColor(R.color.purpleAQ));
        }
        else if(value >300)
        {
            tvAqi.setTextColor(getResources().getColor(R.color.garnetAQ));
            imgOutdoor.setBackgroundColor(getResources().getColor(R.color.garnetAQ));
            tvAqiDesc.setText(getResources().getString(R.string.hazardous));
            tvAqiDesc.setTextColor(getResources().getColor(R.color.garnetAQ));
        }
    }

    public void clearAqi(){
        //btnOnOff.setVisibility(View.INVISIBLE);
        tvAqi.setText("---");
        tvAqi.setTextColor(getResources().getColor(R.color.grayAQ));
        imgOutdoor.setBackgroundColor(getResources().getColor(R.color.grayAQ));
        tvAqiDesc.setText("---");
        tvAqiDesc.setTextColor(getResources().getColor(R.color.grayAQ));
        //--------tvCity.setText("");
    }
}
