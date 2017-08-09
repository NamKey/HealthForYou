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
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.chatlayout, parent, false);
        }

        // Array List에 들어 있는 채팅 문자열을 읽어
        ChatItem msg = chatItemList.get(position);

        // Inflater를 이용해서 생성한 View에, ChatMessage를 삽입한다.
        TextView msgId = (TextView)row.findViewById(R.id.tv_sender);
        TextView msgText = (TextView)row.findViewById(R.id.tv_content);

        msgText.setText(msg.item_content);
        msgId.setText(msg.item_sender);

        msgText.setTextColor(Color.parseColor("#000000"));

          ////보낸 사람이 있으면 상대방 없으면 나
        if(msg.item_sender.equals(""))
        {
            message_left = true;
        }else{
            message_left = false;
        }

//        // 9 - 패치 이미지로 채팅 버블을 출력
        msgText.setBackground(context.getResources().getDrawable( (message_left ?  R.drawable.rounded_yellow : R.drawable.rounded_blue )));

//        // 메세지를 번갈아 가면서 좌측,우측으로 출력

        LinearLayout chatMessageContainer = (LinearLayout)row.findViewById(R.id.chatMessageLayout);

        int align;
        if(message_left) {
            align = Gravity.END;
        }else{
            align = Gravity.START;
        }
        chatMessageContainer.setGravity(align);
        return row;
    }

    public void addItem(ChatItem item)
    {
        chatItemList.add(item);
    }
}
