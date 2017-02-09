package com.boffinapes.geekcard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBDataSource {
    private SQLiteDatabase database;
    private DBStuff dbhelper;
    private String[] colnames = {"fname", "mname", "lname", "sname", "title",
            "organization", "geek1", "glvl1", "geek2", "glvl2", "geek3",
            "glvl3", "nerd1", "nlvl1", "nerd2", "nlvl2", "nerd3", "nlvl3",
            "email", "facebook", "phone", "twitter", "linkedin", "googlep"};
    
    
    public DBDataSource(Context context){
        dbhelper = new DBStuff(context);
    }
    
    public void open() throws SQLException {
        database = dbhelper.getWritableDatabase();
    }
    
    public void close() {
        dbhelper.close();
    }
    
    public long createRow(String[] colvals) {
        ContentValues values = new ContentValues();
        for(int i = 0; i<colvals.length; i++){
            values.put(colnames[i], colvals[i]);
        }
        long insertId = database.insert(DBStuff.TABLENAME, null, values);
        return insertId;
      }

    public void deleteRow(int ID){
        database.delete(DBStuff.TABLENAME,  "_ID = " + ID, null);
    }
     
    
    
}
