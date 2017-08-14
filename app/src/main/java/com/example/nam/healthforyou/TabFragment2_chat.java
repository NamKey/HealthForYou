package com.example.nam.healthforyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NAM on 2017-08-04.
 */

public class TabFragment2_chat extends Fragment {
    RelativeLayout chat_list;
    ChatroomAdapter chatroomAdapter;
    ListView chatroomlv;
    DBhelper dBhelper;
    final static int UPDATE_CHATROOM = 1;
    final static int ADD_CHATROOM = 0;

    List<JSONObject> roomlist;
    List<Chatroomitem> lv_roomlist;
    //핸들러로 넘기기 위해 필요

    int roomlistno;
    int lv_roomlistno;
    boolean is_update;

    //브로드 캐스트 리시버 관련 flag
    boolean mIsReceiverRegistered;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        chat_list = (RelativeLayout)inflater.inflate(R.layout.tab_frag_chat,container,false); //친구목록을 갖고 있는 프레그먼트의 레이아웃

        if(!mIsReceiverRegistered){
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter("updateChatroom"));///새로 메세지에 대한 채팅방 확인해보라는 말
            mIsReceiverRegistered = true;
        }

        dBhelper = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB 접근

        chatroomAdapter = new ChatroomAdapter();///리스트뷰 아답터 선언
        chatroomlv = (ListView)chat_list.findViewById(R.id.lv_chatlist);///채팅방 리스트뷰 선언
        chatroomlv.setAdapter(chatroomAdapter);

        //처음 채팅방을 들어갈 때 나한테 온 메세지들을 Groupby를 통해 묶어옴
        List<JSONObject> roomlist = dBhelper.getChatroomList("SELECT * from ChatMessage GROUP by room_id ORDER by message_date DESC;");
        System.out.println(roomlist);

        for(int i=0;i<roomlist.size();i++)
        {
            chatroomAdapter.addRoom(roomlist.get(i));
        }

        chatroomlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ///개인방을 눌렀을 때와 그룹방을 눌렀을 때 채팅방에서 보내는 방법이 다름
                System.out.println(chatroomAdapter.getRoomitem(position).roomtype+" : roomtype");
                if(chatroomAdapter.getRoomitem(position).roomtype!=0)//roomtype이 1인거임 그룹채팅방
                {
                    Intent intent = new Intent(getActivity(),Chatroom.class);
                    intent.putExtra("from",2);///그룹간의 대화방을 나타내는 인텐트 + 이미 채팅방은 생성되어 있고 방한테 메세지를 바로 보낼 수 있음
                    intent.putExtra("room_id",chatroomAdapter.getRoomitem(position).room_id);///방번호를 채팅방에 알려줌
                    startActivity(intent);
                }else{///개인 채팅방임
                    Intent intent = new Intent(getActivity(),Chatroom.class);
                    intent.putExtra("from",0);////- 개인 채팅방에서는 room_id를 기준으로 메세지를 보냄
                    intent.putExtra("who",chatroomAdapter.getRoomitem(position).room_id);///인텐트를 통해 내가 누구한테 보내는지 채팅 액티비티로 넘겨줌
                    startActivity(intent);
                }
            }
        });

        return chat_list;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Thread thread = new Thread(){
                @Override
                public void run() {
                    roomlist = dBhelper.getChatroomList("SELECT * from ChatMessage WHERE is_looked=0 GROUP by room_id ORDER by message_date DESC limit 1;");///보지 않은 메세지가 있는 채팅방을 업데이트?!
                    System.out.println(roomlist);
                    lv_roomlist = chatroomAdapter.getRoom();///리스뷰에 있는 모든 방들을 갖고옴
                    for(int i=0;i<roomlist.size();i++)///업데이트 할 목록 -> 새로 메세지를 받거나 하면 roomlist가 업그레이드 됨
                    {
                        for(int j=0;j<lv_roomlist.size();j++)//listview에 등록된 방을 조사
                        {
                            try {
                                if(lv_roomlist.get(j).room_id.equals(roomlist.get(i).getString("room_id")))///기존에 방이름이 같은 것이 있다면
                                {
                                    lv_roomlistno=j;
                                    roomlistno=i;
                                    is_update = true;
                                    break;//같은 방을 찾으면 더이상 찾을 필요가 없음
                                }else{///방이름 중에 같은게 없으면 추가해줌
                                    lv_roomlistno=j;
                                    roomlistno=i;
                                    is_update = false;
                                    ///찾지 못하면 계속 찾아야됨
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(is_update)
                    {
                        handler.sendEmptyMessage(UPDATE_CHATROOM);
                    }else{
                        handler.sendEmptyMessage(ADD_CHATROOM);
                    }
                }
            };

            thread.start();
        }
    };
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case ADD_CHATROOM:
                    System.out.println("ADDDDD");
                    if(roomlist.size()!=0)
                    {
                        chatroomAdapter.addRoom(0,roomlist.get(roomlistno));///같은 방이 없으면 새로 추가해줌 - 최근 값으로 추가해줌
                    }else{
                        chatroomAdapter.addRoom(roomlist.get(roomlistno));///같은 방이 없으면 새로 추가해줌 - 최근 값으로 추가해줌
                    }
                    chatroomAdapter.notifyDataSetChanged();
                    break;

                case UPDATE_CHATROOM:
                    System.out.println("UPDATE");
                    chatroomAdapter.updateRoom(lv_roomlistno,roomlist.get(roomlistno));
                    chatroomAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if(mIsReceiverRegistered){//flag를 통해 제어
            getActivity().unregisterReceiver(broadcastReceiver);///브로드캐스트 리시버 해제
            mIsReceiverRegistered = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
