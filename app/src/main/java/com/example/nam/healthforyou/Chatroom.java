package com.example.nam.healthforyou;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Chatroom extends AppCompatActivity{
    //액티비티에서 선언.
    private ClientSocketService mService; //서비스 클래스
    private String who;//누구한테 보낼지
    ChatAdapter chatAdapter;
    ListView chatlist;
    final static int update_message=1;
    final static int sendRecentdata=2;
    final static int sendExactdata=3;
    final static int update_healthmessage=4;
    final static int all_messageUpdate=5;
    ChatItem receiveitem;
    DBhelper dBhelper;
    Context mContext;
    int room_id;
    boolean mServiceIsregistered;
    int sendtype;///채팅방의 종류
    String choose_date;///데이터를 선택할때 선택한 날짜!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        registerReceiver(broadcastReceiver, new IntentFilter("updateChat"));///새로 온메세지를 확인해보라는 말
        mContext = getApplicationContext();
        dBhelper = new DBhelper(mContext, "healthforyou.db", null, 1);///DB정의
        ///채팅 ListviewAdapter 정의
        chatAdapter = new ChatAdapter();
        ///채팅 내용에 대한 리스트뷰
        chatlist = (ListView)findViewById(R.id.chat);
        chatlist.setAdapter(chatAdapter);

        //EditText 정의및 포커스 시 키보드 업
        EditText yourEditText= (EditText) findViewById(R.id.et_content);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);

        ///서비스에서 소켓을 연결하므로 여기서 서비스를 호출하면 mainThread에서 소켓을 호출하게됨
        startServcie_Thread socket_thread = new startServcie_Thread();
        socket_thread.start();

        // 메세지가 올때 스크롤을 아래로 내려주는 부분
        chatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override

            public void onChanged() {
                super.onChanged();
                chatlist.setSelection(chatAdapter.getCount()-1);
            }
        });
        chatlist.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL); // 이게 필수

        ///전송버튼에 관한 로직
        Button btn_send = (Button)findViewById(R.id.btn_health_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_message = (EditText)findViewById(R.id.et_content);
                final String message = et_message.getText().toString();

                //내가 보낸 메세지를 Listview에 추가
                ChatItem me_message = new ChatItem();
                ////시간을 나타내줌
                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // nowDate 변수에 값을 저장한다.
                final String formatDate = sdfNow.format(date);
                ///시간을 더해주기 전에 아이템에 넣어줌
                me_message.item_content=message;

                System.out.println(message+"메세지");
                System.out.println("sendtype: "+sendtype);
                System.out.println("who "+who);

                //////서비스를 통해 보내는 부분
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(sendtype==0)
                        {
                            mService.SendMessage(who,message,formatDate);///누구에게-메세지-시간
                            ///받을 때랑 저장 형식을 맞춰줌
                            JSONObject sendptopJSON = new JSONObject();
                            try {
                                sendptopJSON.put("command","/to");///서버에 보낼 명령어
                                sendptopJSON.put("from","me");///내가 보낸거임
                                sendptopJSON.put("who",who);
                                sendptopJSON.put("message",message);///어떤 내용인지
                                sendptopJSON.put("date",formatDate);///보낸 시간은
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dBhelper.messagejsoninsert(sendptopJSON);///JSON 형식으로 DB에 저장

                        }else if(sendtype==1)
                        {
                            mService.InfoMessage(Integer.parseInt(who),message,formatDate);/////who를 통해 보내므로 parseInt를 통해 값을 보내야됨
                            ///받을 때랑 저장 형식을 맞춰줌
                            JSONObject sendgroupJSON = new JSONObject();
                            try {
                                sendgroupJSON.put("command","/inform");///서버에 보낼 명령어
                                sendgroupJSON.put("room_no",who);
                                sendgroupJSON.put("from","me");///누가 받을 건지
                                sendgroupJSON.put("message",message);///어떤 내용인지
                                sendgroupJSON.put("date",formatDate);///보낸 시간은
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dBhelper.messagejsoninsert(sendgroupJSON);///JSON 형식으로 DB에 저장
                        }
                    }
                });

                thread.start();

                ///내가 보낸 메시지 타입
                me_message.setType(0);
                me_message.item_date=formatDate;

                chatAdapter.addItemME(me_message);
                chatAdapter.notifyDataSetChanged();

                et_message.setText("");
            }
        });

        ImageButton btn_sendhealthdata =(ImageButton)findViewById(R.id.ib_sendhealth);//건강데이터를 보내는 부분
        btn_sendhealthdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
            }
        });
    }

    private void ShowDialog()
    {
        LayoutInflater dialog = LayoutInflater.from(this);
        final View dialogLayout = dialog.inflate(R.layout.choicedata_dialog, null);
        final Dialog myDialog = new Dialog(this);

        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.setContentView(dialogLayout);
        myDialog.show();

        LinearLayout lo_btn_recent = (LinearLayout)myDialog.findViewById(R.id.lo_recent_data);///최근데이터를 보내는 레이아웃 버튼 처럼 작동함
        lo_btn_recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(sendRecentdata);
                myDialog.dismiss();
            }
        });

        LinearLayout lo_btn_whole = (LinearLayout)myDialog.findViewById(R.id.lo_whole_data);///전체적인 데이터중 선택 할수 있는 버튼
        lo_btn_whole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Chatroom.this,Choosedata.class);
                startActivityForResult(intent,1);///특정한 날짜를 받아오라는 의미
                myDialog.dismiss();
            }
        });
    }

    @Override///결과값을 받아오는 부분
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==1)//requestCode는 1 데이터를 받아와라
            {
                choose_date = data.getStringExtra("date");
                handler.sendEmptyMessage(sendExactdata);
            }
        }
    }

    //서비스 커넥션 선언.
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ClientSocketService.ClientSocketServiceBinder binder = (ClientSocketService.ClientSocketServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback(mCallback); //콜백 등록
            ///서비스는 액티비티가 다뜨지 않으면 액티비티와 연결되지 않음
            ////대화 대상이 누구인지 인텐트를 통해 받는 부분
            mServiceIsregistered=true;
            Intent intent = getIntent();
            int from = intent.getIntExtra("from",-1);
            switch(from)
            {
                case 0:
                {
                    who = intent.getStringExtra("who");////누구한테 보낼지 정하는 부분
                    System.out.println("who"+who);
                    sendtype = 0;
                    break;
                }

                case 1://초기 방 생성시 방을 요청해 달라고 하는 부분
                {
                    final String groupList = intent.getStringExtra("groupChat");
                    System.out.println("groupList"+groupList);
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            mService.RequestRoom(groupList);////방을 만들어달라 요청
                        }
                    };
                    thread.start();
                    sendtype = 1;
                }

                case 2://채팅방은 이미 만들어져 있어서 보내기만 하면 되는 상황
                {
                    who = intent.getStringExtra("room_id");///room_id를 받아옴
                    sendtype=1;
                    System.out.println("방아이디 " + who);
                }
            }
            ///기존에 있던 채팅을 뿌려주는 부분
            handler.sendEmptyMessage(all_messageUpdate);

        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    //서비스에서 아래의 콜백 함수를 호출하며, 콜백 함수에서는 액티비티에서 처리할 내용 입력
    private ClientSocketService.ICallback mCallback = new ClientSocketService.ICallback() {
        @Override
        public void Knowroom(String room_no){
            who = room_no;///////room_no를 who에다가 담음
            System.out.println(who+"넘어온 who");
        }
    };

    //서비스 시작.
    public void startServiceMethod(){
        ////소켓에 연결되어 있는 서비스와 채팅창 액티비티를 연결
        Intent Service = new Intent(this, ClientSocketService.class);
        bindService(Service, mConnection, Context.BIND_AUTO_CREATE);
    }

    //액티비티에서 서비스 함수 호출
    public class startServcie_Thread extends Thread
    {
        @Override
        public void run() {
            startServiceMethod();////상대방과 채팅을 해서 오면 서비스 호출
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mServiceIsregistered){/////
            unbindService(mConnection);//서비스와 액티비티의 통신을 끊음
            mServiceIsregistered=false;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case update_message:{/////받은 데이터를 리스트뷰에
                    chatAdapter.addItemYou(receiveitem);
                    chatAdapter.notifyDataSetChanged();
                    break;
                }

                case sendRecentdata://최근 건강 데이터를 보냄
                    {
                        final JSONObject recent_healthdata=dBhelper.PrintHealthChatdata("SELECT * FROM User_health ORDER BY data_signdate desc limit 1;");
                        System.out.println("recent"+recent_healthdata);
                        //내가 보낸 메세지를 Listview에 추가
                        ChatItem me_message = new ChatItem();
                        ////시간을 나타내줌
                        long now = System.currentTimeMillis();
                        // 현재시간을 date 변수에 저장한다.
                        Date date = new Date(now);
                        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        // nowDate 변수에 값을 저장한다.
                        final String formatDate = sdfNow.format(date);
                        ///시간을 더해주기 전에 아이템에 넣어줌
                        System.out.println(recent_healthdata+"최근 데이터Chat");
                        //////서비스를 통해 보내는 부분
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(sendtype==0)
                                {
                                    mService.SendHealthdata(who,recent_healthdata,formatDate);///누구에게-메세지-시간
                                    JSONObject sendptophealthJSON = new JSONObject();
                                    try {
                                        sendptophealthJSON.put("command","/tohealth");///서버에 보낼 명령어
                                        sendptophealthJSON.put("from","me");///내가 보낸거임
                                        sendptophealthJSON.put("who",who);
                                        sendptophealthJSON.put("message",recent_healthdata);///어떤 내용인지
                                        sendptophealthJSON.put("date",formatDate);///보낸 시간은
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dBhelper.messagejsoninsert(sendptophealthJSON);///JSON 형식으로 DB에 저장
                                }else if(sendtype==1)
                                {
                                    mService.SendHealthdata(Integer.parseInt(who),recent_healthdata,formatDate);///누구에게-메세지-시간
                                    JSONObject sendgrouphealthJSON = new JSONObject();
                                    try {
                                        sendgrouphealthJSON.put("command","/informhealth");///서버에 보낼 명령어
                                        sendgrouphealthJSON.put("room_no",who);
                                        sendgrouphealthJSON.put("from","me");///내가 보낸거임
                                        sendgrouphealthJSON.put("message",recent_healthdata);///어떤 내용인지
                                        sendgrouphealthJSON.put("date",formatDate);///보낸 시간은
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dBhelper.messagejsoninsert(sendgrouphealthJSON);///JSON 형식으로 DB에 저장
                                }
                            }
                        });

                        thread.start();

                        ///내가 보낸 메시지 타입
                        me_message.setType(2);//건강 데이터 보내는 형식
                        me_message.item_date=formatDate;
                        me_message.user_bpm = recent_healthdata.optInt("user_bpm");
                        me_message.user_res = recent_healthdata.optInt("user_res");
                        me_message.data_signdate = recent_healthdata.optString("data_signdate");
                        chatAdapter.addItemHealthME(me_message);
                        chatAdapter.notifyDataSetChanged();

                        break;
                    }

                case sendExactdata:
                    {
                        final JSONObject recent_healthdata=dBhelper.PrintHealthChatdata_forgrid("SELECT avg(user_bpm),avg(user_res),strftime('%Y-%m-%d',data_signdate) as date from User_health WHERE date= '" + choose_date + "' GROUP BY strftime('%Y-%m-%d',data_signdate);");
                        System.out.println("recent"+recent_healthdata);
                        //내가 보낸 메세지를 Listview에 추가
                        ChatItem me_message = new ChatItem();
                        ////시간을 나타내줌
                        long now = System.currentTimeMillis();
                        // 현재시간을 date 변수에 저장한다.
                        Date date = new Date(now);
                        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        // nowDate 변수에 값을 저장한다.
                        final String formatDate = sdfNow.format(date);
                        ///시간을 더해주기 전에 아이템에 넣어줌
                        System.out.println(recent_healthdata+"최근 데이터Chat");
                        //////서비스를 통해 보내는 부분
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(sendtype==0)
                                {
                                    mService.SendHealthdata(who,recent_healthdata,formatDate);///누구에게-메세지-시간
                                }else if(sendtype==1)
                                {
                                    mService.SendHealthdata(Integer.parseInt(who),recent_healthdata,formatDate);///누구에게-메세지-시간
                                }
                            }
                        });

                        thread.start();

                        ///내가 보낸 메시지 타입
                        me_message.setType(2);//건강 데이터 보내는 형식
                        me_message.item_date=formatDate;
                        me_message.user_bpm = recent_healthdata.optInt("user_bpm");
                        me_message.user_res = recent_healthdata.optInt("user_res");
                        me_message.data_signdate = recent_healthdata.optString("data_signdate");
                        chatAdapter.addItemHealthME(me_message);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    }

                case update_healthmessage:{
                    chatAdapter.addItemHealthYou(receiveitem);
                    chatAdapter.notifyDataSetChanged();
                }

                case all_messageUpdate:{
                    ///기존에 있던 채팅을 뿌려주는 부분
                    ArrayList<JSONObject> messageList = dBhelper.getAllmessage("SELECT * from ChatMessage WHERE is_looked=1 and room_id= '" + who + "'"+"ORDER BY message_no DESC");
                    System.out.println(messageList+"굿굿");
                    for(int i=0;i<messageList.size();i++)
                    {
                        JSONObject jsonObject=messageList.get(i);
                        JSONObject friendinfo=dBhelper.getFriend(jsonObject.optString("message_sender"));//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임
                        if(jsonObject.optString("message_sender").equals("me"))//나의 메세지
                        {
                            ChatItem chatitem = new ChatItem();
                            try {//JSON형식으로 파싱이 되면 message_content는 JSON 형식의 건강데이터임 - //건강데이터
                                String message = jsonObject.optString("message_content");
                                JSONObject healthdata = new JSONObject(message);
                                System.out.println(healthdata);
                                chatitem.user_bpm=healthdata.optInt("user_bpm");
                                chatitem.user_res=healthdata.optInt("user_res");
                                chatitem.data_signdate=healthdata.optString("data_signdate");
                                chatitem.item_date = jsonObject.optString("message_date");
                                chatitem.setType(2);
                                if(friendinfo.length()!=0)//친구가 있음
                                {
                                    chatitem.item_sender = friendinfo.optString("user_name");//친구의 이름을 넣어줌
                                }else{//친구가 없음
                                    chatitem.item_sender = jsonObject.optString("message_sender");//친구의 아이디를 보여줌
                                }
                                chatAdapter.addItemHealthME(0,chatitem);

                            } catch (JSONException e) {//건강데이터가 아니면 그냥 메세지임 - //건강데이터가 아닌것은 그냥 메세지
                                chatitem.item_content = jsonObject.optString("message_content");
                                chatitem.item_date = jsonObject.optString("message_date");
                                chatitem.setType(0);
                                if(friendinfo.length()!=0)//친구가 있음
                                {
                                    chatitem.item_sender = friendinfo.optString("user_name");//친구의 이름을 넣어줌
                                }else{//친구가 없음
                                    chatitem.item_sender = jsonObject.optString("message_sender");//친구의 아이디를 보여줌
                                }
                                chatAdapter.addItemME(0,chatitem);
                            }

                        }else{//다른 사람이 보낸 메세지

                            ChatItem chatitem = new ChatItem();
                            try {//JSON형식으로 파싱이 되면 message_content는 JSON 형식의 건강데이터임 - //건강데이터
                                String message = jsonObject.optString("message_content");
                                JSONObject healthdata = new JSONObject(message);
                                System.out.println(healthdata);
                                chatitem.user_bpm=healthdata.optInt("user_bpm");
                                chatitem.user_res=healthdata.optInt("user_res");
                                chatitem.data_signdate=healthdata.optString("data_signdate");
                                chatitem.item_date = jsonObject.optString("message_date");
                                chatitem.setType(3);
                                if(friendinfo.length()!=0)//친구가 있음
                                {
                                    chatitem.item_sender = friendinfo.optString("user_name");//친구의 이름을 넣어줌
                                }else{//친구가 없음
                                    chatitem.item_sender = jsonObject.optString("message_sender");//친구의 아이디를 보여줌
                                }
                                chatAdapter.addItemHealthYou(0,chatitem);

                            } catch (JSONException e) {//건강데이터가 아니면 그냥 메세지임 - //건강데이터가 아닌것은 그냥 메세지
                                chatitem.item_content = jsonObject.optString("message_content");
                                chatitem.item_date = jsonObject.optString("message_date");
                                chatitem.setType(1);
                                if(friendinfo.length()!=0)//친구가 있음
                                {
                                    chatitem.item_sender = friendinfo.optString("user_name");//친구의 이름을 넣어줌
                                }else{//친구가 없음
                                    chatitem.item_sender = jsonObject.optString("message_sender");//친구의 아이디를 보여줌
                                }
                                chatAdapter.addItemYou(0,chatitem);
                            }
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                }

            }
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {////
        @Override
        public void onReceive(Context context, Intent intent) {

            String query = "SELECT * FROM ChatMessage WHERE is_looked=0 and room_id= '" + who + "'"+"ORDER BY message_no DESC LIMIT 1;";//////번호 순으로 처리
            JSONObject jsonObject=dBhelper.updatemessage(query);//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임
            System.out.println(jsonObject);
            JSONObject friendinfo=dBhelper.getFriend(jsonObject.optString("message_sender"));//room_id는 개인과 개인일 때는 상대방의 아이디, 그룹채팅일때는 방번호임

            //분리한 데이터를 리스트뷰에 들어갈 아이템 객체로 변환 - 다른 사람이 보낸 메세지 타입
            if(jsonObject.length()!=0){//////JSONObject가 비었는지 판단 - 길이로 판단해야됨
                receiveitem = new ChatItem();

                try {//JSON형식으로 파싱이 되면 message_content는 JSON 형식의 건강데이터임
                    String message = jsonObject.optString("message_content");
                    JSONObject healthdata = new JSONObject(message);
                    System.out.println(healthdata);
                    receiveitem.user_bpm=healthdata.optInt("user_bpm");
                    receiveitem.user_res=healthdata.optInt("user_res");
                    receiveitem.data_signdate=healthdata.optString("data_signdate");
                    receiveitem.item_date = jsonObject.optString("message_date");
                    receiveitem.setType(3);
                    if(friendinfo.length()!=0)//친구가 있음
                    {
                        receiveitem.item_sender = friendinfo.optString("user_name");//친구의 이름을 넣어줌
                    }else{//친구가 없음
                        receiveitem.item_sender = jsonObject.optString("message_sender");//친구의 아이디를 보여줌
                    }
                    handler.sendEmptyMessage(update_healthmessage);

                } catch (JSONException e) {//건강데이터가 아니면 그냥 메세지임
                    receiveitem.item_content = jsonObject.optString("message_content");
                    receiveitem.item_date = jsonObject.optString("message_date");
                    receiveitem.setType(1);
                    if(friendinfo.length()!=0)//친구가 있음
                    {
                        receiveitem.item_sender = friendinfo.optString("user_name");//친구의 이름을 넣어줌
                    }else{//친구가 없음
                        receiveitem.item_sender = jsonObject.optString("message_sender");//친구의 아이디를 보여줌
                    }
                    handler.sendEmptyMessage(update_message);
                }
                if(friendinfo.length()!=0)//친구가 있음
                {
                    receiveitem.item_sender = friendinfo.optString("user_name");//친구의 이름을 넣어줌
                }else{//친구가 없음
                    receiveitem.item_sender = jsonObject.optString("message_sender");//친구의 아이디를 보여줌
                }

                String updateStateQuery = "UPDATE ChatMessage SET is_looked=1 WHERE is_looked=0 and room_id= '" + who + "';";
                dBhelper.update(updateStateQuery);

            }else{
                System.out.println("다른 사람이 메세지를 보냄");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
