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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

public class TestActivity extends Activity{
    PopupWindow pw;
    PopupWindow pw2;
    PopupWindow newpw;
    Cursor cur;
    String nametxt;
    int userID;
    String password;
    String acode;
    private ProgressDialog pDialog;
    Bitmap bmp;
    ImageView imgView;
    JSONParser jsonParser = new JSONParser();
    int addID;
    private String[] addinfo = new String[18];
    private String[][] fulladdinfo;
    boolean added = false;
    private SQLiteDatabase database;
    private DBStuff dbhelper;
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "glvl1", "geek2", "glvl2", "geek3", "glvl3", 
            "nerd1", "nlvl1", "nerd2", "nlvl2", "nerd3", "nlvl3", 
            "email", "facebook", "phone", "twitter", "linkedin", "googlep", "addcode", "deleted"};
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testthing);
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREFS_NAME,0);
        if(sharedPref.contains("userID")){
            userID = sharedPref.getInt("userID", userID);
            password = sharedPref.getString("password", password);
        }
        dbhelper = new DBStuff(this);
        opendb();
        imgView = (ImageView) findViewById(R.id.testimage);
        Button download = (Button) findViewById(R.id.testDownload);
        Button opener = (Button) findViewById(R.id.testOpen);
        download.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new SyncUserCards().execute(); 
               //finish(); 
            }//end of onClick
        });//end of onClickListener
        opener.setOnClickListener(new OnClickListener(){
           public void onClick(View v){
               setImg();
               new SyncUserCards().execute();
               //finish();
           }
        });
    }
    
    public void setImg(){
        Drawable d = getResources().getDrawable(R.drawable.ic_launcher);
        imgView.setImageDrawable(d);
    }
    
 /****************Add card info to idb*************/
    
    class SyncUserCards extends AsyncTask<String, String, String> {
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TestActivity.this);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userID", Integer.toString(userID)));
            params.add(new BasicNameValuePair("password", password));
         
            
            // getting JSON Object
            JSONObject json = jsonParser.makeHttpRequest(RegularUse.SYNCFROMEDB,"POST", params);
            // check log cat for response
            Log.d("Create Response", json.toString());

            //see if a connection returned anything
            if(json != null){

                try {
                    // check for success tag
    
                    int success = json.getInt(RegularUse.TAG_SUCCESS);
                    
                    if (success > 0) {
                        //fulladdinfo = new String[success][];
                        for(int j = 0; j<success; j++){
                        String username = "userinfo" + Integer.toString(j);
                        JSONArray userObj = json.getJSONArray(username); // JSON Array
                        JSONObject userinfo = userObj.getJSONObject(0);
                        
                        
                        addID = userinfo.getInt("_ID");
                        //download image of top half
                        saveBitmapFromURL(addID);
                        
                        if(userinfo.getString("addfname") != "null" && userinfo.getString("addfname")!= null){
                            addinfo[0] = userinfo.getString("addfname");
                        }
                        if(userinfo.getString("addmname") != "null" && userinfo.getString("addmname") != null){
                            addinfo[1] = userinfo.getString("addmname");
                        }
                        if(userinfo.getString("addlname") != "null"){
                            addinfo[2] = userinfo.getString("addlname");
                        }
                        if(userinfo.getString("addsname") != "null" && userinfo.getString("addsname") != null){
                            addinfo[3] = userinfo.getString("addsname");
                        }
                        if(userinfo.getString("addtitle") != "null"){
                            addinfo[4] = userinfo.getString("addtitle");
                        }
                        if(userinfo.getString("addorganization") != "null"){
                            addinfo[5] = userinfo.getString("addorganization");
                        }
                        if(userinfo.getString("addnerd1") != "null"){
                            addinfo[6] = userinfo.getString("addnerd1");
                        }
                        if(userinfo.getString("addnerd2") != "null"){
                            addinfo[7] = userinfo.getString("addnerd2");
                        }
                        if(userinfo.getString("addnerd3") != "null"){
                            addinfo[8] = userinfo.getString("addnerd3");
                        }
                        if(userinfo.getString("addgeek1") != "null"){
                            addinfo[9] = userinfo.getString("addgeek1");
                        }
                        if(userinfo.getString("addgeek2") != "null"){
                            addinfo[10] = userinfo.getString("addgeek2");
                        }
                        if(userinfo.getString("addgeek3") != "null"){
                            addinfo[11] = userinfo.getString("addgeek3");
                        }
                        if(userinfo.getString("addemail") != "null"){
                            addinfo[12] = userinfo.getString("addemail");
                        }
                        if(userinfo.getString("addfacebook") != "null"){
                            addinfo[13] = userinfo.getString("addfacebook");
                        }
                        if(userinfo.getString("addgooglep") != "null"){
                            addinfo[14] = userinfo.getString("addgooglep");
                        }
                        if(userinfo.getString("addtwitter") != "null"){
                            addinfo[15] = userinfo.getString("addtwitter");
                        }
                        if(userinfo.getString("addphone") != "null"){
                            addinfo[16] = userinfo.getString("addphone");
                        }
                        if(userinfo.getString("addlinkedin") != "null"){
                            addinfo[17] = userinfo.getString("addlinkedin");
                        }
                        createRow(addinfo);
                        added = true;
                        //fulladdinfo[j] = addinfo;
                        }//end of for loop
                        
                    
                    }
                    else{ //then it could not find card in the database
                        //no card message
                        //pop up? maybe reuse pop-up for show addcode?
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }

            return null;
           
        }
 
        
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            if(added){            
            //    //then save any info returned to idb
                //createRow(addinfo);
            }
            pDialog.dismiss();
        }
 
    }
    
/***********************ESSENTIAL DB STUFF***********************/
    
    public void deleteRow(int ID){
        database.delete(DBStuff.TABLENAME,  "_ID = " + ID, null);
    }
    
    public long replaceVals(int id, String[] colvals){
        ContentValues values = new ContentValues();
        String[] temparg = { Integer.toString(id)};
        cur = database.query(DBStuff.TABLENAME, null, "_ID = ?", temparg, null, null, null);
        for(int i = 0; i<colvals.length; i++){
            values.put(colnames[i], colvals[i]);
        }
        cur.moveToFirst();
        for(int j = colvals.length; j < colnames.length; j++){
            values.put(colnames[j], cur.getString(cur.getColumnIndex(colnames[j])));
        }
        values.put("_ID", id);
        long insertId = database.replace(DBStuff.TABLENAME, null, values);
        return insertId;
    }
    
    public long createRow(String[] colvals) {
        ContentValues values = new ContentValues();
        values.put("_ID", addID);
        values.put("fname", addinfo[0]);
        values.put("mname", addinfo[1]);
        values.put("lname", addinfo[2]);
        values.put("sname", addinfo[3]);
        values.put("title", addinfo[4]);
        values.put("organization", addinfo[5]);
        values.put("nerd1", addinfo[6] );
        values.put("nerd2", addinfo[7] );
        values.put("nerd3", addinfo[8] );
        values.put("geek1", addinfo[9] );
        values.put("geek2", addinfo[10]);
        values.put("geek3", addinfo[11]);
        values.put("email", addinfo[12]);
        values.put("facebook", addinfo[13]);
        values.put("googlep", addinfo[14]);
        values.put("twitter", addinfo[15]);
        values.put("phone", addinfo[16]);
        values.put("linkedin", addinfo[17]);
        long insertId = database.insert(DBStuff.TABLENAME, null, values);
        return insertId;
    }
    
    public void opendb() throws SQLException {
        database = dbhelper.getWritableDatabase();
    }
    
    public void closedb() {
        dbhelper.close();
    }
    
    public void saveBitmapFromURL(int userID) {
        /*--- this method downloads an Image from the given URL, 
         *  then decodes and returns a Bitmap object
         ---*/
        
          String link = "http://www.boffinapes.com/gcstuff/";
          Display display = getWindowManager().getDefaultDisplay();
          int width = display.getWidth();
          int height = display.getHeight();
          if(((double) width/height) == (3/2) || ((double) height/width) == (3/2)){
              link = link + "2/";
          }
          else if(((double) width/height) == (4/3) || ((double) height/width) == (4/3)){
              link = link + "3/";
          }
          else if(((double) width/height) == (16/9) || ((double) height/width) == (16/9)){
              link = link + "9/";
          }
          else{
              link = link + "10/";
          }
          link = link + Integer.toString((userID%10)) + "/";
          link = link + Integer.toString(userID) + ".png";
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            
            InputStream input = connection.getInputStream();
            
            Bitmap bmp = BitmapFactory.decodeStream(input);
            FileOutputStream out = null;
            try {
                File imgFile = new File(TestActivity.this.getFilesDir(), userID + ".png");
                out = new FileOutputStream(imgFile);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("file issue", e.getMessage().toString());
            }                
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("getBmpFromUrl error: ", e.getMessage().toString());
        }
    
    }
}//end of doInBackground

