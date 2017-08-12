package com.example.nam.healthforyou;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_chat extends Fragment{

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout chat;
    private int[] tabIcons = {
            R.drawable.friend,
            R.drawable.chat
    };

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        chat = (LinearLayout)inflater.inflate(R.layout.frag_chat,container,false);

        //어플 시작시에 Socket을 연다
        ServicesocketThread servicesocketThread = new ServicesocketThread();
        servicesocketThread.start();

        tabLayout = (TabLayout)chat.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("친구목록").setIcon(R.drawable.friend));
        tabLayout.addTab(tabLayout.newTab().setText("채팅목록").setIcon(R.drawable.chat));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager)chat.findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return chat;
    }

    //UI-Thread를 통해서 Socket을 열면 netWorkThreadException 발생 - Thread를 통해 쓰레드 시작
    public class ServicesocketThread extends Thread{
        @Override
        public void run() {
            startServiceMethod();
        }
    }

    //서비스 시작. - 소켓 연결
    public void startServiceMethod(){
        Intent Service = new Intent(getActivity(), ClientSocketService.class);
        getActivity().startService(Service);
    }
}
