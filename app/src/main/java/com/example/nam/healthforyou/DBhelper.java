package com.example.nam.healthforyou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NAM on 2017-07-22.
 */

public class DBhelper extends SQLiteOpenHelper {
    public Context mContext;
    public DBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE User_health(health_no INTEGER PRIMARY KEY AUTOINCREMENT,user_bpm INTEGER,user_res INTEGER,user_pre INTEGER,user_oxi INTEGER,data_signdate TEXT);");
        Toast.makeText(mContext,"Table 생성완료", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void infoinsert(JSONObject healthInfo) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        try {
            values.put("user_bpm",healthInfo.getInt("bpm"));
            values.put("user_res",healthInfo.getInt("res"));
            values.put("user_pre",healthInfo.getInt("pre"));
            values.put("user_oxi",healthInfo.getInt("oxi"));
            values.put("data_signdate",healthInfo.getString("data_signdate"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.insert("User_health",null,values);
        db.close();
    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public String PrintData(String _query) {
        SQLiteDatabase db = getReadableDatabase();
        String str = "";

        Cursor cursor = db.rawQuery(_query, null);
        if(cursor.getCount()!=0)
        {
            while(cursor.moveToNext()) {
                str += cursor.getInt(0)
                        +":"
                        + cursor.getInt(1)
                        +":"
                        + cursor.getInt(2)
                        +":"
                        + cursor.getInt(3)
                        +":"
                        + cursor.getInt(4)
                        +":"
                        + cursor.getString(5)

                        + "\n";
            }
        }else{
          str="false";//저장된 자료가 없음
        }

        return str;
    }

    public JSONObject PrintHealthData(String _query) {
        SQLiteDatabase db = getReadableDatabase();
        JSONObject healthInfo =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                    healthInfo.put("user_pre",(cursor.getInt(3)));
                    healthInfo.put("user_oxi",(cursor.getInt(4)));
                    healthInfo.put("data_signdate",cursor.getString(5));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        return healthInfo;
    }

    public List<JSONObject> getAllinfo() {
        List<JSONObject> healthInfos = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + "User_health ORDER BY data_signdate desc";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject healthInfo = new JSONObject();
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                    healthInfo.put("user_pre",(cursor.getInt(3)));
                    healthInfo.put("user_oxi",(cursor.getInt(4)));
                    healthInfo.put("data_signdate",cursor.getString(5));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Adding contact to list
                healthInfos.add(healthInfo);
            } while (cursor.moveToNext());
        }

        // return contact list
        return healthInfos;
    }
}
