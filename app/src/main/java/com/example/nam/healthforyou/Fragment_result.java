package com.example.nam.healthforyou;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_result extends Fragment {
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    RelativeLayout mView;
    ViewPager mViewPager;
    ViewPagerAdapter myAdapter;
    DBhelper dBhelper;
    BarChart mybarChart;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (RelativeLayout)inflater.inflate(R.layout.frag_result,container,false);
        mViewPager = (ViewPager)mView.findViewById(R.id.health_viewpager);
        dotsLayout = (LinearLayout)mView.findViewById(R.id.layoutDots);
        myAdapter = new ViewPagerAdapter(getChildFragmentManager());
        dBhelper = new DBhelper(getActivity().getApplicationContext(),"healthforyou.db", null, 1);

        //limit절을 통해 평균을 구해놓은 쿼리를 최신순으로 정렬 후 한개만 limit를 통해 갖고 옴
        JSONObject avedata = dBhelper.PrintMyAvgData("SELECT avg(user_bpm),avg(user_res) from User_health;");
        System.out.println(avedata);

        mybarChart = (BarChart)mView.findViewById(R.id.mybarchart);
        mybarChart.getDescription().setEnabled(false);/////라벨 없애줌
        mybarChart.setTouchEnabled(false);
        mybarChart.setPinchZoom(false);
        YAxis leftAxis = mybarChart.getAxisLeft();



        //X축 label값 결정

        // the labels that should be drawn on the XAxis
        final String[] quarters = new String[] { "나", "평균"};

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return quarters[(int) value];
            }

        };

        XAxis xAxis = mybarChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mybarChart.getAxisRight();
        rightAxis.setEnabled(false);
        //Y축값
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        try {
            barEntries.add(new BarEntry(0f,avedata.getInt("user_bpm")));///나의 데이터를 추가해줌
            barEntries.add(new BarEntry(1f,70));///////나이와 성별에 따른 평균데이터 - 서버에서 받아온게 아님
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BarDataSet dataset = new BarDataSet(barEntries,"심박수");//Y축값을 입력
        dataset.setValueTextSize(15);

        BarData data = new BarData(dataset);
        data.setBarWidth(0.3f);
        mybarChart.setData(data);

        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.frag_today,
                R.layout.frag_week,
                R.layout.frag_month,
                };

        // adding bottom dots
        addBottomDots(0);

        mViewPager.setAdapter(myAdapter);

        return mView;

    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(getActivity());
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

}
