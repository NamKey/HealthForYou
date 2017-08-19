package com.example.nam.healthforyou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NAM on 2017-08-14.
 */

public class ChatroomAdapter extends BaseAdapter {
    LayoutInflater inflater;
    List<Chatroomitem> chatroomitemList = new ArrayList<>();
    @Override
    public int getCount() {
        return chatroomitemList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if(convertView==null)
        {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        Chatroomitem chatroomitem = chatroomitemList.get(position);

        convertView = inflater.inflate(R.layout.chatroomlist, parent, false);//viewholder 추천
                /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
        ImageView iv_chatroomprofile = (ImageView) convertView.findViewById(R.id.iv_chatroomprofile) ;
        TextView tv_chatroomid = (TextView) convertView.findViewById(R.id.tv_chatroomid);
        TextView tv_recentdate = (TextView)convertView.findViewById(R.id.tv_recentdate);
        TextView tv_recentmessage = (TextView)convertView.findViewById(R.id.tv_recentchat);

        iv_chatroomprofile.setImageResource(R.drawable.no_profile);////크기 조정해줘야됨
        tv_chatroomid.setText(chatroomitem.room_name);////방의 이름
        tv_recentdate.setText(chatroomitem.recentdate);
        tv_recentmessage.setText(chatroomitem.recentmessage);

        return convertView;
    }

    public void addRoom(JSONObject chatroom){
        Chatroomitem chatroomitem = new Chatroomitem();
        try {
            chatroomitem.room_id=chatroom.getString("room_id");
            chatroomitem.room_name=chatroom.getString("room_name");
            chatroomitem.recentdate = chatroom.getString("message_date");
            chatroomitem.recentmessage = chatroom.getString("message_content");
            chatroomitem.roomtype = chatroom.getInt("room_type");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatroomitemList.add(chatroomitem);
    }

    ////Overloading - addroom
    public void addRoom(int index,JSONObject chatroom){///첫번째에 업데이트 하고 싶은경우
        Chatroomitem chatroomitem = new Chatroomitem();
        try {
            chatroomitem.room_id=chatroom.getString("room_id");
            chatroomitem.room_name=chatroom.getString("room_name");
            chatroomitem.recentdate = chatroom.getString("message_date");
            chatroomitem.recentmessage = chatroom.getString("message_content");
            chatroomitem.roomtype = chatroom.getInt("room_type");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatroomitemList.add(index,chatroomitem);
    }

    public void updateRoom(int index,JSONObject chatroom)
    {
        Chatroomitem chatroomitem = new Chatroomitem();
        try {
            chatroomitem.room_id=chatroom.getString("room_id");
            chatroomitem.recentdate = chatroom.getString("message_date");
            chatroomitem.recentmessage = chatroom.getString("message_content");
            chatroomitem.roomtype = chatroom.getInt("room_type");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chatroomitemList.remove(index);///인덱스에 따른 값을 바꾸고
        chatroomitemList.add(0,chatroomitem);
    }

    public List<Chatroomitem> getRoom()//채팅방을 모두 갖고옴
    {
        return chatroomitemList;
    }

    public Chatroomitem getRoomitem(int position)
    {
        return chatroomitemList.get(position);
    }
}
