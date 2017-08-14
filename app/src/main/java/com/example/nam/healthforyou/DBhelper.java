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
        db.execSQL("CREATE TABLE ChatMessage(message_no INTEGER PRIMARY KEY AUTOINCREMENT,room_type INTEGER,room_id TEXT,message_sender TEXT,message_content TEXT,message_date TEXT,is_looked INTEGER);");//채팅방에 따른 메세지 정보
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

    ////메세지를 등록
    public void messageinsert(String line) {
        SQLiteDatabase db = getWritableDatabase();
        System.out.println("DB insert : "+line);
        /////전송된 데이터를 구분자를 통해 분리함
        String roomid = line.split(":",4)[0];//////그룹간의 대화인지
        String who = line.split(":",4)[1];
        String message = line.split(":",4)[2];
        String date = line.split(":",4)[3];

        //분리한 데이터를 SQlite에 저장 - 다른 사람이 보낸 메세지 타입

        ContentValues values = new ContentValues();
        /////메세지에 들어있는 정보를 분류해야함
        if(roomid.equals("ptop"))///개인간의 대화를 나타냄
        {
            values.put("room_id",who);////개인간의 대화는 방의 id가 상대방으로 설정
            values.put("message_sender",who);//보낸 사람이 누구인지
            values.put("message_content",message);///메세지의 내용
            values.put("message_date",date);///메세지를 보낸 시간
            values.put("is_looked",0);///보였는지 판단 보였으면 1,안보였으면 0
            values.put("room_type",0);///개인간의 대화인지 그룹간의 대화인지 판단 0 - 개인, 1 - 그룹

        }else{///그룹채팅을 의미 방번호가 오게됨

            values.put("room_id",roomid);////그룹간의 대화는 방의 id가 방고유번호 - 서버 RoomManager가 부여
            values.put("message_sender",who);////보낸 사람이 누구인지
            values.put("message_content",message);
            values.put("message_date",date);
            values.put("is_looked",0);///보였는지 판단 보였으면 1,안보였으면 0
            values.put("room_type",1);///개인간의 대화인지 그룹간의 대화인지 판단 0 - 개인, 1 - 그룹
        }

        db.insert("ChatMessage",null,values);
        db.close();
    }

    public JSONObject updatemessage(String _query){
        SQLiteDatabase db = getReadableDatabase();

        JSONObject message =new JSONObject();
        Cursor cursor = db.rawQuery(_query, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    message.put("room_type",(cursor.getString(1)));
                    message.put("room_id",cursor.getString(2));
                    message.put("message_sender",cursor.getString(3));
                    message.put("message_content",(cursor.getString(4)));
                    message.put("message_date",cursor.getString(5));
                    message.put("is_looked",cursor.getInt(6));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return message;
    }

    public List<JSONObject> getChatroomList(String _query)//메세지를 기준으로 방을 나누고 DB에서 긁어오는 메소드
    {
        SQLiteDatabase db = getReadableDatabase();
        List<JSONObject> chatroomList = new ArrayList<>();

        Cursor cursor = db.rawQuery(_query, null);

        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                JSONObject chatList = new JSONObject();
                try {
                    chatList.put("room_type",(cursor.getString(1)));
                    chatList.put("room_id",(cursor.getString(2)));
                    chatList.put("message_sender",(cursor.getString(3)));
                    chatList.put("message_content",(cursor.getString(4)));
                    chatList.put("message_date",(cursor.getString(5)));
                    chatList.put("is_looked",(cursor.getString(6)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Adding contact to list
                chatroomList.add(chatList);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        // 모든 healdata를 갖고옴

        return chatroomList;
    }

}
