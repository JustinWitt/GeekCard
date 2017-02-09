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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ContactDetails extends Activity{
    PopupWindow pw;
    PopupWindow pw2;
    PopupWindow newpw;
    private SQLiteDatabase database;
    private DBStuff dbhelper;
    private String[] addinfo = new String[18];
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "glvl1", "geek2", "glvl2", "geek3", "glvl3", 
            "nerd1", "nlvl1", "nerd2", "nlvl2", "nerd3", "nlvl3", 
            "email", "facebook", "phone", "twitter", "linkedin", "googlep", "addcode", "deleted"};
    Cursor cur;
    String nametxt;
    int CID;
    int userID;
    String password;
    String acode;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private boolean added = false;
    private String thecode;
    private int addID;
    private EditText toaddcode;
    public static final String USERINFOLINK = "http://www.boffinapes.com/gcstuff/userinfo.php";
    public static final String USERINFOUPDATE = "http://www.boffinapes.com/gcstuff/updateuser.php";
    public static final String NEWUSERLINK = "http://www.boffinapes.com/gcstuff/newuser.php";
    public static final String ADDCARDLINK = "http://www.boffinapes.com/gcstuff/addcard.php";
    public static final String TAG_SUCCESS = "success";
    public static final String Testlink = "http://www.boffinapes.com/singleinfo.php";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                
        
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREFS_NAME,0);
        
        if(sharedPref.contains("userID")){
            userID = sharedPref.getInt("userID", userID);
            password = sharedPref.getString("password", password);
        }
        Intent thisintent = getIntent();
        Bundle extras = thisintent.getExtras();
        CID = extras.getInt("CID");
        if(CID == 0){
            CID = userID;
        }

        nametxt = " ";
        String titletxt = "";
        String nerdstuff = "";
        String geekstuff = "";
        String nerdlvls = "";
        String geeklvls = "";
        dbhelper = new DBStuff(this);
        opendb();
        String ider = "_ID = " + Integer.toString(CID);
        cur = database.query(DBStuff.TABLENAME, null, ider, null, null, null, null);
        if(cur.getCount() < 1){
            closedb();
            Intent passer = new Intent(ContactDetails.this, NewUserNoInternet.class);
            startActivity(passer);
            finish();
        }
        else{
            cur.moveToFirst();
            
            for(int i = 1; i < 5; i++){
                if(cur.getString(i) != "" && cur.getString(i) != null){
                    nametxt = nametxt + cur.getString(i) + " ";
                }
            }
            if(cur.getString(5) != null && cur.getString(5) != ""){
                titletxt = cur.getString(5);
            }
            if(cur.getString(6) != null && cur.getString(6) != ""){
                titletxt = titletxt + " at " + cur.getString(6);
            }
            if(cur.getString(7) != null && cur.getString(7) != ""){
                nerdstuff = cur.getString(7);
            }
            if(cur.getString(9) != null && cur.getString(9) != ""){
                nerdstuff = nerdstuff + "\n" + cur.getString(9);
            }
            if(cur.getString(11) != null && cur.getString(11) != ""){
                nerdstuff = nerdstuff + "\n" + cur.getString(11);
            }
            if(cur.getString(13) != null && cur.getString(13) != ""){
                geekstuff = cur.getString(13);
            }
            if(cur.getString(15) != null && cur.getString(15) != ""){
                geekstuff = geekstuff + "\n" + cur.getString(15);
            }
            if(cur.getString(17) != null && cur.getString(17) != ""){
                geekstuff = geekstuff + "\n" + cur.getString(17);
            }
            if(cur.getString(8) != null && cur.getString(8) != ""){
                nerdlvls = cur.getString(8);
            }
            if(cur.getString(10) != null && cur.getString(10) != ""){
                nerdlvls = nerdlvls + "\n" + cur.getString(10);
            }
            if(cur.getString(12) != null && cur.getString(12) != ""){
                nerdlvls = nerdlvls + "\n" + cur.getString(12);
            }
            if(cur.getString(14) != null && cur.getString(14) != ""){
                geeklvls = cur.getString(14);
            }
            if(cur.getString(16) != null && cur.getString(16) != ""){
                geeklvls = geeklvls + "\n" + cur.getString(16);
            }
            if(cur.getString(18) != null && cur.getString(18) != ""){
                geeklvls = geeklvls + "\n" + cur.getString(18);
            }
            if(cur.getString(25) != null && cur.getString(25) != ""){
                acode = cur.getString(25);
            }
            
        }
            
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    
        //get squaresize from screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        
        ImageView tophalf = (ImageView) findViewById(R.id.reguseimg);
        File imgFile = new  File(ContactDetails.this.getFilesDir(), CID + ".png");//wherever that is
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            tophalf.setImageBitmap(myBitmap);
        }
        else{
            //ERROR HANDLING
        }
        
                
        
        LinearLayout bottomhalf = (LinearLayout) findViewById(R.id.bottomhalf);
        bottomhalf.setBackgroundColor(Color.BLACK);
        LinearLayout contactarea = (LinearLayout) findViewById(R.id.contactarea);
        contactarea.setBackgroundColor(Color.WHITE);
        
        TextView ngdivide = (TextView) findViewById(R.id.nerdgeekdivide);
        int ngsize = maxsize("Nerdery / Geekery", (int) (height*.05), width);
        ngdivide.setTextSize(TypedValue.COMPLEX_UNIT_SP, ngsize);
        
             
        
        TextView nerd1 = (TextView) findViewById(R.id.nerdery1);
        TextView nlvls = (TextView) findViewById(R.id.nlvls);
        TextView geek1 = (TextView) findViewById(R.id.geekery1);
        TextView glvls = (TextView) findViewById(R.id.glvltxt);
        
        nerd1.setText(nerdstuff);
        nlvls.setText(nerdlvls);
        geek1.setText(nerdstuff);
        glvls.setText(nerdlvls);
        
        
        
        TextView contacts = (TextView) findViewById(R.id.contacts);
        contacts.isClickable();
        contacts.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                addopencontacts();   
            }
        });
        
        
        ImageView emailimg = (ImageView) findViewById(R.id.emailicon);
        emailimg.isClickable();
        emailimg.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Intent emailintent = new Intent(Intent.ACTION_SEND);
                emailintent.setType("plain/text");
                emailintent.putExtra(Intent.EXTRA_EMAIL, new String[] { "some@email.address" });
                emailintent.putExtra(Intent.EXTRA_SUBJECT, "subject");
                emailintent.putExtra(Intent.EXTRA_TEXT, "mail body");
                startActivity(Intent.createChooser(emailintent, ""));     
            }
        });
        
        ImageView phoneimg = (ImageView) findViewById(R.id.phoneicon);
        phoneimg.isClickable();
        phoneimg.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                Intent phoneintent = new Intent(Intent.ACTION_DIAL);
                phoneintent.setData(Uri.parse("tel:9193026164"));
                startActivity(phoneintent); 
            }
        });
        
        ImageView twitimg = (ImageView) findViewById(R.id.twittericon);
        twitimg.isClickable();
        twitimg.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                pDialog = new ProgressDialog(ContactDetails.this);
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
                try{
                    Intent twitintent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=Without_Witt"));
                    startActivity(twitintent);
                    pDialog.cancel();
                   }catch (Exception e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/Without_Witt"))); 
                   }
            }
        });    
        
        ImageView faceimg = (ImageView) findViewById(R.id.facebookicon);
        faceimg.isClickable();
        faceimg.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                try{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/2725260"));
                    startActivity(intent);
                }catch(Exception e){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/WithoutWitt")));
                }
            }
        });
        
        ImageView linkedimg = (ImageView) findViewById(R.id.linkedinicon);
        linkedimg.isClickable();
        linkedimg.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/in/BoffinApe")));
            }
        });
        
        ImageView googimg = (ImageView) findViewById(R.id.googleplusicon);
        googimg.isClickable();
        googimg.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/104661162418070736170/posts")));
            }
        });
        
    }
    
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
    
    public int maxsize(String txt, int maxheight, int maxwidth){
        /*
        int txtheight = 0;
        int txtwidth = 0;
        int namesize = 1;
        
        while(txtheight < maxheight && txtwidth < maxwidth){
            Paint paint = new Paint();
            Rect bounds = new Rect();
            paint.setTypeface(Typeface.DEFAULT);// your preference here
            paint.setTextSize(namesize);// have this the same as your text size
        
            paint.getTextBounds(txt, 0, txt.length(), bounds);
            
            txtheight =  bounds.height() - bounds.top;
            txtwidth =  bounds.width() - bounds.left;
            namesize++;
        }
        //nametxt = Integer.toString(namesize) + ", " + Integer.toString(txtheight);
        return namesize-1;
        */
        return 20;
    }
    
    public void newcreaterpop(){
        LayoutInflater inflaterpop = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View popper = inflaterpop.inflate(R.layout.newinternetstart, (ViewGroup) findViewById(R.id.newpop));
        newpw = new PopupWindow(popper, 500, 400, true);
        LinearLayout topper = (LinearLayout) popper.findViewById(R.id.newpop);
        newpw.showAtLocation(topper, Gravity.CENTER, 0, 0);

        Button nlp = (Button) popper.findViewById(R.id.newlpass);
        nlp.isClickable();
        
    }
    

    
    public void addopencontacts(){
        final LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View popup = inflater.inflate(R.layout.addeditcontact, (ViewGroup) findViewById(R.id.addopencontacts));
        pw = new PopupWindow(popup, 500, 250, true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        LinearLayout aocontacts = (LinearLayout) popup.findViewById(R.id.addopencontacts);
        pw.showAtLocation(aocontacts, Gravity.CENTER, 0, 0);
        toaddcode = (EditText) popup.findViewById(R.id.addcnumedit);
        toaddcode.setText(acode);
        Button addcbut;
        addcbut  = (Button) popup.findViewById(R.id.addcontactbut);
        addcbut.isClickable();
        addcbut.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                thecode = toaddcode.getText().toString();
                if(isOnline()){
                    new AddUserCard().execute();
                }
                pw.dismiss();
            }
        });
        Button opencbut;
        opencbut = (Button) popup.findViewById(R.id.opencontactbut);
        opencbut.isClickable();
        opencbut.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                pw.dismiss();
                Intent passer = new Intent(ContactDetails.this, ContactDisplay.class);
                startActivity(passer);
            }
        });
        Button showcode;
        showcode = (Button) popup.findViewById(R.id.showcode);
        showcode.isClickable();
        showcode.setOnClickListener(new OnClickListener(){
           public void onClick(View v){
               pw.dismiss();
               View codeshower = inflater.inflate(R.layout.showthecode, (ViewGroup) findViewById(R.id.maincodeshow));
               pw2 = new PopupWindow(codeshower,250,250, true);
               pw2.setBackgroundDrawable(new BitmapDrawable());
               LinearLayout codelayout = (LinearLayout) codeshower.findViewById(R.id.maincodeshow);
               pw2.showAtLocation(codelayout, Gravity.CENTER, 0, 0);
               TextView scode = (TextView) codeshower.findViewById(R.id.showingcode);
               if(acode != null){
                   scode.setText(acode);
               }
               Button goaway = (Button) codeshower.findViewById(R.id.codeokay);
               goaway.isClickable();
               goaway.setOnClickListener(new OnClickListener(){
                   public void onClick(View v){
                       pw2.dismiss();
                   }
               });
           }
        });
    }

    
    /****************Add card info to idb*************/
    
    class AddUserCard extends AsyncTask<String, String, String> {
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ContactDetails.this);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("CID", Integer.toString(CID)));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("addcode", thecode));
            
            // getting JSON Object
            JSONObject json = jsonParser.makeHttpRequest(ContactDetails.ADDCARDLINK,"POST", params);
            //String json = makeHttpRequest(url_create_product,"POST", params);
            // check log cat for response
            Log.d("Create Response", json.toString());

            //see if a connection returned anything
            if(json != null){
                
                try {
                    // check for success tag
    
                    int success = json.getInt(ContactDetails.TAG_SUCCESS);
                    if (success == 1) {
                        
                        
                        JSONArray userObj = json.getJSONArray("userinfo"); // JSON Array
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
                        added = true;
                        /**/
                    }
                    else{ //then it could not find card in the database
                        //no card message
                        //pop up? maybe reuse pop-up for show addcode?
                        /*
                        Intent passer = new Intent(RegularUse.this, NewUserNoInternet.class);
                        startActivity(passer);
                        finish();
                        */
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                    /*
                    Intent passer = new Intent(RegularUse.this, NewUserNoInternet.class);
                    startActivity(passer);
                    */
                    
                }
            }
            return null;
            
        }
 
        
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            if(added){            
                //then save any info returned to idb
                createRow(addinfo);
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
    
    public void saveBitmapFromURL(int CID) {
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
          link = link + Integer.toString((CID%10)) + "/";
          link = link + Integer.toString(CID) + ".png";
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            
            InputStream input = connection.getInputStream();
            
            Bitmap bmp = BitmapFactory.decodeStream(input);
            FileOutputStream out = null;
            try {
                File imgFile = new File(ContactDetails.this.getFilesDir(), CID + ".png");
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
