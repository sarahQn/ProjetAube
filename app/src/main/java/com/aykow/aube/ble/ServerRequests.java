package com.aykow.aube.ble;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.aykow.aube.ble.Callback.GetAqiCallback;
import com.aykow.aube.ble.Callback.GetSendEmailCallback;
import com.aykow.aube.ble.Callback.GetUserCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sarah on 09/03/2016.
 */
public class ServerRequests {

    ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 *15;
    public static final String  SERVER_ADDRESS = "http://aykow.fr/Aube/Application/";
    HttpURLConnection urlConnection = null;

    public ServerRequests(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(context.getString(R.string.processing));
        progressDialog.setMessage(context.getString(R.string.please_wait));

    }
    //--------------------------------------------------------------------------------------------------------
    public void storeUserDataInBackground(User user, GetUserCallback userCallback){
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallback).execute();
    }

    public void fetchUserDataInBackground(User user, GetUserCallback userCallback){
        progressDialog.show();
        new FetchUserDataAsyncTask(user, userCallback).execute();
    }

    public void sendEmailInBackground(String email, GetSendEmailCallback sendEmailCallback){
        new SendEmailAsyncTask(email, sendEmailCallback).execute();
    }
   // public void getAqiData(double lat, double lng, GetAqiCallback getAqiCallback){
   //     new GetAqiAsyncTask(lat,lng,getAqiCallback).execute();
   // }

    //-----------------------------------------------------------------------------------------------------------
    //  Store User in SQLDB
    //
    //-------------------------------------------------------------------------------------------
    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, String>{
        User user;
        GetUserCallback userCallback;

        public StoreUserDataAsyncTask(User user, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }

        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> postDataParams = new HashMap<String, String>();
            postDataParams.put("firstName", user.firstName);
            postDataParams.put("lastName", user.lastName);
            postDataParams.put("email", user.email);
            postDataParams.put("password", user.password);
            postDataParams.put("birthDate", user.birthDate);
            postDataParams.put("address", user.address);
            URL url;
            String response = "";

            try{
                url = new URL(SERVER_ADDRESS+"register.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response = br.readLine();

                }
                else{
                    response = "Error registering";
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e("exception", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("exception", e.getMessage());
            }

            return response;
        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()){
                if(first){
                    first = false;
                }else{
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(),"UTF-8"));
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            userCallback.doneString(result);
        }



    }

    //-----------------------------------------------------------------------------------------------------------
    //      Login by checking if user is in the SQLDB
    //
    //-----------------------------------------------------------------------------------------------------------

    private class FetchUserDataAsyncTask extends  AsyncTask<Void, Void, User>{
        User user;
        GetUserCallback userCallback;

        public FetchUserDataAsyncTask(User user, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }

        @Override
        protected User doInBackground(Void... params) {

            User returnedUser = null;

            HashMap<String, String> postDataParams = new HashMap<String, String>();

            postDataParams.put("email", user.email);
            postDataParams.put("password", user.password);

            URL url;

            String response = "";
            try{
                Log.d("Part2","server request preparing http request");
                url = new URL(SERVER_ADDRESS+"fetchUserData.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                Log.d("Part3", "server request sending http request");
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK)
                {
                    Log.d("Part4", "server request receiving OK from http request");
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response = br.readLine();
                    Log.d("Part5", "server request receiving response: "+response);
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.length() != 0) {
                        String firstName = jsonObject.getString("firstName");
                        Log.d("JSON Data","FirstName: "+firstName);

                        String lastName = jsonObject.getString("lastName");
                        Log.d("JSON Data","LastName: "+lastName);

                        String idUser = jsonObject.getString("idUser");
                        Log.d("JSON Data","User Id : "+idUser);


                        String birthDate = jsonObject.getString("birthDate");
                        String address = jsonObject.getString("address");
                        returnedUser = new User(firstName, lastName, user.email,idUser, birthDate, address,true);
                    }

                }
                else{
                    response = "Error login";
                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e("exception", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("exception", e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            super.onPostExecute(returnedUser);
            progressDialog.dismiss();
            userCallback.done(returnedUser);

        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if(first) {
                    first = false;
                }else{
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(),"UTF-8"));
            }

            return  result.toString();
        }
    }

    //-----------------------------------------------------------------------------------------------
    //  Send email to recover forgotten password
    //
    //-----------------------------------------------------------------------------------------------

    private class SendEmailAsyncTask extends  AsyncTask<Void, Void, String>{
        String email;
        GetSendEmailCallback sendEmailCallback;

        public SendEmailAsyncTask(String email, GetSendEmailCallback sendEmailCallback) {
            this.email= email;
            this.sendEmailCallback = sendEmailCallback;
        }

        @Override
        protected String doInBackground(Void... params) {

            HashMap<String, String> postDataParams = new HashMap<String, String>();

            postDataParams.put("email", email);

            URL url;
            String response = "";

            try{
                url = new URL(SERVER_ADDRESS+"sendEmail.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response = br.readLine();

                }
                else{
                    response = "Error registering";
                }

                Log.d("Response","response"+response);


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e("exception", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("exception", e.getMessage());
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if(first) {
                    first = false;
                }else{
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(),"UTF-8"));
            }

            return  result.toString();
        }
    }

    //---------------------------------------------------------------------------------------------------------
    //  Add device recorded on sqlite database to sql database on server
    //
    //---------------------------------------------------------------------------------------------------------


}
