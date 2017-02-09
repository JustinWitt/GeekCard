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
import android.app.AlertDialog;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class NewUserInternet extends Activity{
    private SharedPreferences sharedPref;
    private String lname = "";
    private String femail = "";
    private String pass1 = "";
    private String pass2 = "";
    private EditText editlname;
    private EditText editfemail;
    private EditText editpass1;
    private EditText editpass2;
    private Button newcreatebutton;
    private int uid = -1;
    private boolean isnew = true;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private String[] privateinfo = new String[19]; 
    private boolean added = false;
    private Bitmap bmp;
    /* private info will have the following fields:
     * fname mname lname sname title organization nerd1 nerd2 nerd3
     * geek1 geek2 geek3 email facebook googlep twitter phone linkedin addcode
     */
    //internal db stuff
    private DBStuff dbhelper;
    private SQLiteDatabase database;
    private Cursor cur;
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "geek2", "geek3", "nerd1", "nerd2", "nerd3", 
            "email", "facebook", "phone", "twitter", "linkedin", "googlep", "addcode"};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        //eventually this will be the introduction stuff, but for now...
        setContentView(R.layout.newinternetstart);

        editlname = (EditText) findViewById(R.id.newlname);
        editfemail = (EditText) findViewById(R.id.newforgetemail);
        editpass1 = (EditText) findViewById(R.id.newpass);
        editpass2 = (EditText) findViewById(R.id.newpassconfirm);
        newcreatebutton = (Button) findViewById(R.id.newlpass);
        
        //open database and clear out any old tables that might be lingering from a previous install
        dbhelper = new DBStuff(this);
        opendb();
        dropper();
        recreatetable();
        
               
        
        //onclick...
        newcreatebutton.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                lname = editlname.getText().toString();
                femail = editfemail.getText().toString();
                pass1 = editpass1.getText().toString();
                pass2 = editpass2.getText().toString();
                //first check to see if top spaces filled, if not, changed text color
                if(lname.equals("") || femail.equals("")){
                    String[] empties = {lname, femail};
                    TextView[] colorchanges = {(TextView) findViewById(R.id.newtextlname), (TextView) findViewById(R.id.newtextfemail)};
                    for(int i = 0; i<2; i++){
                        if(empties[i].equals("")){
                            colorchanges[i].setTextColor(Color.RED);
                        }
                    }
                }
                //next check to see if passwords match, if not change text color and text for confirmpasstext
                else if(!pass1.equals(pass2)){
                    TextView cpass = (TextView) findViewById(R.id.newtextconfirmpass);
                    cpass.setText("Retyped Password MUST Match");
                    cpass.setTextColor(Color.MAGENTA);
                }
                else if(pass1.equals("") && pass2.equals("")){
                    AlertDialog.Builder openbuilder = new AlertDialog.Builder(NewUserInternet.this);
                    openbuilder.setMessage("You'll be emailed a temporary password");
                    openbuilder.show();
                    new ForgotPassword().execute();
                    
                }
                //if all of the above, then check online and see if lname, forgetemail and 
                //password are already there or not
                else{
         
                    //connect to edb and save password and lname to sharedpref if created successfully
                    new GetUserID().execute();
                                 
                }
            }
        });
          
    }
    
public void finishthis(){
    Intent passer = new Intent(NewUserInternet.this, RegularUse.class);
    startActivity(passer);
    finish();
}

class ForgotPassword extends AsyncTask<String, String, String> {
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(NewUserInternet.this);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    
    protected String doInBackground(String... args) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("lname", lname));
        params.add(new BasicNameValuePair("femail", femail));
        
        // getting JSON Object
        jsonParser.makeHttpRequest(RegularUse.FORGOTPASSLINK,"POST", params);
        //String json = makeHttpRequest(url_create_product,"POST", params);
        // check log cat for response
        return null;
        
    }

    
    protected void onPostExecute(String file_url) {
        // dismiss the dialog once done
        pDialog.dismiss();
    }

}
    
class GetUserID extends AsyncTask<String, String, String> {
        
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewUserInternet.this);
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
            params.add(new BasicNameValuePair("lname", lname));
            params.add(new BasicNameValuePair("forgetemail", femail));
            params.add(new BasicNameValuePair("password", pass1));
            
            // getting JSON Object
            JSONObject json = jsonParser.makeHttpRequest(RegularUse.NEWUSERLINK,"POST", params);

            
            
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
                        uid = userinfo.getInt("_ID");
                        
                        //save card image from url
                        saveBitmapFromURL(uid);
                        
                        isnew = userinfo.getBoolean("isnew");
                        if(userinfo.getString("fname") != "" && userinfo.getString("fname") != "null"){
                            privateinfo[0] = userinfo.getString("fname");
                        }
                        if(userinfo.getString("mname") != "" && userinfo.getString("mname") != "null"){
                            privateinfo[1] = userinfo.getString("mname");
                        }
                        if(userinfo.getString("lname") != "" && userinfo.getString("lname") != "null"){
                            privateinfo[2] = userinfo.getString("lname");
                        }
                        if(userinfo.getString("sname") != "" && userinfo.getString("sname") != "null"){
                            privateinfo[3] = userinfo.getString("sname");
                        }
                        if(userinfo.getString("title") != "" && userinfo.getString("title") != "null"){
                            privateinfo[4] = userinfo.getString("title");
                        }
                        if(userinfo.getString("organization") != "" && userinfo.getString("organization") != "null"){
                            privateinfo[5] = userinfo.getString("organization");
                        }
                        if(userinfo.getString("nerd1") != "" && userinfo.getString("nerd1") != "null"){
                            privateinfo[6] = userinfo.getString("nerd1");
                        }
                        if(userinfo.getString("nerd2") != "" && userinfo.getString("nerd2") != "null"){
                            privateinfo[7] = userinfo.getString("nerd2");
                        }
                        if(userinfo.getString("nerd3") != "" && userinfo.getString("nerd3") != "null"){
                            privateinfo[8] = userinfo.getString("nerd3");
                        }
                        if(userinfo.getString("geek1") != "" && userinfo.getString("geek1") != "null"){
                            privateinfo[9] = userinfo.getString("geek1");
                        }
                        if(userinfo.getString("geek2") != "" && userinfo.getString("geek2") != "null"){
                            privateinfo[10] = userinfo.getString("geek2");
                        }
                        if(userinfo.getString("geek3") != "" && userinfo.getString("geek3") != "null"){
                            privateinfo[11] = userinfo.getString("geek3");
                        }
                        if(userinfo.getString("email") != "" && userinfo.getString("email") != "null"){
                            privateinfo[12] = userinfo.getString("email");
                        }
                        if(userinfo.getString("facebook") != "" && userinfo.getString("facebook") != "null"){
                            privateinfo[13] = userinfo.getString("facebook");
                        }
                        if(userinfo.getString("googlep") != "" && userinfo.getString("googlep") != "null"){
                            privateinfo[14] = userinfo.getString("googlep");
                        }
                        if(userinfo.getString("twitter") != "" && userinfo.getString("twitter") != "null"){
                            privateinfo[15] = userinfo.getString("twitter");
                        }
                        if(userinfo.getString("phone") != "" && userinfo.getString("phone") != "null"){
                            privateinfo[16] = userinfo.getString("phone");
                        }
                        if(userinfo.getString("linkedin") != "" && userinfo.getString("linkedin") != "null"){
                            privateinfo[17] = userinfo.getString("linkedin");
                        }
                        if(userinfo.getString("addcode") != "" && userinfo.getString("addcode") != "null"){
                            privateinfo[18] = Integer.toString(userinfo.getInt("addcode"));
                        }
                        added = true;
                    }
                    else{ //craptastic
                        Intent passer = new Intent(NewUserInternet.this, NewUserNoInternet.class);
                        startActivity(passer);
                        finish();
                    }
                } catch (JSONException e) {
                    Intent passer = new Intent(NewUserInternet.this, NewUserNoInternet.class);
                    startActivity(passer);
                    finish();
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
            if(added){
                //save uid and password to sharedpreferences
                SharedPreferences.Editor se = sharedPref.edit();
                se.putInt("userID", uid);
                se.putString("password", pass1);
                se.commit();
                //then save any info returned to idb
                //cur = database.query(DBStuff.TABLENAME, null, null, null, null, null, null);
                
                long quick = createRow(privateinfo);
                editlname.setText(Integer.toString(uid));
                editfemail.setText(Long.toString(quick));
                //editfemail.setText(Integer.toString(cur.getColumnCount()));
                //go to main card after that
                finishthis();
            }
            pDialog.dismiss();
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
            File imgFile = new File(NewUserInternet.this.getFilesDir(), userID + ".png");
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


public static void saveFromURL(Context c, String link, String filename) {
    /*--- this method downloads an Image from the given URL, 
     *  then decodes and saves the downloaded image
     ---*/
    try {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream itest = c.getResources().openRawResource(R.raw.fbmessage);
        InputStream input = connection.getInputStream();
        byte[] data = new byte[255000];
        FileOutputStream fos = c.openFileOutput(filename, MODE_PRIVATE);
        itest.read(data);
        fos.write(data);
        /*int i=0;  
        while((i=itest.read())!=-1){  
            fos.write((byte)i);  
        } */ 
        //Bitmap myBitmap = BitmapFactory.decodeStream(input);

    } catch (IOException e) {
        e.printStackTrace();
        Log.e("getBmpFromUrl error: ", e.getMessage().toString());
        
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
        values.put("_ID", uid);
        values.put("fname", privateinfo[0]);
        values.put("mname", privateinfo[1]);
        values.put("lname", privateinfo[2]);
        values.put("sname", privateinfo[3]);
        values.put("title", privateinfo[4]);
        values.put("organization", privateinfo[5]);
        values.put("nerd1", privateinfo[6] );
        values.put("nerd2", privateinfo[7] );
        values.put("nerd3", privateinfo[8] );
        values.put("geek1", privateinfo[9] );
        values.put("geek2", privateinfo[10]);
        values.put("geek3", privateinfo[11]);
        values.put("email", privateinfo[12]);
        values.put("facebook", privateinfo[13]);
        values.put("googlep", privateinfo[14]);
        values.put("twitter", privateinfo[15]);
        values.put("phone", privateinfo[16]);
        values.put("linkedin", privateinfo[17]);
        values.put("addcode", privateinfo[18]);
        //values.put(colnames[i], colvals[i]);
    long insertId = database.insert(DBStuff.TABLENAME, null, values);
    return insertId;
}

public void opendb(){
    try{
        database = dbhelper.getWritableDatabase();
        
    }
    catch(SQLException s){
        
        editlname.setText(s.toString());
    }
}

public void recreatetable(){
    database.execSQL("CREATE TABLE " + DBStuff.TABLENAME + " (" +
            "_ID" + " INTEGER PRIMARY KEY, " +   //1
            "fname" + " TEXT, " +                //2
            "mname" + " TEXT, " +                //3
            "lname" + " TEXT, " +                //4
            "sname" + " TEXT, " +                //5
            "title" + " TEXT, " +                //6
            "organization" + " TEXT, " +         //7    
            "geek1" + " TEXT, " +                //8
            "glvl1" + " TEXT, " +                //9
            "geek2" + " TEXT, " +                //10
            "glvl2" + " TEXT, " +                //11
            "geek3" + " TEXT, " +                //12
            "glvl3" + " TEXT, " +                //13
            "nerd1" + " TEXT, " +                //14
            "nlvl1" + " TEXT, " +                //15
            "nerd2" + " TEXT, " +                //16
            "nlvl2" + " TEXT, " +                //17
            "nerd3" + " TEXT, " +                //18
            "nlvl3" + " TEXT, " +                //19
            "email" + " TEXT, " +                //20
            "facebook" + " TEXT, " +             //21
            "phone" + " TEXT, " +                //22    
            "twitter" + " TEXT, " +              //23
            "linkedin" + " TEXT, " +             //24
            "googlep" + " TEXT, " +               //25
            "addcode" + " TEXT, " +               //26
            "deleted" + " INTEGER" +            //27
            ")");
}

public void killdb() {
    cur = database.query(DBStuff.TABLENAME, null, null, null, null, null, null);
    for(int i = 0; i<cur.getCount(); i++){
        cur.moveToLast();
        int ID = cur.getInt(0); 
        database.delete(DBStuff.TABLENAME,  "_ID = " + ID, null);
    }
}

public void dropper(){
    try{
        database.execSQL("DROP TABLE IF EXISTS " + DBStuff.TABLENAME);
    }
    catch(SQLException s){
        editlname.setText(s.toString());
    }
}

public void closedb() {
    database.close();
}

}
