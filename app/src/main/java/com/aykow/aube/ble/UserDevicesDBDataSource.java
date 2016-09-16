package com.aykow.aube.ble;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarah on 28/04/2016.
 */
public class UserDevicesDBDataSource {

   //-------> private SQLiteDatabase database;

   //------> private  String[] allColumns = { DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_BTD_ADDR, DatabaseHelper.COLUMN_NAME_BTD,
   //---------->         DatabaseHelper.COLUMN_CLASS_D, DatabaseHelper.COLUMN_SUB_CLASS_D, DatabaseHelper.COLUMN_ID_USER};

   /*------------- private DatabaseHelper dbHelper;

    public UserDevicesDBDataSource(Context context)
    {
        dbHelper =DatabaseHelper.getDbHelperInstance(context);
    }----------------------*/

    /*---->public void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public UserDevicesDB createUserDeviceInDB(String addrDev, String nameDev, String classDev, String subClassDev, String idUserDev){

        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_BTD_ADDR, addrDev);
        values.put(DatabaseHelper.COLUMN_NAME_BTD, nameDev);
        values.put(DatabaseHelper.COLUMN_CLASS_D, classDev);
        values.put(DatabaseHelper.COLUMN_SUB_CLASS_D, subClassDev);
        values.put(DatabaseHelper.COLUMN_ID_USER,idUserDev);

        long insertId = database.insert(DatabaseHelper.TABLE_USER_DEVICES, null, values);

        Cursor cursor = database.query(DatabaseHelper.TABLE_USER_DEVICES, allColumns, DatabaseHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
        cursor.moveToFirst();
        UserDevicesDB newUserDevicesDB = cursorToUserDevicesDB(cursor);
        cursor.close();

        return newUserDevicesDB;
    }<-----*/



    /*---------->public void deleteUserDeviceInDB(UserDevicesDB userDevicesDB)
    {
        long id = userDevicesDB.getIdDev();
        Log.d("Delete userDeviceDB", "userDevicesDB deleted with id: "+id);
        database.delete(DatabaseHelper.TABLE_USER_DEVICES, DatabaseHelper.COLUMN_ID + " = " + id, null);
    }<---------------*/

    /*----------->public List<UserDevicesDB> getAllUserDevicesDB(){
        List<UserDevicesDB> userDevicesDBList = new ArrayList<UserDevicesDB>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_USER_DEVICES, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            UserDevicesDB userDevicesDB = cursorToUserDevicesDB(cursor);
            userDevicesDBList.add(userDevicesDB);
            cursor.moveToNext();
        }

        cursor.close();
        return userDevicesDBList;
    }

    private UserDevicesDB cursorToUserDevicesDB(Cursor cursor) {

        UserDevicesDB userDevicesDB = new UserDevicesDB();
        userDevicesDB.setIdDev(cursor.getLong(0));
        userDevicesDB.setAddr(cursor.getString(1));
        userDevicesDB.setNameDev(cursor.getString(2));
        userDevicesDB.setClassDev(cursor.getString(3));
        userDevicesDB.setSubClassDev(cursor.getString(4));
        userDevicesDB.setIdUserDev(cursor.getString(5));

        return userDevicesDB;
    }<---------------------------*/

  /*------------  public boolean createUserDeviceInDB(String addrDev, String nameDev, String classDev, String subClassDev, String idUserDev){
        boolean result = dbHelper.insertDevice(addrDev, nameDev, classDev, subClassDev,idUserDev);
        return  result;
    }

    public List<UserDevicesDB> getAllDevicesInDB(){
        Cursor result = dbHelper.getAllDevices();
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

    public List<UserDevicesDB> getAllHomeDevicesInDB(){
        Cursor result = dbHelper.getAllHomeDevices();
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

    public List<UserDevicesDB> getAllCarDevicesInDB(){
        Cursor result = dbHelper.getAllCarDevices();
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
    }----------------------*/

}
