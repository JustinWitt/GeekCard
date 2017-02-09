package com.boffinapes.geekcard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boffinapes.geekcard.RegularUse.AddUserCard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactDisplay extends Activity {
    public ImageView[] rletters = new ImageView[26];
    public ImageView[] hletters = new ImageView[26];
    int numofletters;
    int lside = -1;
    int currentselection = -1;
    int first = 0;
    int last = -1;
    int middle = -1;
    float downX = -1;
    float downY = -1;
    float originalY = 100;
    float originalX = 100;
    public DBDataSource dbds;
    public int valsize;
    public int currentrow;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private String thecode;
    
    //database stuff
    private SQLiteDatabase database;
    private DBStuff dbhelper;
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "glvl1", "geek2", "glvl2", "geek3", "glvl3", 
            "nerd1", "nlvl1", "nerd2", "nlvl2", "nerd3", "nlvl3", 
            "email", "facebook", "phone", "twitter", "linkedin", "googlep"};
    Cursor cur;
    GCInfo[] vals;
    private int userID;
    private String password;
    public String firstname;
    public String secondname;
    public String thirdname;
    public TextView fc;
    public TextView sc;
    public TextView tc;
    public ImageView cardimg;
    public LinearLayout contacttouch;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREFS_NAME,0);
        if(sharedPref.contains("userID")){
            userID = sharedPref.getInt("userID", userID);
            password = sharedPref.getString("password", password);
        }
        dbhelper = new DBStuff(this);
        opendb();
                
         
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.contact_display);
        
        LinearLayout scrollarea = (LinearLayout) findViewById(R.id.contactalpha);
        fc = (TextView) findViewById(R.id.firstcontact);
        sc = (TextView) findViewById(R.id.secondcontact);
        tc = (TextView) findViewById(R.id.thirdcontact);
        cardimg = (ImageView) findViewById(R.id.facebookicon);
        contacttouch = (LinearLayout) findViewById(R.id.contactcarddisplaylayout);
        
        
        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        int width = display.getWidth();
        lside = height / 10;
        numofletters = width/lside;
        if(numofletters%2 == 0){
            numofletters--;
        }
        int half = numofletters/2;
        first = 26-half;
        
        setletters(lside);
        
        //initial setup for letters 

        for(int j = 0; j<numofletters; j++){
            if(((first+j)%26) == 0){
                scrollarea.addView(hletters[0]);
                currentselection = 0;
                middle = j;
            }
            else{
                int temp = (first+j) % 26;
                scrollarea.addView(rletters[temp]);
                last = (first+j)%26;
            }
        }
        
        filltablefirst();
        setfirstdisplay();
        refreshscreen();
        
        scrollarea.setOnTouchListener(new OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event) {
                LinearLayout myview = (LinearLayout) v;
                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN){
                    downX = event.getX();
                }
                if(action == MotionEvent.ACTION_MOVE){
                    float moveX = event.getX();
                    if(moveX-downX > lside){
                        downX = moveX;
                        goright(myview);
                        myview.postInvalidate();
                    }
                    else if(downX - moveX > lside){
                        downX = moveX;
                        goleft(myview);
                        myview.postInvalidate();
                    }
                }
                if(action == MotionEvent.ACTION_UP){
                    
                    toalpha(Character.toString((char)(currentselection+65)));
                    refreshscreen();
                    
                }
                return true;
            }
        });
               
        LinearLayout carea = (LinearLayout) findViewById(R.id.contactarea);
        
        carea.setOnTouchListener(new OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN){
                    downY = event.getY();
                    originalY = downY;
                    downX = event.getX();
                    originalX = downX;
                }
                if(action == MotionEvent.ACTION_MOVE){
                    float moveY = event.getY();
                    if(moveY-downY > lside){
                        downY = moveY;
                        alphaprev();
                        refreshscreen();
                    }
                    else if(downY - moveY > lside){
                        downY = moveY;
                        alphanext();
                        refreshscreen();
                    }                    
                }
                if(action == MotionEvent.ACTION_UP){
                    float upY = event.getY();
                    float upX = event.getX();
                    if((Math.abs(upX-originalX) < lside) && (Math.abs(upY-originalY) < lside)){ 
                        if(vals[currentrow].id != userID){                 
                          //Delete card from contacts
                            
                            AlertDialog.Builder openbuilder = new AlertDialog.Builder(ContactDisplay.this);
                            String[] odc = {"Open", "Delete", "Cancel"};
                            openbuilder.setItems(odc, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    if(which == 0){
                                        Intent passer = new Intent(ContactDisplay.this, ContactDetails.class);
                                        passer.putExtra("CID", vals[currentrow].id);
                                        startActivity(passer);
                                    }
                                    else if(which == 1){
                                        deletecard();
                                    }
                                }
                            });
                            openbuilder.show();
                        }
                        else{//case where the card picked is the same as the user's card
                            //Intent passer = new Intent(ContactDisplay.this, RegularUse.class);
                            //startActivity(passer);
                            finish();
                        }
                    }
                }
              
                return true;
            }
            
        });
        
        
       
    }
    
    public void deletecard(){
        //deleteinternal
        int done = deleteRow(vals[currentrow].id);
        //send userID, password and contactID to php which marks deleted from edb
        thecode = Integer.toString(vals[currentrow].id);
        new DelUserCard().execute();
        if(done == 1){
            //currentrow = (currentrow + 1) % valsize;
            filltablefirst();
            refreshscreen();
        }
    }
    
    
    private boolean doesViewContain(View view, int rx, int ry) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();

        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }

    public void refreshscreen(){
        fc.setText(firstname);
        sc.setText(secondname);
        tc.setText(thirdname);
        fc.postInvalidate();
        sc.postInvalidate();
        tc.postInvalidate();
        //changes the cardimg to the current display (val[currentrow].id is id of the person)
        File imgFile = new  File(ContactDisplay.this.getFilesDir(), vals[currentrow].id + ".png");//wherever that is
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            cardimg.setImageBitmap(myBitmap);
        }
        cardimg.postInvalidate();
    }
    
    public void toalpha(String center){//center on name or letter given also will work for a search
        String[] temparg = {center};
        if(vals.length < 20 ){
            cur = database.query(DBStuff.TABLENAME, null, "lname <= ?", temparg, null, null, "lname DESC, sname DESC, fname DESC, mname DESC");
            if(cur.getCount() > 0){
                cur.moveToFirst();
                int foundid = cur.getInt(0);
                for(int i = 0; i<vals.length; i++){
                    if(vals[i].id == foundid){
                        currentrow = i;
                    }
                }
            }    
        }
        else{
            currentrow = 10;
            cur = database.query(DBStuff.TABLENAME, null, "lname >= ?", temparg, null, null, "lname, sname, fname, mname", "10");
            cur.moveToFirst();
            
            if(cur.getCount() < 10){
                for(int i=0; i<cur.getCount(); i++){
                    int tempi = cur.getInt(0);
                    String[] tempvals = new String[24];
                    for(int j = 1; j < 25; j++){
                        tempvals[j-1] = cur.getString(j);
                    }
                    vals[i+10].fillGCI(tempi,tempvals);
                    cur.moveToNext();
                }
                int counter = cur.getCount();
                String limit = String.valueOf(10-counter);
                cur = database.query(DBStuff.TABLENAME,null,null,null,null,null,"lname",limit);
                cur.moveToFirst();
                for(int i=counter; i<10; i++){
                    int tempi = cur.getInt(0);
                    String[] tempvals = new String[24];
                    for(int j = 1; j < 25; j++){
                        tempvals[j-1] = cur.getString(j);
                    }
                    vals[i+10].fillGCI(tempi,tempvals);
                    cur.moveToNext();
                }   
            }
            else{
                for(int i = 0; i<10; i++){
                    int tempi = cur.getInt(0);
                    String[] tempvals = new String[24];
                    for(int j = 1; j < 25; j++){
                        tempvals[j-1] = cur.getString(j);
                    }
                    vals[i+10].fillGCI(tempi,tempvals);
                    cur.moveToNext();
                }
            }
            //now fill top half
            cur = database.query(DBStuff.TABLENAME, null, "lname < ?", temparg, null, null, "lname DESC, sname DESC, fname DESC, mname DESC", "10");
            cur.moveToFirst();
            if(cur.getCount()<10){
                int counter = cur.getCount();
                for(int i = 0; i<counter; i++){
                    int tempi = cur.getInt(0);
                    String[] tempvals = new String[24];
                    for(int j = 1; j < 25; j++){
                        tempvals[j-1] = cur.getString(j);
                    }
                    vals[9-i].fillGCI(tempi,tempvals);
                    cur.moveToNext();
                }
                String temper = String.valueOf(10-counter);
                cur = database.query(DBStuff.TABLENAME, null, null, null, null, null, "lname DESC, sname DESC, fname DESC, mname DESC", temper);            
                cur.moveToFirst();
                for(int i = counter; i<10; i++){
                    int tempi = cur.getInt(0);
                    String[] tempvals = new String[24];
                    for(int j = 1; j < 25; j++){
                        tempvals[j-1] = cur.getString(j);
                    }
                    vals[9-i].fillGCI(tempi,tempvals);
                    cur.moveToNext();
                }
            }
            else{
                for(int i = 0; i<10; i++){
                    int tempi = cur.getInt(0);
                    String[] tempvals = new String[24];
                    for(int j = 1; j < 25; j++){
                        tempvals[j-1] = cur.getString(j);
                    }
                    vals[9-i].fillGCI(tempi,tempvals);
                    cur.moveToNext();
                }
            }
        }
        //now actually put shit in its place in the names
        firstname = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]) + Integer.toString(currentrow);
        currentrow = (currentrow + 1) % vals.length;
        secondname = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]) + Integer.toString(currentrow);
        currentrow = (currentrow + 1) % vals.length;
        thirdname = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]) + Integer.toString(currentrow);
        currentrow = (currentrow + 1) % vals.length;
    }
    
    
    public void alphanext(){//swipe up
        if(valsize < 20){
            firstname = secondname;
            secondname = thirdname;
            thirdname = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]); // + Integer.toString(currentrow);
            currentrow = (currentrow + 1) % valsize;
        }
        else{
            firstname = secondname;
            secondname = thirdname;
            thirdname  = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]);
            currentrow++;
            if(currentrow > 17){
                shiftvalsup();
            }
            
        }   
    }

    public void alphaprev(){//swipe down
        if(valsize < 20){
            currentrow = (currentrow + valsize - 1) % valsize;
            thirdname = secondname;
            secondname = firstname;
            int temp = (currentrow + (valsize*4) - 3) % valsize; 
            firstname = namecreator(vals[temp].cols[0], vals[temp].cols[1], vals[temp].cols[2], vals[temp].cols[3]);
        }
        else{
            if(currentrow < 5){
                shiftvalsdown();
            }
            currentrow--;
            thirdname = secondname;
            secondname = firstname;
            int temp = currentrow - 3;
            firstname = namecreator(vals[temp].cols[0], vals[temp].cols[1], vals[temp].cols[2], vals[temp].cols[3]);
        }   
    }

    public void shiftvalsdown(){
        for(int i = 0; i<10; i++){
            valsreplace(currentrow+9-i,19-i);
        }
        
        String[] temparg = {vals[currentrow].cols[2]};
        currentrow = 10;
        cur = database.query(DBStuff.TABLENAME, null, "lname < ?", temparg, null, null, "lname DESC, sname DESC, fname DESC, mname DESC","10");            
        cur.moveToFirst();
        
        if(cur.getCount()<10){
            int counter = cur.getCount();
            for(int i = 0; i<counter; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[9-i].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }
            String temper = String.valueOf(10-counter);
            cur = database.query(DBStuff.TABLENAME, null, null, null, null, null, "lname DESC, sname DESC, fname DESC, mname DESC", temper);            
            cur.moveToFirst();
            for(int i = counter; i<10; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[9-i].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }
        }
        else{
            for(int i = 0; i<10; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[9-i].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }
        }
    }
    
    public void shiftvalsup(){//recenters on current row
        //top ten becomes bottom ten, bottom ten get last and wrap as necessary
        String[] temparg = {vals[currentrow].cols[2]};
        cur = database.query(DBStuff.TABLENAME,null,"lname > ?",temparg,null,null,"lname","9");
        cur.moveToFirst();
        for(int i = 0; i<11; i++){
            valsreplace(currentrow-10+i,i);
        }
        currentrow = 10;
       
        
        if(cur.getCount() < 9){
            for(int i=0; i<cur.getCount(); i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[i+11].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }
            int counter = cur.getCount();
            String limit = String.valueOf(9-counter);
            cur = database.query(DBStuff.TABLENAME,null,null,null,null,null,"lname",limit);
            cur.moveToFirst();
            for(int i=counter; i<9; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[i+11].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }
        }
        else{
            for(int i=0; i<9; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[i+11].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }
        }
        
    }

  
    public void setfirstdisplay(){
        firstname = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]);
        currentrow = (currentrow + 1) %  valsize;
        secondname = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]);
        currentrow = (currentrow + 1) %  valsize;
        thirdname = namecreator(vals[currentrow].cols[0], vals[currentrow].cols[1], vals[currentrow].cols[2], vals[currentrow].cols[3]);
        currentrow = (currentrow + 1) % valsize;
        fc.setText(firstname);
        sc.setText(secondname);
        tc.setText(thirdname);
    }

    
    
    public void filltablefirst(){
        cur = database.query(DBStuff.TABLENAME,null,null,null,null,null,"lname, sname, fname, mname", "20");
        if(cur.getCount() < 20){
            valsize = cur.getCount();
            cur.moveToFirst();
            vals = new GCInfo[valsize];
            for(int i = 0; i < valsize; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[i] = new GCInfo();
                vals[i].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }
            currentrow = 0;         
        }
        else{
            vals = new GCInfo[20];
            valsize = 20;
            cur = database.query(DBStuff.TABLENAME,null,null,null,null,null,"lname DESC,sname DESC,fname DESC", "10");
            cur.moveToLast();
            
            for(int i = 0; i < 10; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[i] = new GCInfo();
                vals[i].fillGCI(tempi,tempvals);
                cur.moveToPrevious();
            }
            
            cur = database.query(DBStuff.TABLENAME,null,null,null,null,null,"lname,sname,fname", "10");
            cur.moveToFirst();
            for(int i = 0; i < 10; i++){
                int tempi = cur.getInt(0);
                String[] tempvals = new String[24];
                for(int j = 1; j < 25; j++){
                    tempvals[j-1] = cur.getString(j);
                }
                vals[i+10] = new GCInfo();
                vals[i+10].fillGCI(tempi,tempvals);
                cur.moveToNext();
            }   
            currentrow = 10;            
        }    
    }
    
    public void valsreplace(int from, int to){
        int tempID = vals[from].id;
        String[] temparray = new String[24];
        for(int i = 0; i<24; i++){
            temparray[i] = vals[from].cols[i];
        }
        vals[to].fillGCI(tempID, temparray);
    }

    
    /***********************ESSENTIAL DB STUFF***********************/
    
    public int deleteRow(int ID){
        String[] deleteargs = {Integer.toString(ID)};
        int finished = database.delete(DBStuff.TABLENAME,  "_ID = ?", deleteargs);
        return finished;
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
    
    public String namecreator(String first, String mid, String last, String suf){
        String returnthis = "";
        if(first != null && first != ""){
            returnthis = returnthis + " " + first;
        }
        if(mid != null && mid != ""){
            returnthis = returnthis + " " + mid;
        }
        returnthis = returnthis + " " + last;
        if(suf != null && suf != ""){
            returnthis = returnthis + " " + suf;
        }
        return returnthis;
    }
    
    
    /****************************BOTTOM ALPHABET STUFF***************************/
    
    public void goleft(LinearLayout sa){
        
        //first switch middle number
        sa.removeViewAt(middle);
        sa.addView(rletters[currentselection], middle);
        
        //update index pointers
        last = (last+1) % 26;
        first = (first+1) % 26;
        currentselection = (currentselection + 1) % 26;
        
        //now remove first and add last to move down one
        sa.removeViewAt(0);
        sa.addView(rletters[last], numofletters-1);
        
        //then switch the middle one to a highlighted letter
        sa.removeViewAt(middle);
        sa.addView(hletters[currentselection],middle);
        
    }
    
    public void goright(LinearLayout sa){
        //first switch middle number
        sa.removeViewAt(middle);
        sa.addView(rletters[currentselection], middle);
        //update index pointers
        last = (last + 25) % 26;
        first = (first + 25) % 26;
        currentselection = (currentselection + 25) % 26;
        //now remove last and add first to move up one
        sa.removeViewAt(numofletters-1);
        sa.addView(rletters[first],0);
        //then switch the middle one to a highlighted letter
        sa.removeViewAt(middle);
        sa.addView(hletters[currentselection],middle);
        
    }
    
    
    public void setletters(int ls){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ls, ls);
        for(int i = 0; i<26; i++){
            rletters[i] = new ImageView(this);
            rletters[i].setLayoutParams(lp);
            hletters[i] = new ImageView(this);
            hletters[i].setLayoutParams(lp);
        }
        rletters[0].setImageResource(R.drawable.a1);
        rletters[1].setImageResource(R.drawable.b1);
        rletters[2].setImageResource(R.drawable.c1);
        rletters[3].setImageResource(R.drawable.d1);
        rletters[4].setImageResource(R.drawable.e1);
        rletters[5].setImageResource(R.drawable.f1);
        rletters[6].setImageResource(R.drawable.g1);
        rletters[7].setImageResource(R.drawable.h1);
        rletters[8].setImageResource(R.drawable.i1);
        rletters[9].setImageResource(R.drawable.j1);
        rletters[10].setImageResource(R.drawable.k1);
        rletters[11].setImageResource(R.drawable.l1);
        rletters[12].setImageResource(R.drawable.m1);
        rletters[13].setImageResource(R.drawable.n1);
        rletters[14].setImageResource(R.drawable.o1);
        rletters[15].setImageResource(R.drawable.p1);
        rletters[16].setImageResource(R.drawable.q1);
        rletters[17].setImageResource(R.drawable.r1);
        rletters[18].setImageResource(R.drawable.s1);
        rletters[19].setImageResource(R.drawable.t1);
        rletters[20].setImageResource(R.drawable.u1);
        rletters[21].setImageResource(R.drawable.v1);
        rletters[22].setImageResource(R.drawable.w1);
        rletters[23].setImageResource(R.drawable.x1);
        rletters[24].setImageResource(R.drawable.y1);
        rletters[25].setImageResource(R.drawable.z1);
        hletters[0].setImageResource(R.drawable.a2);
        hletters[1].setImageResource(R.drawable.b2);
        hletters[2].setImageResource(R.drawable.c2);
        hletters[3].setImageResource(R.drawable.d2);
        hletters[4].setImageResource(R.drawable.e2);
        hletters[5].setImageResource(R.drawable.f2);
        hletters[6].setImageResource(R.drawable.g2);
        hletters[7].setImageResource(R.drawable.h2);
        hletters[8].setImageResource(R.drawable.i2);
        hletters[9].setImageResource(R.drawable.j2);
        hletters[10].setImageResource(R.drawable.k2);
        hletters[11].setImageResource(R.drawable.l2);
        hletters[12].setImageResource(R.drawable.m2);
        hletters[13].setImageResource(R.drawable.n2);
        hletters[14].setImageResource(R.drawable.o2);
        hletters[15].setImageResource(R.drawable.p2);
        hletters[16].setImageResource(R.drawable.q2);
        hletters[17].setImageResource(R.drawable.r2);
        hletters[18].setImageResource(R.drawable.s2);
        hletters[19].setImageResource(R.drawable.t2);
        hletters[20].setImageResource(R.drawable.u2);
        hletters[21].setImageResource(R.drawable.v2);
        hletters[22].setImageResource(R.drawable.w2);
        hletters[23].setImageResource(R.drawable.x2);
        hletters[24].setImageResource(R.drawable.y2);
        hletters[25].setImageResource(R.drawable.z2);
    }
    
 class DelUserCard extends AsyncTask<String, String, String> {
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ContactDisplay.this);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
 
        
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userID", Integer.toString(userID)));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("deleteID", thecode));
            
            // getting JSON Object
            jsonParser.makeHttpRequest(RegularUse.DELCARDLINK,"POST", params);
            //String json = makeHttpRequest(url_create_product,"POST", params);
            // check log cat for response
            return null;
            
        }
 
        
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }
 
    }
    

}


