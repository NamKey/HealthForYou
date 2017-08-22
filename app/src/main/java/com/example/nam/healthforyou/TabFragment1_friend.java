package com.example.nam.healthforyou;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

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

    final static int ACT_ADDFRIEND =0;
    private FloatingActionMenu fam;
    private FloatingActionButton fab_team_chat, fab_add_user;
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tabfrag_friend = (RelativeLayout)inflater.inflate(R.layout.tab_frag_friend,container,false); //친구목록을 갖고 있는 프레그먼트의 레이아웃
        dBhelper = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB 접근

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
                listViewAdapter.addItemFriend(friendlist.get(i));///친구를 불러오는 부분
            }
            listViewAdapter.notifyDataSetChanged();
        }

        profileList = (ListView)tabfrag_friend.findViewById(R.id.lv_friendlist);//리스트뷰
        profileList.setAdapter(listViewAdapter);///리스트뷰와 아답터 연결
        profileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        fab_team_chat = (FloatingActionButton)tabfrag_friend.findViewById(R.id.fab2);
        fab_add_user = (FloatingActionButton)tabfrag_friend.findViewById(R.id.fab3);
        fam = (FloatingActionMenu)tabfrag_friend.findViewById(R.id.fab_menu);
        fam.setMenuButtonColorNormal(R.color.floating_normal);
        fam.setMenuButtonColorPressed(R.color.floating_pressed);
        //handling each floating action button clicked
        fab_team_chat.setOnClickListener(onButtonClick());
        fab_add_user.setOnClickListener(onButtonClick());

        fam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fam.isOpened()) {
                    fam.close(true);
                }
            }
        });

        fam.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //프로필 다이얼로그 정의
                layout = inflater.inflate(R.layout.custom_profiledialog,null);

                final ProfileItem clickProfile= listViewAdapter.getprofile(position);

                ImageView iv_profile = (ImageView)layout.findViewById(R.id.iv_dialogprofile);
                iv_profile.bringToFront();
                iv_profile.invalidate();
                TextView tv_name = (TextView)layout.findViewById(R.id.tv_dialogname);
                tv_name.setText(clickProfile.name);

                TextView tv_email = (TextView)layout.findViewById(R.id.tv_dialogemail);
                tv_email.setText(clickProfile.email);

                //채팅하기 액티비티로 이동
                Button btn_chattofriend = (Button)layout.findViewById(R.id.btn_chat);
                btn_chattofriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Intent intent = new Intent(getActivity(),Chatroom.class);
                        intent.putExtra("from",0);////개인간의 대화를 나타내는 인텐트 - 다이얼로그를 통해 올 수 있음
                        intent.putExtra("who",clickProfile.email);///인텐트를 통해 내가 누구한테 보내는지 채팅 액티비티로 넘겨줌
                        startActivity(intent);
                    }
                });

                builder = new AlertDialog.Builder(getActivity());
                builder.setView(layout);
                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return tabfrag_friend;
    }

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == fab_add_user) {//////////친구 추가
                    Intent intent =new Intent(getActivity(),Addfriend.class);
                    startActivityForResult(intent,ACT_ADDFRIEND);////친구를 추가
                    getActivity().overridePendingTransition(R.anim.slide_up,R.anim.no_change);

                } else if (view == fab_team_chat) {
                    Intent intent =new Intent(getActivity(),AddGroupChat.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_up,R.anim.no_change);
                }
                fam.close(true);
            }
        };
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1)//RESULT_CODE OK
        {
            if(requestCode==ACT_ADDFRIEND)////최근에 추가한 친구를 상단에 표시
            {
                listViewAdapter.addItemNewFriend(dBhelper.getnewfriend());///최근에 등록된 친구를 리스트뷰에 등록
                listViewAdapter.notifyDataSetChanged();
            }
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("생명주기","onStart");
    }

    @Override
    public void onResume() {
        Log.d("생명주기","onResume");
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
