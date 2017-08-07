package com.example.nam.healthforyou;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by NAM on 2017-08-04.
 */

public class TabFragment1_friend extends Fragment {
    RelativeLayout tabfrag_friend;///
    List<JSONObject> friendlist;
    DBhelper dBhelper;
    ListViewAdapter listViewAdapter;
    ListView profileList;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    View layout;
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tabfrag_friend = (RelativeLayout)inflater.inflate(R.layout.tab_frag_friend,container,false); //친구목록을 갖고 있는 프레그먼트의 레이아웃
        dBhelper = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB 접근

        listViewAdapter = new ListViewAdapter();//////아답터 선언
        profileList = (ListView)tabfrag_friend.findViewById(R.id.lv_friendlist);//리스트뷰
        profileList.setAdapter(listViewAdapter);///리스트뷰와 아답터 연결
        profileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        int count=dBhelper.PrintCountfriend();
        System.out.println(count+"친구의 갯수");

        ///친구추가 액티비티로 이동하는 버튼
        Button btn_addfriend = (Button)tabfrag_friend.findViewById(R.id.btn_addfriend);
        btn_addfriend.bringToFront();
        btn_addfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewAdapter.Deleteall();///모든 리스트뷰 아이템 삭제 - 페이징 구현 필요!!!!20170824
                Intent intent =new Intent(getActivity(),Addfriend.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_up,R.anim.no_change);
            }
        });

        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //프로필 다이얼로그 정의
                layout = inflater.inflate(R.layout.custom_profiledialog,null);

                ProfileItem clickProfile= listViewAdapter.getprofile(position);

                ImageView iv_profile = (ImageView)layout.findViewById(R.id.iv_dialogprofile);
                iv_profile.bringToFront();
                iv_profile.invalidate();
                TextView tv_name = (TextView)layout.findViewById(R.id.tv_dialogname);
                tv_name.setText(clickProfile.name);

                TextView tv_email = (TextView)layout.findViewById(R.id.tv_dialogemail);
                tv_email.setText(clickProfile.email);

                //채팅하기 액티비티로 이동


                builder = new AlertDialog.Builder(getActivity());
                builder.setView(layout);
                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return tabfrag_friend;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("생명주기","onStart");
    }

    @Override
    public void onResume() {
        Log.d("생명주기","onResume");
        int count=dBhelper.PrintCountfriend();
        if(count!=0)
        {
            friendlist=dBhelper.getAllfriend();
            if(friendlist!=null)
            {
                System.out.println(friendlist+"친구목록");
            }

            for(int i=0;i<friendlist.size();i++)//NULLPointer Exception 주의
            {
                listViewAdapter.addItem(friendlist.get(i));
            }
            listViewAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("생명주기","onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("생명주기","onStop");
        super.onStop();
    }
}
