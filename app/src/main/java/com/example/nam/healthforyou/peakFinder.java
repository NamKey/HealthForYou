package com.example.nam.healthforyou;

import java.util.ArrayList;

/**
 * Created by NAM on 2017-07-18.
 */

public class peakFinder {
    public int init =0; // 초깃값
    public int W_size =0;  //윈도우 사이즈

    public ArrayList<Integer> data;
    public ArrayList<Integer> peakX;

    ///피크를 찾는 함수
    public void peakfind(int input)
    {
        data.add(input);///인풋을 데이터에 넣어줌
    }


    ///최솟값을 찾아 윈도우 크기를 정해주는 함수
    public int setWindow(int input)
    {
        return W_size;
    }
}
