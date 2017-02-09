package com.boffinapes.geekcard;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBStuff extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "gc.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLENAME = "GCContacts";
    
    
    public DBStuff(Context context) {
        // calls the super constructor, requesting the default cursor factory.
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
 
        db.execSQL("CREATE TABLE " + TABLENAME + " (" +
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

    
    /*
     * Need to change this to being more than just dropping of tables
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Kills the table and existing data
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        // Recreates the database with a new version
        onCreate(db);
    }
    

}
