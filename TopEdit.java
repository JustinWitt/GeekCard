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
import android.os.Handler;
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

public class TopEdit extends Activity {
    EditText fnameEdit;
    EditText mnameEdit;
    EditText lnameEdit;
    EditText snameEdit;
    EditText titleEdit;
    EditText orgEdit;
    String fname = "";
    String mname = "";
    String lname = "";
    String sname = "";
    String title = "";
    String org = "";
    String[] info = new String[6];
    PopupWindow pw;
    private SQLiteDatabase database;
    private DBStuff dbhelper;
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "glvl1", "geek2", "glvl2", "geek3", "glvl3", 
            "nerd1", "nlvl1", "nerd2", "nlvl2", "nerd3", "nlvl3", 
            "email", "facebook", "phone", "twitter", "linkedin", "googlep"};
    Cursor cur;
    private int userID = 0;
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
        setContentView(R.layout.topedit);
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREFS_NAME,0);
        if(sharedPref.contains("userID")){
            userID = sharedPref.getInt("userID", userID);
            password = sharedPref.getString("password", password);
        }
        else{
            //popup to ask for last name and new password
            
        }
        dbhelper = new DBStuff(this);
        fnameEdit = (EditText) findViewById(R.id.edit1);
        mnameEdit = (EditText) findViewById(R.id.edit2);
        lnameEdit = (EditText) findViewById(R.id.edit3);
        snameEdit = (EditText) findViewById(R.id.edit4);
        titleEdit = (EditText) findViewById(R.id.edit5);
        orgEdit = (EditText) findViewById(R.id.edit6);
        Button saver = (Button) findViewById(R.id.topeditsave);
        Button clear = (Button) findViewById(R.id.topeditclear);
        
        //if external db is available, populate with that
        if(userID != 0){
            //new GetUserInfo().execute();
        }
        //else populate with internal db
        if(gotInfo == false){
            opendb();
            String[] cols = {"_ID", "fname", "mname", "lname", "sname", "title", "organization"};
            String ider = "_ID = " + Integer.toString(userID);
            cur = database.query(DBStuff.TABLENAME, cols, ider, null, null, null, null);
            if(cur.getCount() > 0){
                cur.moveToFirst();
                fname = cur.getString(1);
                mname = cur.getString(2);
                lname = cur.getString(3);
                sname = cur.getString(4);
                title = cur.getString(5);
                org = cur.getString(6);
            }
            closedb();
        }
        else{
            fname = "crap";
        }
        fnameEdit.setText(fname);
        mnameEdit.setText(mname);
        lnameEdit.setText(lname);
        snameEdit.setText(sname);
        titleEdit.setText(title);
        orgEdit.setText(org);   
        
        
        
        
       clear.isClickable();
       clear.setOnClickListener(new OnClickListener(){
           public void onClick(View v) {
               fnameEdit.setText("");
               mnameEdit.setText("");
               lnameEdit.setText("");
               snameEdit.setText("");
               titleEdit.setText("");
               orgEdit.setText("");
           }
       });

             
       saver.isClickable();
       saver.setOnClickListener(new OnClickListener(){
           public void onClick(View v) {
               info[0] = fnameEdit.getText().toString();
               info[1] = mnameEdit.getText().toString();
               info[2] = lnameEdit.getText().toString();
               info[3] = snameEdit.getText().toString();
               info[4] = titleEdit.getText().toString();
               info[5] = orgEdit.getText().toString();
               if(info[2].equals("") || info[2] == null){
                   savepop();
               }
               else{
                   //update external db
                   new SaveUserInfo().execute();

                   //update internal db
                   opendb();
                   replaceVals(userID,info);
                   closedb();
                   
               }
           }
       });
               
    }
    
    public void finishthis(){
        Intent passer = new Intent(TopEdit.this, RegularUse.class);
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
        for(int i = 0; i<colvals.length; i++){
            values.put(colnames[i], colvals[i]);
        }
        long insertId = database.insert(DBStuff.TABLENAME, null, values);
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
            pDialog = new ProgressDialog(TopEdit.this);
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
                       
                        fname = userinfo.getString("fname");
                        mname = userinfo.getString("mname");
                        lname = userinfo.getString("lname");
                        sname = userinfo.getString("sname");
                        title = userinfo.getString("title");
                        org = userinfo.getString("organization");
                        
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
            if(fname != "null"){
                fnameEdit.setText(fname);
            }
            if(mname != "null"){
                mnameEdit.setText(mname);
            }
            if(lname != "null"){
                lnameEdit.setText(lname);
            }
            if(sname != "null"){
                snameEdit.setText(sname);
            }
            if(title != "null"){
                titleEdit.setText(title);
            }
            if(org != "null"){
                orgEdit.setText(org);
            }
        }
 
    }


class SaveUserInfo extends AsyncTask<String, String, String> {
    
    /**
     * Before starting background thread Show Progress Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(TopEdit.this);
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
        params.add(new BasicNameValuePair("fname", info[0]));
        params.add(new BasicNameValuePair("mname", info[1]));
        params.add(new BasicNameValuePair("lname", info[2]));
        params.add(new BasicNameValuePair("sname", info[3]));
        params.add(new BasicNameValuePair("title", info[4]));
        params.add(new BasicNameValuePair("organization", info[5]));
        // getting JSON Object
        // Note that create product url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(RegularUse.USERINFOUPDATE,"POST", params);
        
        return null;
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    protected void onPostExecute(String file_url) {
        // dismiss the dialog once done
        saveBitmapFromURL(userID);
        pDialog.dismiss();
        finishthis();
    }
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
        bmp = BitmapFactory.decodeStream(input);
        FileOutputStream out = null;
        try {
            File imgFile = new File(TopEdit.this.getFilesDir(), userID + ".png");
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


}
