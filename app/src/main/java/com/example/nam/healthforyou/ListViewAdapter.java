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

/**
 * Created by NAM on 2017-08-04.
 */

public class ListViewAdapter extends BaseAdapter {

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<ProfileItem> profileItems = new ArrayList<>();

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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem, parent, false);
        }

        /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
        ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_profile) ;
        TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name) ;

        ProfileItem profileItem = profileItems.get(position);

        iv_img.setImageResource(R.drawable.no_profile);////크기 조정해줘야됨
        tv_name.setText(profileItem.name);

        /* (위젯에 대한 이벤트리스너를 지정하고 싶다면 여기에 작성하면된다..)  */

        return convertView;
    }

    public void addItem(JSONObject friendprofile)
    {
        ProfileItem profileItem = new ProfileItem();
        try {
            profileItem.name=friendprofile.getString("user_name");//이름을 담고
            profileItem.email=friendprofile.getString("user_friend");//이메일을 담고
            ////프로필 사진을 담아야됨
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
