package com.aykow.aube.ble;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by sarah on 09/03/2016.
 */
public class UserLocalStore {

    public static final String SP_NAME="userDetails";
    SharedPreferences spUserLocalDB;

    public UserLocalStore(Context context){
        spUserLocalDB = context.getSharedPreferences(SP_NAME,0);
    }

    public void storeUserData(User user){

        SharedPreferences.Editor spEditor = spUserLocalDB.edit();
        spEditor.putString("firstName", user.firstName);
        Log.d("SharedPreferences", "FirstName: " + user.firstName);

        spEditor.putString("lastName", user.lastName);
        Log.d("SharedPreferences", "LastName: " + user.lastName);

        spEditor.putString("email", user.email);
        Log.d("SharedPreferences", "Email: " + user.email);

        spEditor.putString("idUser", user.idUser);
        Log.d("SharedPreferences", "idUser: " + user.idUser);

        spEditor.putString("birthDate", user.birthDate);
        Log.d("SharedPreferences", "BirthDate: " + user.birthDate);

        spEditor.putString("address", user.address);
        Log.d("SharedPreferences", "Address: " + user.address);

        spEditor.commit();

        Log.d("SharedPreferences", "idUser in SP: " + spUserLocalDB.getString("idUser",null));
    }

    public User getUserLoggedInUser(){
        String email = spUserLocalDB.getString("email", "");
        String firstName = spUserLocalDB.getString("firstName", "");
        String lastName = spUserLocalDB.getString("lastName", "");
        String idUser = spUserLocalDB.getString("idUser","");
        String birthDate = spUserLocalDB.getString("birthDate", "");
        String address = spUserLocalDB.getString("address", "");

        User storedUser = new User(firstName, lastName, email, idUser, birthDate,address,true);
        return storedUser;
    }

    public void setIsUserLoggedIn(boolean loggedIn){
        SharedPreferences.Editor spEditor = spUserLocalDB.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.commit();
    }

    public boolean getIsUserLoggedIn(){
        if(spUserLocalDB.getBoolean("loggedIn", false)==true){
            return true;
        }
        else{
            return false;
        }
    }

    public void clearSpUserData(){
        SharedPreferences.Editor spEditor = spUserLocalDB.edit();
        spEditor.clear();
        spEditor.commit();
    }
}
