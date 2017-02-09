package com.boffinapes.geekcard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


public class NGEdit extends Activity{
    private int userID;
    EditText n1Edit;
    EditText n2Edit;
    EditText n3Edit;
    EditText g1Edit;
    EditText g2Edit;
    EditText g3Edit;
    String nerd1 = "";
    String nerd2 = "";
    String nerd3 = "";
    String geek1 = "";
    String geek2 = "";
    String geek3 = "";
    String[] info = new String[6];
    PopupWindow pw;
    private SQLiteDatabase database;
    private DBStuff dbhelper;
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "glvl1", "geek2", "glvl2", "geek3", "glvl3", 
            "nerd1", "nlvl1", "nerd2", "nlvl2", "nerd3", "nlvl3", 
            "email", "facebook", "phone", "twitter", "linkedin", "googlep"};
    Cursor cur;
    private String password = "";
    //private String addcode;
    //for external db stuff
    //InputStream is = null;
    //JSONObject jObj = null;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    boolean gotInfo = false;
    SharedPreferences sharedPref;
    Bitmap bmp;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ngedit);
        
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREFS_NAME,0);
        if(sharedPref.contains("userID")){
            userID = sharedPref.getInt("userID", userID);
            password = sharedPref.getString("password", password);
        }
        else{
            //popup to ask for last name and new password      
        }
        
        dbhelper = new DBStuff(this);
        
        n1Edit = (EditText) findViewById(R.id.ngedit1);
        n2Edit = (EditText) findViewById(R.id.ngedit2);
        n3Edit = (EditText) findViewById(R.id.ngedit3);
        g1Edit = (EditText) findViewById(R.id.ngedit4);
        g2Edit = (EditText) findViewById(R.id.ngedit5);
        g3Edit = (EditText) findViewById(R.id.ngedit6);
        Button saver = (Button) findViewById(R.id.ngeditsave);
        Button clear = (Button) findViewById(R.id.ngeditclear);
        
        //if external db is available, populate with that
        if(userID != 0){
            //new GetUserInfo().execute();
        }
        //else populate with internal db
        if(gotInfo == false){
            opendb();
            String[] cols = {"_ID", "nerd1", "nerd2", "nerd3", "geek1", "geek2", "geek3"};
            String ider = "_ID = " + Integer.toString(userID);
            cur = database.query(DBStuff.TABLENAME, cols, ider, null, null, null, null);
            if(cur.getCount() > 0){
                cur.moveToFirst();
                nerd1 = cur.getString(1);
                nerd2 = cur.getString(2);
                nerd3 = cur.getString(3);
                geek1 = cur.getString(4);
                geek2 = cur.getString(5);
                geek3 = cur.getString(6);
            }
            closedb();
        }
        n1Edit.setText(nerd1);
        n2Edit.setText(nerd2);
        n3Edit.setText(nerd3);
        g1Edit.setText(geek1);
        g2Edit.setText(geek2);
        g3Edit.setText(geek3);   
        
        
        
        
       clear.isClickable();
       clear.setOnClickListener(new OnClickListener(){
           public void onClick(View v) {
               n1Edit.setText("");
               n2Edit.setText("");
               n3Edit.setText("");
               g1Edit.setText("");
               g2Edit.setText("");
               g3Edit.setText("");
           }
       });

             
       saver.isClickable();
       saver.setOnClickListener(new OnClickListener(){
           public void onClick(View v) {
               info[0] = n1Edit.getText().toString();
               info[1] = n2Edit.getText().toString();
               info[2] = n3Edit.getText().toString();
               info[3] = g1Edit.getText().toString();
               info[4] = g2Edit.getText().toString();
               info[5] = g3Edit.getText().toString();
               //update external db
               new SaveUserInfo().execute();
               //saveBitmapFromURL(userID);
               //update internal db
               opendb();
               replaceVals(userID,info);
               closedb();       
           }
        });
          
    }
    
    public void finishthis(){
        Intent passer = new Intent(NGEdit.this, RegularUse.class);
        startActivity(passer);
        finish();
    }
    
    
    public void savepop(){
        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View popup = inflater.inflate(R.layout.savepop, (ViewGroup) findViewById(R.id.savepop));
        pw = new PopupWindow(popup, 500, 175, true);
        LinearLayout topper = (LinearLayout) findViewById(R.id.topedittop);
        pw.showAtLocation(topper, Gravity.CENTER, 0, 0);
        Button oker;
        oker  = (Button) popup.findViewById(R.id.savepopok);
        oker.isClickable();
        oker.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                pw.dismiss();
            }
        });
       
    }
    
    public void createnewpop(){
        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View popup = inflater.inflate(R.layout.lnamepasspop, (ViewGroup) findViewById(R.id.lnamepasspopll));
        pw = new PopupWindow(popup, 500, 175, true);
        LinearLayout topper = (LinearLayout) findViewById(R.id.topedittop);
        pw.showAtLocation(topper, Gravity.CENTER, 0, 0);
        EditText newlnameedit = (EditText) popup.findViewById(R.id.lnamenewpop);
        info[2] = newlnameedit.getText().toString();
        EditText pass1edit = (EditText) popup.findViewById(R.id.passnewpop);
        String pass1 = pass1edit.getText().toString();
        EditText pass2edit = (EditText) popup.findViewById(R.id.passtwonewpop);
        String pass2 = pass2edit.getText().toString();
        if(pass1.equals(pass2) && (!pass1.equals(""))){
            password = pass1;
        }
        else{
            TextView mustmatch = (TextView) popup.findViewById(R.id.newmustmatch);
            mustmatch.setText("Re-enter Password MUST MATCH");
        }
        Button oker;
        oker  = (Button) popup.findViewById(R.id.lnamepasspopbut);
        oker.isClickable();
        oker.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                if(!password.equals("") && !info[2].equals("")){
                    //add
                    SharedPreferences.Editor eddie = sharedPref.edit();
                    eddie.putString("password", password);
                    pw.dismiss();
                }
            }
        });        
    }
    
   /***********************ESSENTIAL DB STUFF***********************/
    
    public void deleteRow(int ID){
        database.delete(DBStuff.TABLENAME,  "_ID = " + ID, null);
    }
    
    public long replaceVals(int id, String[] colvals){
        ContentValues values = new ContentValues();
        String[] temparg = { Integer.toString(id)};
        cur = database.query(DBStuff.TABLENAME, null, "_ID = ?", temparg, null, null, null);
        cur.moveToFirst();
        for(int i = 0; i<colnames.length; i++){
            values.put(colnames[i], cur.getString(cur.getColumnIndex(colnames[i])));
        }
        values.put("nerd1", colvals[0]);
        values.put("nerd2", colvals[1]);
        values.put("nerd3", colvals[2]);
        values.put("geek1", colvals[3]);
        values.put("geek2", colvals[4]);
        values.put("geek3", colvals[5]);
        values.put("_ID", id);
        long insertId = database.replace(DBStuff.TABLENAME, null, values);
        return insertId;
    }
    
    public void opendb() throws SQLException {
        database = dbhelper.getWritableDatabase();
    }
    
    public void closedb() {
        dbhelper.close();
    }
    
    /***************************Asyncs to External DB***********************/
class GetUserInfo extends AsyncTask<String, String, String> {
        
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NGEdit.this);
            pDialog.setMessage("Getting Info...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userID", Integer.toString(userID)));
            params.add(new BasicNameValuePair("password", password));
            //params.add(new BasicNameValuePair("addcode", addcode));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(RegularUse.USERINFOLINK,"POST", params);
            //String json = makeHttpRequest(url_create_product,"POST", params);
            // check log cat for response
            Log.d("Create Response", json.toString());

            //see if a connection returned anything
            if(json != null){
                try {
                    // check for success tag
                    int success = json.getInt(RegularUse.TAG_SUCCESS);
                    
                    if (success == 1) {
                        JSONArray userObj = json.getJSONArray("userinfo"); // JSON Array
                        JSONObject userinfo = userObj.getJSONObject(0);
                       
                        nerd1 = userinfo.getString("nerd1");
                        nerd2 = userinfo.getString("nerd2");
                        nerd3 = userinfo.getString("nerd3");
                        geek1 = userinfo.getString("geek1");
                        geek2 = userinfo.getString("geek2");
                        geek3 = userinfo.getString("geek3");
                        
                        gotInfo = true;
                    } else {
                        // failed to retrieve user info
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }
    
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }
 
    }


class SaveUserInfo extends AsyncTask<String, String, String> {
    
    /**
     * Before starting background thread Show Progress Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(NGEdit.this);
        pDialog.setMessage("Saving Info...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    protected String doInBackground(String... args) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("uid", Integer.toString(userID)));
        params.add(new BasicNameValuePair("password", password));
        //params.add(new BasicNameValuePair("addcode", addcode));
        params.add(new BasicNameValuePair("nerd1", info[0]));
        params.add(new BasicNameValuePair("nerd2", info[1]));
        params.add(new BasicNameValuePair("nerd3", info[2]));
        params.add(new BasicNameValuePair("geek1", info[3]));
        params.add(new BasicNameValuePair("geek2", info[4]));
        params.add(new BasicNameValuePair("geek3", info[5]));
        // getting JSON Object
        // Note that create product url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(RegularUse.USERNGUPDATE,"POST", params);
        
        return null;
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    protected void onPostExecute(String file_url) {
        // dismiss the dialog once done
        pDialog.dismiss();
        finishthis();
    }
}



}
