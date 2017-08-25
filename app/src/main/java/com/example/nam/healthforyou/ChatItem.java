package com.example.nam.healthforyou;

/**
 * Created by NAM on 2017-08-08.
 */

public class ChatItem {

    private int type ;///내가 보낸 카톡과 아닌것을 구분하기 위함

    String item_sender;
    String item_senderId;
    String item_content;
    String item_date;

    int user_bpm;//건강 데이터를 보내기 위한 부분
    int user_res;
    String data_signdate;//측정한 날짜

    /////리스트뷰 타입을 분류
    public void setType(int type) { this.type = type ; }
    public int getType() { return this.type ; }




}
