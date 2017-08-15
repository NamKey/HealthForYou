package com.example.nam.healthforyou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by NAM on 2017-08-04.
 */

public class ListViewAdapter extends BaseAdapter {

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<ProfileItem> profileItems = new ArrayList<>();

    private static final int FRIENDLIST_TYPE=0;
    private static final int GROUPCHATLIST_TYPE=1;
    LayoutInflater inflater;

    @Override
    public int getCount() {
        return profileItems.size();
    }

    @Override
    public Object getItem(int position) {
        return profileItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        ProfileItem profileItem = profileItems.get(position);

        switch(profileItem.getType())
        {
            case FRIENDLIST_TYPE:
            {
                convertView = inflater.inflate(R.layout.listitem, parent, false);
                /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
                ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_profile) ;
                TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name) ;
                iv_img.setImageResource(R.drawable.no_profile);////크기 조정해줘야됨
                tv_name.setText(profileItem.name);
                break;
            }

            case GROUPCHATLIST_TYPE:
            {
                convertView = inflater.inflate(R.layout.listitem2, parent, false);
                /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
                ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_profilegroup);
                TextView tv_name = (TextView) convertView.findViewById(R.id.tv_namegroup);
                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.cb_addgroup);
                checkBox.setFocusable(false);
                checkBox.setClickable(false);

                iv_img.setImageResource(R.drawable.no_profile);////크기 조정해줘야됨
                tv_name.setText(profileItem.name);
                checkBox.setChecked(((ListView)parent).isItemChecked(position));////체크 박스 기억
                if(checkBox.isChecked())
                {
                    profileItem.checked=true;
                }else{
                    profileItem.checked=false;
                }
                break;
            }
        }

        return convertView;
    }

    public void addItemFriend(JSONObject friendprofile)
    {
        ProfileItem profileItem = new ProfileItem();
        try {
            profileItem.name=friendprofile.getString("user_name");//이름을 담고
            profileItem.email=friendprofile.getString("user_friend");//이메일을 담고
            profileItem.setType(FRIENDLIST_TYPE);
            ////프로필 사진을 담아야됨
        } catch (JSONException e) {
            e.printStackTrace();
        }
        profileItems.add(profileItem);//////유저의 프로필 추가
    }

    public void addItemNewFriend(JSONObject friendprofile)
    {
        ProfileItem profileItem = new ProfileItem();
        try {
            profileItem.name=friendprofile.getString("user_name");//이름을 담고
            profileItem.email=friendprofile.getString("user_friend");//이메일을 담고
            profileItem.setType(FRIENDLIST_TYPE);
            ////프로필 사진을 담아야됨
        } catch (JSONException e) {
            e.printStackTrace();
        }
        profileItems.add(0,profileItem);//////유저의 프로필 추가
    }

    public void addGroupFriend(ProfileItem profileItem)
    {
        profileItems.add(profileItem);//////유저의 프로필 추가
    }

    public ProfileItem getprofile(int position)
    {
        return profileItems.get(position);
    }

    public void Deleteall()
    {
        profileItems.clear();
    }
}
