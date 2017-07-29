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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (RelativeLayout)inflater.inflate(R.layout.frag_result,container,false);
        mViewPager = (ViewPager)mView.findViewById(R.id.health_viewpager);
        dotsLayout = (LinearLayout)mView.findViewById(R.id.layoutDots);
        myAdapter = new ViewPagerAdapter(getChildFragmentManager());

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
