package com.example.nam.healthforyou;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by NAM on 2017-08-04.
 */

public class TabFragment2_chat extends Fragment {
    RelativeLayout chat_list;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        chat_list = (RelativeLayout)inflater.inflate(R.layout.tab_frag_chat,container,false); //친구목록을 갖고 있는 프레그먼트의 레이아웃



        return chat_list;
    }
}
