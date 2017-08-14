package com.example.nam.healthforyou;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddGroupChat extends AppCompatActivity {
    DBhelper dBhelper;
    ListViewAdapter listViewAdapter;
    List<JSONObject> friendlist;
    ListView profileList;
    StringBuffer sb;
    Button btn_confirm;
    int friendcount;////내가 체크한 친구의 인원
    int checkcount;////내가 체크한 리스트 체크 - 0개일때 확인 버튼 비활성화, 1개일때부터 활성화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_chat);
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);//액션바 숨기기

        dBhelper = new DBhelper(getApplicationContext(), "healthforyou.db", null, 1);//DB 접근
        showActionBar();//액션바 보여주기
        int count=dBhelper.PrintCountfriend();
        listViewAdapter = new ListViewAdapter();//////아답터 선언
        sb = new StringBuffer();//////체크된 친구들의 이메일을 String 형태로 만들 역할
        if(count!=0)
        {
            friendlist=dBhelper.getAllfriend();
            if(friendlist!=null)
            {
                System.out.println(friendlist+"친구목록");
            }

            for(int i=0;i<friendlist.size();i++)//NULLPointer Exception 주의
            {
                //JSONObject -> ProfileItem
                JSONObject jsonObject = friendlist.get(i);
                ProfileItem profileItem = new ProfileItem();
                try {
                    profileItem.name=jsonObject.getString("user_name");//이름을 담고
                    profileItem.email=jsonObject.getString("user_friend");//이메일을 담고
                    profileItem.setType(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listViewAdapter.addGroupFriend(profileItem);///친구를 불러오는 부분
            }
            listViewAdapter.notifyDataSetChanged();
        }

        profileList = (ListView)findViewById(R.id.addgroupfriendlist);//리스트뷰
        profileList.setAdapter(listViewAdapter);///리스트뷰와 아답터 연결
        profileList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewAdapter.notifyDataSetChanged();
                if(!listViewAdapter.getprofile(position).checked)////이전상태를 판단하는 것이기 때문에 !로 표현해야한다
                {
                    checkcount++;
                }else{
                    checkcount--;
                }
                System.out.println("체크된 갯수"+checkcount);
                if(checkcount!=0)
                {
                    btn_confirm.setEnabled(true);
                }else{
                    btn_confirm.setEnabled(false);
                }
            }
        });
    }

    private void showActionBar() {
        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.ab_custom, null);

        //취소버튼
        Button btn_cancel = (Button)v.findViewById(R.id.btn1);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.out.println("취소");
            }
        });
        ///확인 버튼
        btn_confirm = (Button)v.findViewById(R.id.btn2);
        btn_confirm.setEnabled(false);//초기단계에서는 비활성화
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sb.append("");
                friendcount=0;
                for(int i=0;i<listViewAdapter.getCount();i++)
                {
                    System.out.println(listViewAdapter.getprofile(i).checked+"체크여부");
                    if(listViewAdapter.getprofile(i).checked)
                    {
                        sb.append(":");
                        sb.append(listViewAdapter.getprofile(i).email);
                        friendcount++;////친구인원수 세는 부분
                    }
                }
                sb.append(";");
                System.out.println("확인");
                System.out.println(sb.toString().substring(1));//2부터 시작하는 이유 초기에 :를 붙여주기 때문에 제거해줌
                ///////친구의 갯수를 생각해야됨
                Intent intent = new Intent(AddGroupChat.this,Chatroom.class);
                intent.putExtra("from",1);///그룹간의 대화방을 나타내는 인텐트
                intent.putExtra("groupChat",sb.toString().substring(1));
                startActivity(intent);

                sb.setLength(0);
                finish();///그룹채팅 추가액티비티를 끝냄
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(v);
    }
}
