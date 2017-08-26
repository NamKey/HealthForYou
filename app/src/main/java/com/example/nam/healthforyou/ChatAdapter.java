package com.example.nam.healthforyou;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by NAM on 2017-08-08.
 */

public class ChatAdapter extends BaseAdapter {
    List<ChatItem> chatItemList =new ArrayList<>();
    boolean message_left;

    //내가 쓴 카톡과 아닌것을 구분하기 위한 type
    private static final int ITEM_VIEW_TYPE_ME = 0 ;
    private static final int ITEM_VIEW_TYPE_YOU = 1 ;

    private static final int ITEM_VIEW_TYPE_HEALTHME = 2;
    private static final int ITEM_VIEW_TYPE_HEALTHYOU= 3;

    LayoutInflater inflater;

    @Override
    public int getCount() {
        return chatItemList.size();
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
        View row = convertView;
        Context context = parent.getContext();
        if (row == null) {
            // inflator를 생성하여, chatting_message.xml을 읽어서 View객체로 생성한다.
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        // Array List에 들어 있는 채팅 문자열을 읽어
        ChatItem msg = chatItemList.get(position);
        switch(msg.getType())
        {
            case ITEM_VIEW_TYPE_ME:
            {
                row = inflater.inflate(R.layout.chatlayout2_me, parent, false);

                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgText = (TextView)row.findViewById(R.id.tv_content);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                /////리스트에 정보를 출력
                msgText.setText(msg.item_content);
                msgDate.setText(msg.item_date);

                msgText.setTextColor(Color.parseColor("#000000"));
                break;
            }

            case ITEM_VIEW_TYPE_YOU:
            {
                row = inflater.inflate(R.layout.chatlayout1_you, parent, false);
                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgId = (TextView)row.findViewById(R.id.tv_sender);
                TextView msgText = (TextView)row.findViewById(R.id.tv_content);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                ImageView profile = (ImageView)row.findViewById(R.id.iv_chatimage);
                /////리스트에 정보를 출력
                msgText.setText(msg.item_content);
                msgId.setText(msg.item_sender);
                msgDate.setText(msg.item_date);
                msgText.setTextColor(Color.parseColor("#000000"));
                if(msg.item_senderId!=null)
                {
                    Bitmap bitmap = new InternalImageManger(context).//내부저장공간에서 불러옴
                            setFileName(msg.item_senderId+"_Image").///파일 이름
                            setDirectoryName("PFImage").
                            load();
                    if(bitmap!=null)
                    {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        Glide.with(context)
                                .load(stream.toByteArray())
                                .asBitmap()
                                .override(50,50)
                                .error(R.drawable.no_profile)
                                .transform(new CropCircleTransformation(context))
                                .into(profile);
                    }else{
                        Glide.with(context)
                                .load(R.drawable.no_profile)
                                .asBitmap()
                                .override(50,50)
                                .into(profile);
                    }

                }else{
                    Glide.with(context)
                            .load(R.drawable.no_profile)
                            .asBitmap()
                            .override(50,50)
                            .into(profile);
                }

                break;
            }

            case ITEM_VIEW_TYPE_HEALTHME:
            {
                row = inflater.inflate(R.layout.chatlayout3_mehealth, parent, false);
                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgbpm = (TextView)row.findViewById(R.id.tv_chatHeart1);
                TextView msgres = (TextView)row.findViewById(R.id.tv_chatRes1);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                TextView chatDatasigndate = (TextView)row.findViewById(R.id.tv_chatdatasigndate);
                /////리스트에 정보를 출력
                msgbpm.setText(msg.user_bpm+"bpm");
                msgres.setText(msg.user_res+"/min");
                msgDate.setText(msg.item_date);
                chatDatasigndate.setText("측정날짜 :"+msg.data_signdate);
                break;
            }

            case ITEM_VIEW_TYPE_HEALTHYOU:
            {
                row = inflater.inflate(R.layout.chatlayout4_youhealth, parent, false);
                // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
                TextView msgId = (TextView)row.findViewById(R.id.tv_sender);
                TextView msgbpm = (TextView)row.findViewById(R.id.tv_chatHeart1);
                TextView msgres = (TextView)row.findViewById(R.id.tv_chatRes1);
                TextView msgDate = (TextView)row.findViewById(R.id.tv_sendtime);
                TextView chatDatasigndate = (TextView)row.findViewById(R.id.tv_chatdatasigndate);
                ImageView profile = (ImageView)row.findViewById(R.id.iv_healthchatimage);
                /////리스트에 정보를 출력
                msgId.setText(msg.item_sender);
                msgbpm.setText(msg.user_bpm+"bpm");
                msgres.setText(msg.user_res+"/min");
                msgDate.setText(msg.item_date);
                chatDatasigndate.setText("측정날짜 :"+msg.data_signdate);

                if(msg.item_senderId!=null)
                {
                    Bitmap bitmap = new InternalImageManger(context).//내부저장공간에서 불러옴
                            setFileName(msg.item_senderId+"_Image").///파일 이름
                            setDirectoryName("PFImage").
                            load();
                    if(bitmap!=null)
                    {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        Glide.with(context)
                                .load(stream.toByteArray())
                                .asBitmap()
                                .override(50,50)
                                .error(R.drawable.no_profile)
                                .transform(new CropCircleTransformation(context))
                                .into(profile);
                    }else{
                        Glide.with(context)
                                .load(R.drawable.no_profile)
                                .asBitmap()
                                .override(50,50)
                                .into(profile);
                    }

                }else{
                    Glide.with(context)
                            .load(R.drawable.no_profile)
                            .asBitmap()
                            .override(50,50)
                            .into(profile);
                }

                break;
            }
        }
        return row;
    }

    public void addItemYou(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_YOU);
        chatItemList.add(item);
    }

    public void addItemYou(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_YOU);
        chatItemList.add(0,item);
    }

    public void addItemME(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_ME);
        chatItemList.add(item);
    }

    public void addItemME(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_ME);
        chatItemList.add(0,item);
    }

    public void addItemHealthME(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHME);
        chatItemList.add(item);
    }

    public void addItemHealthME(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHME);
        chatItemList.add(0,item);
    }

    public void addItemHealthYou(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHYOU);
        chatItemList.add(item);
    }

    public void addItemHealthYou(int index,ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_HEALTHYOU);
        chatItemList.add(0,item);
    }
}
