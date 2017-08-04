package com.example.nam.healthforyou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
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
        db.execSQL("CREATE TABLE User_health(health_no INTEGER PRIMARY KEY AUTOINCREMENT,user_bpm INTEGER,user_res INTEGER,data_signdate TEXT,is_synced INTEGER,graph_image TEXT);");//건강데이터에 관한 로컬 DB table
        db.execSQL("CREATE TABLE User_friend(friend_no INTEGER PRIMARY KEY AUTOINCREMENT,user_friend TEXT,friendname TEXT);");//친구목록에 대한 로컬 DB table
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
            values.put("data_signdate",healthInfo.getString("data_signdate"));
            values.put("is_synced",healthInfo.getInt("is_synced"));
            values.put("graph_image",healthInfo.getString("graph_image"));
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
                        + cursor.getString(3)
                        +":"
                        + cursor.getInt(4)
                        + "\n";
            }
            cursor.close();
        }else{
          str="false";//저장된 자료가 없음
        }

        return str;
    }

    public int PrintCountData() {
        SQLiteDatabase db = getReadableDatabase();
        String str="";

        Cursor cursor = db.rawQuery("SELECT health_no FROM User_health;", null);

            while(cursor.moveToNext()) {
                str += cursor.getInt(0)
                        + "\n";
            }
            cursor.close();


        return cursor.getCount();
    }


    public List<JSONObject> PrintAvgData(String _query) {
        SQLiteDatabase db = getReadableDatabase();
        List<JSONObject> healthInfos = new ArrayList<>();

        Cursor cursor = db.rawQuery(_query, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject healthInfo =new JSONObject();
                try {
                    healthInfo.put("data_signdate",cursor.getString(0));
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                healthInfos.add(healthInfo);
            } while (cursor.moveToNext());
        }
        return healthInfos;
    }

    public JSONObject PrintMyAvgData(String _query) {/////나의 심박수 평균과
        SQLiteDatabase db = getReadableDatabase();
        JSONObject healthInfo =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        if (cursor.moveToFirst()) {
            do {

                try {
                    healthInfo.put("user_bpm",(cursor.getInt(0)));
                    healthInfo.put("user_res",(cursor.getInt(1)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }
        return healthInfo;
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
                    healthInfo.put("data_signdate",cursor.getString(3));
                    healthInfo.put("is_synced",cursor.getInt(4));
                    healthInfo.put("graph_image",cursor.getString(5));
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
        String selectQuery = "SELECT * FROM User_health WHERE is_synced=0 ORDER BY data_signdate desc";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject healthInfo = new JSONObject();
                try {
                    healthInfo.put("user_bpm",(cursor.getInt(1)));
                    healthInfo.put("user_res",(cursor.getInt(2)));
                    healthInfo.put("data_signdate",cursor.getString(3));
                    healthInfo.put("is_synced",cursor.getInt(4));
                    healthInfo.put("graph_image",cursor.getString(5));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Adding contact to list
                healthInfos.add(healthInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // 모든 healdata를 갖고옴
        return healthInfos;
    }

    public void friendinsert(JSONObject friendinfo) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        try {
            values.put("user_friend",friendinfo.getString("user_friend"));
            values.put("friendname",friendinfo.getString("user_name"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.insert("User_friend",null,values);
        db.close();
    }

    public List<JSONObject> getAllfriend()
    {
        List<JSONObject> friendlist = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM User_friend ORDER BY friendname ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject friend = new JSONObject();
                try {
                    friend.put("user_friend",(cursor.getString(1)));
                    friend.put("user_name",(cursor.getString(2)));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Adding contact to list
                friendlist.add(friend);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // 모든 healdata를 갖고옴
        return friendlist;
    }

    public int PrintCountfriend() {
        SQLiteDatabase db = getReadableDatabase();
        String str="";

        Cursor cursor = db.rawQuery("SELECT friend_no FROM User_friend;", null);

        while(cursor.moveToNext()) {
            str += cursor.getInt(0)
                    + "\n";
        }
        cursor.close();


        return cursor.getCount();
    }
}
