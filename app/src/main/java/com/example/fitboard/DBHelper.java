package com.example.fitboard;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.sql.PreparedStatement;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FIT_DB.db";
    public static final String TABLE_CONTACTS = "auth";

    public static final String KEY_ID = "_id";
    public static final String KEY_PASS = "pass";
    public static final String KEY_NAME = "name";
    public static final String KEY_ROOT = "root";

    public static final String TABLE_EVENT = "event";
    public static final String KEY_PLACE = "place";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_DESC = "description";
    public static final String KEY_AUTH_EVENT ="auth_event";

    public static final String TABLE_ENTRY = "entry";
    public static final String KEY_EVENT_ENTRY = "_id_event";
    public static final String KEY_USER_ENTRY = "_id_user";

    public DBHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_CONTACTS + "(" + KEY_ID
                + " integer primary key," + KEY_NAME + " text," + KEY_PASS + " text," + KEY_ROOT + " integer" +")");

        db.execSQL("create table " + TABLE_EVENT + "(" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + KEY_NAME + " text," + KEY_PLACE + " text,"
                + KEY_DATE + " text," + KEY_TIME + " text," + KEY_DESC + " text," + KEY_AUTH_EVENT + " text" + ")");

        db.execSQL("create table " + TABLE_ENTRY + "(" + KEY_ID
                + " integer primary key AUTOINCREMENT NOT NULL," + KEY_EVENT_ENTRY + " integer," + KEY_USER_ENTRY + " integet"  +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(" drop table if exists " + TABLE_CONTACTS);
        db.execSQL(" drop table if exists " + TABLE_EVENT);
        db.execSQL("drop table if exists " + TABLE_ENTRY);
        onCreate(db);
    }


    public Cursor viewData(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from auth", null);
        return cursor;
    }

    public Cursor select(SQLiteDatabase db, String id) {
        return db.query(TABLE_CONTACTS,
                new String[] {"_id", "name", "pass", "root"},
                "_id = ?",
                new String[] {id},
                null,null,null);
    }
    public int delete(SQLiteDatabase db, String id) {
        return db.delete(TABLE_CONTACTS, "_id = ?", new String[] {id});
    }

    public int update(SQLiteDatabase db, String id, String _name, String _pass, int _root) {
        ContentValues cv = new ContentValues();
        cv.put("name", _name);
        cv.put("pass", _pass);
        cv.put("root", _root);

        return db.update(TABLE_CONTACTS, cv, "_id = ?", new String[] { id });
    }

    public int delete_event(SQLiteDatabase db, String date){
        return db.delete(TABLE_EVENT, " date < ? ", new String[]{date});
    }
}
