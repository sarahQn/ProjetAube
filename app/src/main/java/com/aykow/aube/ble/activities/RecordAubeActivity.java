package com.aykow.aube.ble.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aykow.aube.ble.DatabaseHelper;
import com.aykow.aube.ble.NetworkStateHandler;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.SyncController;
import com.aykow.aube.ble.UserDevicesDBDataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordAubeActivity extends AppCompatActivity {

    public static final String SP_NAME="userDetails";

    ArrayList<String> list_category = new ArrayList<>();
    ArrayList<String> list_category_name = new ArrayList<>();
    ArrayList<String> list_subcategory_home = new ArrayList<>();
    ArrayList<String> list_subcategory_home_name = new ArrayList<>();
    ArrayList<String> list_subcategory_vehicles = new ArrayList<>();
    ArrayList<String> list_subcategory_vehicles_name = new ArrayList<>();

    ArrayList<String> list_subcategory = new ArrayList<>();
    ArrayList<String> list_selected_subcategory_name = new ArrayList<>();

    ArrayAdapter<String> arrayAdapter_category;
    ArrayAdapter<String> arrayAdapter_subCategory;


    BluetoothDevice selected_aube;

    TextView tv_address_aube;
    EditText et_name;
    Spinner spinner_category;
    Spinner spinner_subcategory;
    Button btn_save;



    private UserDevicesDBDataSource datasource;

    SharedPreferences spUserLocalDB;

    NetworkStateHandler networkStateHandler;
    SyncController syncController;
    DatabaseHelper dbHelper;

    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_aube);

        dbHelper = DatabaseHelper.getDbHelperInstance(this);
        networkStateHandler = NetworkStateHandler.getInstance(this);
        syncController = SyncController.getInstance(this, dbHelper);
        //syncController = new SyncController(dbHelper);

        list_subcategory_vehicles.add(getResources().getString(R.string.car));
        list_subcategory_vehicles.add(getResources().getString(R.string.motorhome));
        list_subcategory_vehicles.add(getResources().getString(R.string.boat));

        list_subcategory_vehicles_name.add("car");
        list_subcategory_vehicles_name.add("motorhome");
        list_subcategory_vehicles_name.add("boat");


        list_subcategory_home.add(getResources().getString(R.string.bedroom));
        list_subcategory_home.add(getResources().getString(R.string.dining_room));
        list_subcategory_home.add(getResources().getString(R.string.dressing_room));
        list_subcategory_home.add(getResources().getString(R.string.kitchen_room));
        list_subcategory_home.add(getResources().getString(R.string.laundry_room));
        list_subcategory_home.add(getResources().getString(R.string.lounge));
        list_subcategory_home.add(getResources().getString(R.string.office));
        list_subcategory_home.add(getResources().getString(R.string.bathroom));
        list_subcategory_home.add(getResources().getString(R.string.toilet));
        list_subcategory_home.add(getResources().getString(R.string.attic));
        list_subcategory_home.add(getResources().getString(R.string.cellar));
        list_subcategory_home.add(getResources().getString(R.string.other));

        list_subcategory_home_name.add("bedroom");
        list_subcategory_home_name.add("dining_room");
        list_subcategory_home_name.add("dressing_room");
        list_subcategory_home_name.add("kitchen_room");
        list_subcategory_home_name.add("laundry_room");
        list_subcategory_home_name.add("lounge");
        list_subcategory_home_name.add("office");
        list_subcategory_home_name.add("bathroom");
        list_subcategory_home_name.add("toilet");
        list_subcategory_home_name.add("attic");
        list_subcategory_home_name.add("cellar");
        list_subcategory_home_name.add("other");





        //--------------datasource = new UserDevicesDBDataSource(getApplicationContext());

       /*------->try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }<---------------------*/

        //-------------------------------------------------------------------------------
        //--------------- Address Device ------------------------------------------------
        //-------------------------------------------------------------------------------
        selected_aube = getIntent().getParcelableExtra("selected_aube");

        tv_address_aube =(TextView) findViewById(R.id.tv_address_record);
        tv_address_aube.setText(selected_aube.getAddress());

        //---------------------------------------------------------------------------------
        //---------------- Name -----------------------------------------------------------
        //---------------------------------------------------------------------------------

        et_name = (EditText) findViewById(R.id.et_name_aube);

        //---------------------------------------------------------------------------------
        //----------------  Category ------------------------------------------------------
        //---------------------------------------------------------------------------------

        list_category.add(getResources().getString(R.string.home));
        list_category.add(getResources().getString(R.string.vehicle));

        list_category_name.add("home");
        list_category_name.add("vehicle");

        arrayAdapter_category = new ArrayAdapter<String>(this, R.layout.spinner_item, list_category);

        spinner_category= (Spinner) findViewById(R.id.spinner_category);

        arrayAdapter_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_category.setAdapter(arrayAdapter_category);
        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spUserLocalDB = getApplicationContext().getSharedPreferences(SP_NAME, 0);
                if (position==0){
                    list_subcategory_home = new ArrayList<>();
                    list_subcategory_home.add(getResources().getString(R.string.bedroom));
                    list_subcategory_home.add(getResources().getString(R.string.dining_room));
                    list_subcategory_home.add(getResources().getString(R.string.dressing_room));
                    list_subcategory_home.add(getResources().getString(R.string.kitchen_room));
                    list_subcategory_home.add(getResources().getString(R.string.laundry_room));
                    list_subcategory_home.add(getResources().getString(R.string.lounge));
                    list_subcategory_home.add(getResources().getString(R.string.office));
                    list_subcategory_home.add(getResources().getString(R.string.bathroom));
                    list_subcategory_home.add(getResources().getString(R.string.toilet));
                    list_subcategory_home.add(getResources().getString(R.string.attic));
                    list_subcategory_home.add(getResources().getString(R.string.cellar));
                    list_subcategory_home.add(getResources().getString(R.string.other));

                    list_subcategory = list_subcategory_home;
                    list_selected_subcategory_name = list_subcategory_home_name;
                }
                else if( position==1){
                    list_subcategory = list_subcategory_vehicles;
                    list_selected_subcategory_name = list_subcategory_vehicles_name;
                }

                if(i!=0) {
                    //Log.d("SPINNER", "position: " + position + " list_subcat item1: " + list_subcategory.get(0));
                    arrayAdapter_subCategory.clear();
                    arrayAdapter_subCategory.addAll(list_subcategory);
                }
                i++;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //---------------------------------------------------------------------------------
        //----------------  Sub Category ------------------------------------------------------
        //---------------------------------------------------------------------------------

        list_subcategory = list_subcategory_home;

        arrayAdapter_subCategory = new ArrayAdapter<String>(this, R.layout.spinner_item, list_subcategory);
        spinner_subcategory= (Spinner) findViewById(R.id.spinner_subcategory);

        arrayAdapter_subCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_subcategory.setAdapter(arrayAdapter_subCategory);
        spinner_subcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //------------------------------------------------------------------------------------
        //------------------------------------------------------------------------------------

        btn_save = (Button) findViewById(R.id.btn_save_aube);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //--------------> UserDevicesDB  userDevicesDB = null;
                String addrDev = selected_aube.getAddress();
                String nameDev = et_name.getText().toString();
                String classDev = spinner_category.getSelectedItem().toString();

                String classDevName = list_category_name.get(spinner_category.getSelectedItemPosition());
                //Log.e("SAVE_AUBE", "category : " + classDevName );

                String subClassDev = spinner_subcategory.getSelectedItem().toString();
                String subClassDevName = list_selected_subcategory_name.get(spinner_subcategory.getSelectedItemPosition());
                //Log.e("SAVE_AUBE", "category : " + subClassDevName );

                String idUser = spUserLocalDB.getString("idUser", "");

                if(nameDev.equals("")){
                    //Log.d("ETNAME", "etname is empty");
                    et_name.setError("Empty field!");
                }
                else{
                    //Log.d("ETNAME", "etname is not empty: +"+nameDev+"+");

                    //------------------> userDevicesDB = datasource.createUserDeviceInDB(addrDev, nameDev, classDev, subClassDev, idUser);

                    //boolean isInserted = datasource.createUserDeviceInDB(addrDev, nameDev, classDev, subClassDev, idUser);
                    //---------------------------------------------------------------------------------------------
                    // add Device to sqlite database
                    //---------------------------------------------------------------------------------------------
                    boolean isInserted = dbHelper.insertDevice(addrDev,nameDev,classDevName,subClassDevName,idUser);
                    //---------------------------------------------------------------------------------------------
                    // insert device into sql DB
                    if(networkStateHandler.isNetworkAvailable(getApplicationContext())) {
                        //Log.d("INTERNET STATUS", "------------> CONNECTED");

                        if (syncController.isDeviceNeedExport()) {
                            Log.d("SYNC DEVICE STATUS", "---------------> Devices table need to be sync");
                            dbHelper.exportDevicesFromSQLiteToSQLDB();
                            Log.d("SYNC DEVICE STATUS", "---------------> export done !!!");
                        }
                    }


                    if(isInserted) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    else
                    {
                        Toast.makeText(RecordAubeActivity.this, "An error occured, please try again.", Toast.LENGTH_SHORT).show();
                    }
                }


                //Log.d("DATA", "addrDev: " + addrDev+ " nameDev: "+ nameDev + " classDev: "+ classDev+ " subClassDev: "+ subClassDev + " idUser: "+ idUser);


            }
        });
    }

}
