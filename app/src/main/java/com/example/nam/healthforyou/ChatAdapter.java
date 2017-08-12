package com.example.nam.healthforyou;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NAM on 2017-08-08.
 */

public class ChatAdapter extends BaseAdapter {
    List<ChatItem> chatItemList =new ArrayList<>();
    boolean message_left;

    //내가 쓴 카톡과 아닌것을 구분하기 위한 type
    private static final int ITEM_VIEW_TYPE_ME = 0 ;
    private static final int ITEM_VIEW_TYPE_YOU = 1 ;
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
                /////리스트에 정보를 출력
                msgText.setText(msg.item_content);
                msgId.setText(msg.item_sender);
                msgDate.setText(msg.item_date);

                msgText.setTextColor(Color.parseColor("#000000"));

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

    public void addItemME(ChatItem item)
    {
        item.setType(ITEM_VIEW_TYPE_ME);
        chatItemList.add(item);
    }
}
