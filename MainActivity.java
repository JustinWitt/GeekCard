package com.boffinapes.geekcard;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private boolean newuser = true;
    private boolean internet = false;
    private int userID;
    private String password;
    public static final String PREFS_NAME = "MyPrefsFile";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        Intent passer = new Intent(MainActivity.this, TestActivity.class);
        startActivity(passer);
        finish();
        */
        
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME,0);
        
        if(sharedPref.contains("userID")){
            userID = sharedPref.getInt("userID", userID);
            password = sharedPref.getString("password", password);
            newuser = false;
        }
        
        //newuser = false;
        internet = isOnline();
        
        if(!newuser){
            //pass to regularuse
            Intent passer = new Intent(MainActivity.this, RegularUse.class);
            startActivity(passer);
            finish();
        }
        else if(newuser && internet){
            //pass to NewUserInternet
            Intent passer = new Intent(MainActivity.this, NewUserInternet.class);
            startActivity(passer);
            finish();
        }
        else{
            //pass to NewUserNoInternet
            Intent passer = new Intent(MainActivity.this, NewUserNoInternet.class);
            startActivity(passer);
            finish();
        }
        
    }
    
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
