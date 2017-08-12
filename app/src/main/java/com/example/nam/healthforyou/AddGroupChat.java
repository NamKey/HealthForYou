package com.example.nam.healthforyou;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_chat);

        dBhelper = new DBhelper(getApplicationContext(), "healthforyou.db", null, 1);//DB 접근

        int count=dBhelper.PrintCountfriend();
        listViewAdapter = new ListViewAdapter();//////아답터 선언
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
            }
        });

    }
}
