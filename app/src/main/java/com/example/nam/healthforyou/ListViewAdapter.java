package com.example.nam.healthforyou;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by NAM on 2017-08-04.
 */

public class ListViewAdapter extends BaseAdapter {

    public class ViewHolder{
        public int number;
        ImageView iv_img;
        TextView tv_name;
    }
    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<ProfileItem> profileItems = new ArrayList<>();
    private static final int FRIENDLIST_TYPE=0;
    private static final int GROUPCHATLIST_TYPE=1;
    LayoutInflater inflater;
    Context mContext;
    ByteArrayOutputStream stream;
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
        View v = convertView;
        final ViewHolder viewHolder;
        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }else{

        }

        ProfileItem profileItem = profileItems.get(position);

        switch(profileItem.getType())
        {
            case FRIENDLIST_TYPE:
            {
                convertView = inflater.inflate(R.layout.listitem, parent, false);
                /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
                ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_profile) ;
                TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                Bitmap bitmap = new InternalImageManger(context).//내부저장공간에서 불러옴
                        setFileName(profileItem.profileName).///파일 이름
                        setDirectoryName("PFImage").
                        load();
                if(bitmap!=null)
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    Glide.with(context)
                            .load(stream.toByteArray())
                            .override(64,64)
                            .centerCrop()
                            .error(R.drawable.no_profile)
                            .into(iv_img);
                }else{
                    Glide.with(context)
                            .load(R.drawable.no_profile)
                            .asBitmap()
                            .override(128,128)
                            .into(iv_img);
                }

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

                Bitmap bitmap = new InternalImageManger(context).//내부저장공간에서 불러옴
                        setFileName(profileItem.profileName).///파일 이름
                        setDirectoryName("PFImage").
                        load();
                if(bitmap!=null)
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    Glide.with(context)
                            .load(stream.toByteArray())
                            .override(64,64)
                            .centerCrop()
                            .error(R.drawable.no_profile)
                            .into(iv_img);
                }else{
                    Glide.with(context)
                            .load(R.drawable.no_profile)
                            .asBitmap()
                            .override(128,128)
                            .into(iv_img);
                }

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
            profileItem.profileName=friendprofile.getString("user_profile");//프로필 이미지를 담는다
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
            profileItem.profileName=friendprofile.getString("user_profile");//프로필 이미지를 담는다
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