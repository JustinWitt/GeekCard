package com.boffinapes.geekcard;

import java.io.InputStream;
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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class ContactEdit extends Activity{
    private ProgressDialog pDialog;
    private String uid;
    private String password;
    private String addcode;
    InputStream is = null;
    JSONObject jObj = null;
    JSONParser jsonParser = new JSONParser();
    EditText email;
    EditText facebook;
    EditText phone;
    EditText twitter;
    EditText linkedin;
    EditText googlep;
    String[] info = new String[6];
    PopupWindow pw;
    private SQLiteDatabase database;
    private DBStuff dbhelper;
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "glvl1", "geek2", "glvl2", "geek3", "glvl3", 
            "nerd1", "nlvl1", "nerd2", "nlvl2", "nerd3", "nlvl3", 
            "email", "facebook", "phone", "twitter", "linkedin", "googlep"};;
    Cursor cur;


@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.topedit);
    dbhelper = new DBStuff(this);
    
    TextView etxt = (TextView) findViewById(R.id.tetxt1);
    etxt.setText("email");
    email = (EditText) findViewById(R.id.edit1);
    
    TextView ftxt = (TextView) findViewById(R.id.tetxt2);
    ftxt.setText("facebook");
    facebook = (EditText) findViewById(R.id.edit2);
    
    TextView ptxt = (TextView) findViewById(R.id.tetxt3);
    ptxt.setText("phone");
    phone = (EditText) findViewById(R.id.edit3);
    
    TextView ttxt = (TextView) findViewById(R.id.tetxt4);
    ttxt.setText("twitter");
    twitter = (EditText) findViewById(R.id.edit4);
    
    TextView ltxt = (TextView) findViewById(R.id.tetxt5);
    ltxt.setText("linkedin");
    linkedin = (EditText) findViewById(R.id.edit5);
    
    TextView gtxt = (TextView) findViewById(R.id.tetxt6);
    gtxt.setText("google plus");
    googlep = (EditText) findViewById(R.id.edit6);
    
    Button saver = (Button) findViewById(R.id.topeditsave);
    Button clear = (Button) findViewById(R.id.topeditclear);

    opendb();
    String[] cols = {"_ID", "email", "facebook", "phone", "twitter", "linkedin", "googlep"};
    cur = database.query(DBStuff.TABLENAME, cols, "_ID = 1", null, null, null, null);
    cur.moveToFirst();
    
    
    if(cur.getCount() > 0){
        cur.moveToFirst();
        email.setText(cur.getString(1));
        facebook.setText(cur.getString(2));
        phone.setText(cur.getString(3));
        twitter.setText(cur.getString(4));
        linkedin.setText(cur.getString(5));
        googlep.setText(cur.getString(6));
    }

    closedb();
    
    
   clear.isClickable();
   clear.setOnClickListener(new OnClickListener(){
       public void onClick(View v) {
           email.setText("");
           facebook.setText("");
           phone.setText("");
           twitter.setText("");
           linkedin.setText("");
           googlep.setText("");
       }
   });

   

   
   saver.isClickable();
   saver.setOnClickListener(new OnClickListener(){
       public void onClick(View v) {
           info[0] = email.getText().toString();
           info[1] = facebook.getText().toString();
           info[2] = phone.getText().toString();
           info[3] = twitter.getText().toString();
           info[4] = linkedin.getText().toString();
           info[5] = googlep.getText().toString();
           if(info[0].equals("") && info[1].equals("") && 
              info[2].equals("") && info[3].equals("") &&
              info[4].equals("") && info[5].equals("")){
               savepop();
           }
           else{
               opendb();
               replaceVals(1,new String[] {"email", "facebook", "phone", "twitter", "linkedin", "googlep"}, info);
               closedb();
           }
       }
   });
   


    
}

public void savepop(){
    LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
    View popup = inflater.inflate(R.layout.savepop, (ViewGroup) findViewById(R.id.savepop));
    pw = new PopupWindow(popup, 500, 250, true);
    LinearLayout topper = (LinearLayout) findViewById(R.id.topedittop);
    pw.showAtLocation(topper, Gravity.CENTER, 0, 0);
    TextView required = (TextView) popup.findViewById(R.id.lnamerequired);
    required.setText("Enter in at Least One Contact Method");
    Button oker;
    oker  = (Button) popup.findViewById(R.id.savepopok);
    oker.isClickable();
    oker.setOnClickListener(new OnClickListener(){
        public void onClick(View v) {
            pw.dismiss();
        }
    });
   
}

/***********************ESSENTIAL DB STUFF***********************/


public void deleteRow(int ID){
    database.delete(DBStuff.TABLENAME,  "_ID = " + ID, null);
}

public long replaceVals(int id, String[] colids, String[] colvals){
    ContentValues values = new ContentValues();
    String[] temparg = { Integer.toString(id)};
    cur = database.query(DBStuff.TABLENAME, null, "_ID = ?", temparg, null, null, null);
    cur.moveToFirst();
    if(cur.getCount() > 0){
        for(int j = 0; j < colnames.length; j++){
            values.put(colnames[j], cur.getString(cur.getColumnIndex(colnames[j])));
        }
    }    
    for(int i = 0; i<colvals.length; i++){
        values.put(colids[i], colvals[i]);
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

/******************************External DB Stuff*****************************/
class GetUserInfo extends AsyncTask<String, String, String> {
    
    /**
     * Before starting background thread Show Progress Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(ContactEdit.this);
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
        params.add(new BasicNameValuePair("uid", uid));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("addcode", addcode));
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
                   
                    info[0] = userinfo.getString("fname");
                    info[1] = userinfo.getString("mname");
                    info[2] = userinfo.getString("lname");
                    info[3] = userinfo.getString("sname");
                    info[4] = userinfo.getString("title");
                    info[5] = userinfo.getString("organization");
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
        if(info[0] != "null"){
            email.setText(info[0]);
        }
        if(info[1] != "null"){
            facebook.setText(info[1]);
        }
        if(info[2] != "null"){
            phone.setText(info[2]);
        }
        if(info[3] != "null"){
            twitter.setText(info[3]);
        }
        if(info[4] != "null"){
            linkedin.setText(info[4]);
        }
        if(info[5] != "null"){
            googlep.setText(info[5]);
        }
    }

}



}