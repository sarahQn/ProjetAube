package com.aykow.aube.ble;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.koushikdutta.async.http.AsyncHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by sarah on 25/03/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static  DatabaseHelper dbHelperInstance;

    //-----------------------------------------------------------------------------------------
    // callbacks
    //-----------------------------------------------------------------------------------------

    public static interface DeviceExportCallback{
        public void onFinishExportDevice(boolean isFinished);
    }
    public static interface DeviceImportCallback{
        public void onFinishImportDevice(boolean isFinished);
    }

    public static interface AqExportCallback{
        public void onFinishExportAq(boolean isFinished);
    }
    public static interface AqImportCallback{
        public void onFinishImportAq(boolean isFinished);
    }

    //-----------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------

    public static final String PREFERENCES = "MyPrefs" ;
    public static final String SPUSERDETAILS = "userDetails";
    public static final String CheckForDeviceImport = "checkForDeviceImport";
    public static final String CheckForDeviceExport = "checkForDeviceExport";
    public static final String CheckForAQImport = "checkForAQImport";
    public static final String CheckForAQExport = "checkForAQExport";
    SharedPreferences sharedpreferences;
    SharedPreferences spUserDetails;
    public static Context contextApp;

    public static synchronized DatabaseHelper getDbHelperInstance(Context context){
        contextApp = context;
        if(dbHelperInstance == null){

            dbHelperInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return dbHelperInstance;
    }

    private static final String DATABASE_NAME = "Aube.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USER_DEVICES = "AubeUserDevices";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_BTD_ADDR = "btd_addr";
    public static final String COLUMN_NAME_BTD = "name_btd";
    public static final String COLUMN_CLASS_D = "class_d";
    public static final String COLUMN_SUB_CLASS_D = "sub_class_d";
    public static final String COLUMN_ID_USER = "id_user";
    public static final String COLUMN_SYNC_IN_DB = "sync_in_db";
    public static final String COLUMN_DELETED ="deleted";
    public static final String COLUMN_TS_LAST_MODIF = "ts_last_modif";


    public static final String TABLE_AQ_DATA = "AubeQaData";
    public static  final String COLUMN_AQ = "aq_data";
    public static final String COLUMN_TS = "ts_data";


    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_TABLE_USER_DEVICES = "create table " + TABLE_USER_DEVICES + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_BTD_ADDR + " TEXT, "
                + COLUMN_NAME_BTD + " TEXT, "
                + COLUMN_CLASS_D + " TEXT, "
                +COLUMN_SUB_CLASS_D + " TEXT, "
                +COLUMN_ID_USER + " TEXT,"
                +COLUMN_SYNC_IN_DB + " TEXT, "
                +COLUMN_DELETED + " TEXT, "
                +COLUMN_TS_LAST_MODIF+ " INT);";

        db.execSQL(DATABASE_CREATE_TABLE_USER_DEVICES);
        Log.d("BDD SQLITE Devices", "---------------> is created !!");

        String DATABASE_CREATE_TABLE_AQ_DATA = "create table " + TABLE_AQ_DATA + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_BTD_ADDR + " TEXT, "
                + COLUMN_AQ + " INT, "
                + COLUMN_TS + " INT, "
                + COLUMN_ID_USER + " TEXT,"
                + COLUMN_SYNC_IN_DB + " TEXT);";

        db.execSQL(DATABASE_CREATE_TABLE_AQ_DATA);
        Log.d("BDD SQLITE AQ", "---------------> is created !!");

        // To check for column name on aq table
        /*
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_AQ_DATA + " WHERE 0", null);
        try {
            String[] columnNames = c.getColumnNames();
            for (int i=0 ; i< columnNames.length;  i++)
            {
                Log.d("COLUMN AQ TABLE "+i, "Name: "+ columnNames[i]);
            }
        } finally {
            c.close();
        }*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query;
        query = "DROP TABLE IF EXISTS "+ TABLE_USER_DEVICES;
        db.execSQL(query);
        onCreate(db);

        String queryQa;
        queryQa = "DROP TABLE IF EXISTS "+ TABLE_AQ_DATA;
        db.execSQL(queryQa);
        onCreate(db);
    }

    public boolean insertDevice(String addr, String name, String category, String subCategory, String idUser){
        long tsLastModif = System.currentTimeMillis();
        tsLastModif = tsLastModif/1000;
        Log.e("TIMESTAMPS", "-------- Long ---------> " + tsLastModif + " <----------------------------");
        Log.e("TIMESTAMPS", "---------- String -------> " + String.valueOf(tsLastModif) + " <----------------------------");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BTD_ADDR, addr);
        contentValues.put(COLUMN_NAME_BTD, name);
        contentValues.put(COLUMN_CLASS_D, category);
        contentValues.put(COLUMN_SUB_CLASS_D, subCategory);
        contentValues.put(COLUMN_ID_USER, idUser);
        contentValues.put(COLUMN_SYNC_IN_DB, "no");
        contentValues.put(COLUMN_DELETED, "no");
        contentValues.put(COLUMN_TS_LAST_MODIF, tsLastModif);

        long result = db.insert(TABLE_USER_DEVICES, null, contentValues);
        db.close();

        if(result == -1)
        {
            return  false;
        }
        else {
            return true;
        }
    }

    public boolean insertDevice(String addr, String name, String category, String subCategory, String idUser, String deleted, int tsLastModif){
        Log.e("TIMESTAMPS", "-------- Long ---------> " + tsLastModif + " <----------------------------");
        Log.e("TIMESTAMPS", "---------- String -------> " + String.valueOf(tsLastModif) + " <----------------------------");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BTD_ADDR, addr);
        contentValues.put(COLUMN_NAME_BTD, name);
        contentValues.put(COLUMN_CLASS_D, category);
        contentValues.put(COLUMN_SUB_CLASS_D, subCategory);
        contentValues.put(COLUMN_ID_USER, idUser);
        contentValues.put(COLUMN_SYNC_IN_DB, "yes");
        contentValues.put(COLUMN_DELETED, deleted);
        contentValues.put(COLUMN_TS_LAST_MODIF, tsLastModif);
        Log.d("INSERT DEVICE", "insert into table device , the "+ name);
        long result = db.insert(TABLE_USER_DEVICES, null, contentValues);
        db.close();

        if(result == -1)
        {
            Log.d("INSERT DEVICE","Failed");
            return  false;
        }
        else {
            Log.d("INSERT DEVICE","Success");
            return true;
        }
    }

    public boolean updateDevice(String addr, String name, String category, String subCategory, String idUser)
    {
        long tsLastModif = System.currentTimeMillis();
        tsLastModif = tsLastModif/1000;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BTD_ADDR, addr);
        contentValues.put(COLUMN_NAME_BTD, name);
        contentValues.put(COLUMN_CLASS_D, category);
        contentValues.put(COLUMN_SUB_CLASS_D, subCategory);
        contentValues.put(COLUMN_ID_USER, idUser);
        contentValues.put(COLUMN_SYNC_IN_DB, "no");
        contentValues.put(COLUMN_DELETED,"no");
        contentValues.put(COLUMN_TS_LAST_MODIF, tsLastModif);
        long result = db.update(TABLE_USER_DEVICES, contentValues, COLUMN_BTD_ADDR + " = '" + addr + "'", null);
        db.close();
        if(result == -1)
        {
            return  false;
        }
        else {
            return true;
        }
    }

    public boolean deleteDevice(String addr)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        long tsLastModif = System.currentTimeMillis();
        contentValues.put(COLUMN_DELETED,"no");
        contentValues.put(COLUMN_TS_LAST_MODIF, tsLastModif);
        long result =  db.update(TABLE_USER_DEVICES,contentValues, COLUMN_BTD_ADDR + " = '" + addr + "'", null); //db.delete(TABLE_USER_DEVICES, COLUMN_BTD_ADDR + " = '" + addr + "'", null);
        db.close();
        if(result == -1)
        {
            return  false;
        }
        else {
            return true;
        }
    }

    public Cursor getAllDevices(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_USER_DEVICES, null);
        return result;
    }

    public int getCountDeviceSQLite(){
        Cursor result = getAllDevices();
        return result.getCount();
    }

    public List<UserDevicesDB> getAllDevicesInDbSQLite(){
        Cursor result = getAllDevices();
        List<UserDevicesDB> userDevicesDBList= new ArrayList<UserDevicesDB>();
        if(result.getCount() != 0){
            //show message
            while (result.moveToNext()){
                UserDevicesDB userDevicesDB = new UserDevicesDB();

                userDevicesDB.setIdDev(result.getLong(0));
                userDevicesDB.setAddr(result.getString(1));
                userDevicesDB.setNameDev(result.getString(2));
                userDevicesDB.setClassDev(result.getString(3));
                userDevicesDB.setSubClassDev(result.getString(4));
                userDevicesDB.setIdUserDev(result.getString(5));

                userDevicesDBList.add(userDevicesDB);

            }
        }

        return userDevicesDBList;
    }

    public Cursor getAllHomeDevices(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_USER_DEVICES + " WHERE " + COLUMN_CLASS_D + " = '" + "home" + "'", null);
        return result;
    }

    public List<UserDevicesDB> getAllHomeDevicesInDdSQLite(){
        Cursor result = getAllHomeDevices();
        List<UserDevicesDB> userDevicesDBList = new ArrayList<UserDevicesDB>();
        if (result.getCount() != 0) {
            //show message
            while (result.moveToNext()){
                UserDevicesDB userDevicesDB = new UserDevicesDB();

                userDevicesDB.setIdDev(result.getLong(0));
                userDevicesDB.setAddr(result.getString(1));
                userDevicesDB.setNameDev(result.getString(2));
                userDevicesDB.setClassDev(result.getString(3));
                userDevicesDB.setSubClassDev(result.getString(4));
                userDevicesDB.setIdUserDev(result.getString(5));

                userDevicesDBList.add(userDevicesDB);

            }
        }

        return userDevicesDBList;
    }

    public Cursor getAllCarDevices(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_USER_DEVICES + " WHERE "+ COLUMN_CLASS_D + " = '"+"car"+"'", null);
        return result;
    }

    public List<UserDevicesDB> getAllCarDevicesInDbSQLite(){
        Cursor result = getAllCarDevices();
        List<UserDevicesDB> userDevicesDBList= new ArrayList<UserDevicesDB>();
        if(result.getCount() != 0){
            //show message
            while (result.moveToNext()){
                UserDevicesDB userDevicesDB = new UserDevicesDB();

                userDevicesDB.setIdDev(result.getLong(0));
                userDevicesDB.setAddr(result.getString(1));
                userDevicesDB.setNameDev(result.getString(2));
                userDevicesDB.setClassDev(result.getString(3));
                userDevicesDB.setSubClassDev(result.getString(4));
                userDevicesDB.setIdUserDev(result.getString(5));

                userDevicesDBList.add(userDevicesDB);
            }
        }
        return userDevicesDBList;
    }

    public String composeDeviceJSONfromSQLite(){
        ArrayList<HashMap<String, String>> deviceList;
        deviceList = new ArrayList<HashMap<String, String>>();
        spUserDetails = contextApp.getSharedPreferences(SPUSERDETAILS, Context.MODE_PRIVATE);
        String idUser = spUserDetails.getString("idUser",null);

        Log.d("DATABASEHELPER"," -------> idUser used in composeDeviceJson : "+ idUser);
        String selectQuery = "SELECT  * FROM "+ TABLE_USER_DEVICES +" where " + COLUMN_ID_USER+ " = " + idUser +" and "+ COLUMN_SYNC_IN_DB +" = '"+"no"+"'";



        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("JSON Device", "nb row : " + cursor.getCount());// <--------------------------------
        if(cursor.moveToFirst()){
            do{
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(COLUMN_ID,cursor.getString(0));
                map.put(COLUMN_BTD_ADDR,cursor.getString(1));
                map.put(COLUMN_NAME_BTD,cursor.getString(2));
                map.put(COLUMN_CLASS_D ,cursor.getString(3));
                map.put(COLUMN_SUB_CLASS_D,cursor.getString(4));
                map.put(COLUMN_ID_USER,cursor.getString(5));
                map.put(COLUMN_SYNC_IN_DB,cursor.getString(6));
                map.put(COLUMN_TS_LAST_MODIF,cursor.getString(8));
                deviceList.add(map);

            }while (cursor.moveToNext());
        }
        db.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(deviceList);
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbDeviceNeedExportCount(){

        SQLiteDatabase database = this.getWritableDatabase();


        //-------------------------------------------------------------
        Cursor curCSV = database.rawQuery("select * from "+ TABLE_USER_DEVICES, null);

        while (curCSV.moveToNext())
        {
            int id = curCSV.getInt(curCSV.getColumnIndex(COLUMN_ID));
            String nameBtd = curCSV.getString(curCSV.getColumnIndex(COLUMN_NAME_BTD));
            String idUser = curCSV.getString(curCSV.getColumnIndex(COLUMN_ID_USER));
            Log.d("Table devices", "id : "+ id + " || name_btd : "+ nameBtd+ " || id_user : "+idUser);
        }
        curCSV.close();

        //-------------------------------------------------------------
        spUserDetails = contextApp.getSharedPreferences(SPUSERDETAILS, Context.MODE_PRIVATE);
        String idUserSP = spUserDetails.getString("idUser",null);
        Log.d("DbHelper ","id user in shared preferences: " +idUserSP );

        int count = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_USER_DEVICES + " where " + COLUMN_ID_USER + " = " +idUserSP + " and " + COLUMN_SYNC_IN_DB + " = '"+"no"+"'";

        Cursor dbCursor = database.query(TABLE_USER_DEVICES,null, null, null, null, null,null);
        String[] columnNames = dbCursor.getColumnNames();
        for(int i=0; i<columnNames.length; i++)
        {
            Log.d("----DBHelper----", "Column "+ (i+1)+ " : "+columnNames[i]);
        }
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        Log.d("DATABASE HELPER", "count of device need export: "+ count);
        return count;
    }


    //------------------------------------------------------------------------------------------------------------
    //
    //     Export Device data from SQLite DB to SQL
    //
    //------------------------------------------------------------------------------------------------------------

    public void exportDevicesFromSQLiteToSQLDB(final DeviceExportCallback callback){
        Log.w("composeDeviceJSON", composeDeviceJSONfromSQLite());
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("deviceJSON", composeDeviceJSONfromSQLite());
        client.post("http://aykow.fr/Aube/Application/Bdd/insertdevice.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("RESULT INSERT DB SQL", "result success : " + new String(responseBody));
                Log.d("DATABASEHELPER", "devise added successfully!!! <---------------------");
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        Log.e("composeDeviceJsonResult", "id: "+jsonObj.get("id").toString() + " status: "+jsonObj.get("status").toString());
                        updateDeviceSyncStatus(jsonObj.get("id").toString(), jsonObj.get("status").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /* //used in callback, otherwise uncomment
                sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceExport, true);
                editor.commit();
                Log.d("EXPORT DEVICE", "finished");*/

                //-----------------------------------------------------------------------------------
                callback.onFinishExportDevice(true);
                //-----------------------------------------------------------------------------------
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT INSERT DB", "result failure : " + responseBody.toString());
                /* //used in callback, otherwise uncomment
                sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceExport, true);
                editor.commit();
                Log.d("EXPORT DEVICE", "finished");*/

                //-----------------------------------------------------------------------------------
                callback.onFinishExportDevice(true);
                //-----------------------------------------------------------------------------------
            }
        });

    }


    public void exportDevicesFromSQLiteToSQLDB(){
        Log.e("composeDeviceJSON", composeDeviceJSONfromSQLite());
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("deviceJSON", composeDeviceJSONfromSQLite());
        client.post("http://aykow.fr/Aube/Application/Bdd/insertdevice.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("RESULT INSERT DB SQL", "result success : " + new String(responseBody));
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        updateDeviceSyncStatus(jsonObj.get("id").toString(), jsonObj.get("status").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceExport, true);
                editor.commit();
                Log.d("EXPORT DEVICE", "finished");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT INSERT DB", "result failure : " + responseBody.toString());
                sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceExport, true);
                editor.commit();
                Log.d("EXPORT DEVICE", "finished");
            }
        });

    }

    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateDeviceSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update " + TABLE_USER_DEVICES + " set " + COLUMN_SYNC_IN_DB + " = '"+ status +"' where " + COLUMN_ID + "="+"'"+ id +"'";
        Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    //------------------------------------------------------------------------------------------------------------
    //
    //     Import Device data from SQL DB to SQLite
    //
    //------------------------------------------------------------------------------------------------------------

    /*public void importDevicesFromSQLDBToSQLite(){
        sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        spUserDetails = contextApp.getSharedPreferences(SPUSERDETAILS, Context.MODE_PRIVATE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String idUser = spUserDetails.getString("idUser", null);
        Log.d("DATABASE HELPER", "-------> Import device From sql to sqlite where id: "+idUser);
        params.put("idUser", idUser);

        List<UserDevicesDB> deviceList = getAllDevicesInDbSQLite();
        //params.put("deviceJSON", composeDeviceJSONfromSQLite());
        client.post("http://aykow.fr/Aube/Application/Bdd/getDevices.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("RESULT IMPORT DB", "result Success : " + new String(responseBody));
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        //Log.d("INSERT INTO SQLite", "1 insert data into device table and device name is : "+jsonObj.get("name"));
                        Log.d("INSERT INTO SQLite", "2 insert data into device table and device name is : "+jsonObj.get("name_bd"));
                        insertDevice(jsonObj.get("bd_addr").toString(), jsonObj.get("name_bd").toString(), jsonObj.get("category").toString(),jsonObj.get("sub_class").toString(),jsonObj.get("id_user").toString(), jsonObj.get("deleted").toString(), Integer.parseInt(jsonObj.get("ts_last_modif").toString()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceImport, true);
                editor.commit();
                Log.d("IMPORT DEVICE", "finished");

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT IMPORT DB", "result failure : " + responseBody.toString());
                sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceImport, true);
                editor.commit();
                Log.d("IMPORT DEVICE", "finished");
            }
        });

    }*/



    public void importDevicesFromSQLDBToSQLite(final DeviceImportCallback callback){
        sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        spUserDetails = contextApp.getSharedPreferences(SPUSERDETAILS, Context.MODE_PRIVATE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String idUser = spUserDetails.getString("idUser", null);
        Log.d("DATABASE HELPER", "-------> Import device From sql to sqlite where id: "+idUser);
        params.put("idUser", idUser);

        List<UserDevicesDB> deviceList = getAllDevicesInDbSQLite();
        //params.put("deviceJSON", composeDeviceJSONfromSQLite());
        client.post("http://aykow.fr/Aube/Application/Bdd/getDevices.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("RESULT IMPORT DB", "result Success : " + new String(responseBody));
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        //Log.d("INSERT INTO SQLite", "1 insert data into device table and device name is : "+jsonObj.get("name"));
                        Log.d("INSERT INTO SQLite", "2 insert data into device table and device name is : "+jsonObj.get("name_bd"));
                        insertDevice(jsonObj.get("bd_addr").toString(), jsonObj.get("name_bd").toString(), jsonObj.get("category").toString(),jsonObj.get("sub_class").toString(),jsonObj.get("id_user").toString(), jsonObj.get("deleted").toString(), Integer.parseInt(jsonObj.get("ts_last_modif").toString()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceImport, true);
                editor.commit();
                Log.d("IMPORT DEVICE", "finished");*/

                callback.onFinishImportDevice(true);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT IMPORT DB", "result failure : " + responseBody.toString());
                /*sharedpreferences = contextApp.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForDeviceImport, true);
                editor.commit();
                Log.d("IMPORT DEVICE", "finished");*/

                callback.onFinishImportDevice(true);
            }
        });

    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------- TABLE AQ DATA --------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

   /* public boolean insertAQData(String addr, int aqValue, int ts, String idUser){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BTD_ADDR, addr);
        contentValues.put(COLUMN_AQ, aqValue);
        contentValues.put(COLUMN_TS, ts);
        contentValues.put(COLUMN_ID_USER, idUser);
        contentValues.put(COLUMN_SYNC_IN_DB, "no");
        long result = db.insert(TABLE_AQ_DATA, null, contentValues);
        db.close();

        if(result == -1)
        {
            return  false;
        }
        else {
            return true;
        }
    }*/

    public boolean insertAQData(String addr, int aqValue, int ts, String idUser, String sync){
        if(addr!=null) {
            if(!addr.equals("")) {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_BTD_ADDR, addr);
                contentValues.put(COLUMN_AQ, aqValue);
                contentValues.put(COLUMN_TS, ts);
                contentValues.put(COLUMN_ID_USER, idUser);
                contentValues.put(COLUMN_SYNC_IN_DB, sync);
                long result = db.insert(TABLE_AQ_DATA, null, contentValues);// <----------------------------------------------------
                db.close();

                if (result == -1) {
                    Log.d("DbHelper", "inser Aq value on sqlite => failed");
                    return false;
                } else {
                    Log.d("DbHelper", "inser Aq value on sqlite => successed");
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public Cursor getAllAQdata(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_AQ_DATA , null);
        return result;
    }

    public int getCountAQdataSQLite(){
        Cursor result = getAllAQdata();
        return result.getCount();
    }

    public String composeAQDataJSONfromSQLite(){
        ArrayList<HashMap<String, String>> dataList;
        dataList = new ArrayList<HashMap<String, String>>();
        sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        spUserDetails = contextApp.getSharedPreferences(SPUSERDETAILS, Context.MODE_PRIVATE);
        String idUser = spUserDetails.getString("idUser",null);
        Log.d("DATABASEHELPER"," -------> idUser used in composeAQDataJson : "+ idUser);
        String selectQuery = "SELECT  * FROM "+ TABLE_AQ_DATA +" where " +COLUMN_ID_USER +" = "+ idUser+ " and " + COLUMN_SYNC_IN_DB +" = '"+"no"+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            do{
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(COLUMN_ID, cursor.getString(0));
                map.put(COLUMN_BTD_ADDR,cursor.getString(1));
                map.put(COLUMN_AQ,cursor.getString(2));
                map.put(COLUMN_TS,cursor.getString(3));
                map.put(COLUMN_ID_USER,cursor.getString(4));
                //map.put(COLUMN_SYNC_IN_DB,cursor.getString(5));
                dataList.add(map);

            }while (cursor.moveToNext());
        }
        db.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(dataList);
    }

    public int dbAQDataNeedExportCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_AQ_DATA + " where " + COLUMN_SYNC_IN_DB + " = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }


    public Cursor getAQData28(String critere){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_AQ_DATA + " WHERE " + COLUMN_CLASS_D + " = '" + critere + "'", null);
        return result;
    }

    //---------------------------------------------------------------------
    //
    //    Import AQ Data from SQLite to SQL Database
    //
    //--------------------------------------------------------------------------

    /*public void importAQdataFromSQLDbToSQLite(){
        sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        spUserDetails = contextApp.getSharedPreferences(SPUSERDETAILS, Context.MODE_PRIVATE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String idUser = spUserDetails.getString("idUser", null);
        Log.d("DBHELPER", "id user used to import aq data: "+ idUser);
        params.put("idUser", idUser);
        client.post("http://aykow.fr/Aube/Application/Bdd/getAQdata.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("RESULT IMPORT DB AQ", "result Success : " + new String(responseBody));
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        //Log.d("INSERT INTO SQLite", "1 insert data into device table and device name is : "+jsonObj.get("name"));
                        //insertAQData(jsonObj.get("bd_addr").toString(), jsonObj.get("name_bd").toString(), jsonObj.get("category").toString(),jsonObj.get("sub_class").toString(),jsonObj.get("id_user").toString(), jsonObj.get("deleted").toString(), Integer.parseInt(jsonObj.get("ts_last_modif").toString()));
                        insertAQData(jsonObj.get("bd_addr").toString(),jsonObj.getInt("aq_value"), jsonObj.getInt("ts"),jsonObj.get("id_user").toString(),"yes");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQImport, true);
                editor.commit();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT IMPORT DB", "result failure : " + responseBody.toString());
                sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQImport, true);
                editor.commit();
            }
        });

    }*/

    public void importAQdataFromSQLDbToSQLite(final AqImportCallback callback){
        sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        spUserDetails = contextApp.getSharedPreferences(SPUSERDETAILS, Context.MODE_PRIVATE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        String idUser = spUserDetails.getString("idUser", null);
        params.put("idUser", idUser);
        client.post("http://aykow.fr/Aube/Application/Bdd/getAQdata.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("RESULT IMPORT DB AQ", "result Success : " + new String(responseBody));
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        //Log.d("INSERT INTO SQLite", "1 insert data into device table and device name is : "+jsonObj.get("name"));
                        //insertAQData(jsonObj.get("bd_addr").toString(), jsonObj.get("name_bd").toString(), jsonObj.get("category").toString(),jsonObj.get("sub_class").toString(),jsonObj.get("id_user").toString(), jsonObj.get("deleted").toString(), Integer.parseInt(jsonObj.get("ts_last_modif").toString()));
                        insertAQData(jsonObj.get("bd_addr").toString(),jsonObj.getInt("aq_value"), jsonObj.getInt("ts"),jsonObj.get("id_user").toString(),"yes");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQImport, true);
                editor.commit();*/
                callback.onFinishImportAq(true);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT IMPORT DB", "result failure : " + responseBody.toString());
                /*sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQImport, true);
                editor.commit();*/
                callback.onFinishImportAq(true);
            }
        });

    }



    //---------------------------------------------------------------------
    //
    //    Export AQ Data from SQLite to SQL Database
    //
    //--------------------------------------------------------------------------

    public void exportAQdataFromSQLiteToSQLDB(){
        Log.e("composeAQDataJSON", composeAQDataJSONfromSQLite());
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("AqJSON", composeAQDataJSONfromSQLite());
            client.post("http://aykow.fr/Aube/Application/Bdd/insertAQdata.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Log.d("RESULT INSERT DB SQL", "result success : " + new String(responseBody));
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        updateAQdataSyncStatus(jsonObj.get("id").toString(), jsonObj.get("status").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQExport, true);
                editor.commit();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT INSERT DB", "result failure : " + responseBody.toString());
                sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQExport, true);
                editor.commit();
            }
        });

    }

    public void exportAQdataFromSQLiteToSQLDB(final AqExportCallback callback){
        Log.w("composeAQDataJSON", composeAQDataJSONfromSQLite());
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("AqJSON", composeAQDataJSONfromSQLite());
        client.post("http://aykow.fr/Aube/Application/Bdd/insertAQdata.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Log.d("RESULT INSERT DB SQL", "result success : " + new String(responseBody));
                String result = new String(responseBody);
                Log.d("RESULT INSERT DB", "result success : " + result);

                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        updateAQdataSyncStatus(jsonObj.get("id").toString(), jsonObj.get("status").toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /*sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQExport, true);
                editor.commit();*/
                callback.onFinishExportAq(true);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                Log.d("RESULT INSERT DB", "result failure : " + result);
               /* sharedpreferences = contextApp.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(CheckForAQExport, true);
                editor.commit();*/

                callback.onFinishExportAq(true);
            }
        });

    }

    //-----------------------------------------------------------------------------------------------
    //
    //  Insert into DB SQL single Air quality value
    //
    //-----------------------------------------------------------------------------------------------

    public static interface InsertAQinSqlDbCallback{
        public void onFinishInsertAQinSQL(boolean result);
    }
    public void insertAQintoDbSQL(String addr, int aqValue, int ts, String userId, final InsertAQinSqlDbCallback callback){
        ArrayList<HashMap<String, String>>  dataList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(COLUMN_BTD_ADDR,addr);
        map.put(COLUMN_AQ,String.valueOf(aqValue));
        map.put(COLUMN_TS,String.valueOf(ts));
        map.put(COLUMN_ID_USER,userId);
        //map.put(COLUMN_SYNC_IN_DB,cursor.getString(5));
        dataList.add(map);

        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        final String json = gson.toJson(dataList);

        Log.e("DbHelper Aq insert", "json : "+ json);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("AqJSON", json);
        client.post("http://aykow.fr/Aube/Application/Bdd/insertAQdata.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d("RESULT INSERT DB", "result success : " + result);
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        Log.e("AqJsonResult", "id: "+jsonObj.get("id").toString() + " status: "+jsonObj.get("status").toString());
                        if((jsonObj.get("status").toString()).equals("yes"))
                        {
                            callback.onFinishInsertAQinSQL(true);
                        }
                        else
                        {
                            callback.onFinishInsertAQinSQL(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFinishInsertAQinSQL(false);

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("RESULT INSERT DB", "result failure : " + responseBody.toString());
                callback.onFinishInsertAQinSQL(false);
            }
        });
    }

    /**
     * Update Sync status against each User ID
     * @param id
     * @param status
     */
    public void updateAQdataSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update " + TABLE_AQ_DATA + " set " + COLUMN_SYNC_IN_DB + " = '"+ status +"' where " + COLUMN_ID + "="+"'"+ id +"'";
        //Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }



}
