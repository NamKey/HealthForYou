package com.example.nam.healthforyou;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Chatroom extends AppCompatActivity {
    //액티비티에서 선언.
    private ClientSocketService mService; //서비스 클래스
    private String who;//누구한테 보낼지
    private String sender;//보낸사람
    ChatAdapter chatAdapter;
    int state;
    final static int update_message=1;
    ChatItem receiveitem;
    DBhelper dBhelper;
    Context mContext;
    int room_id;

    int sendtype;///채팅방의 종류

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
        ListView chatlist = (ListView)findViewById(R.id.chat);
        chatlist.setAdapter(chatAdapter);

        //EditText 정의및 포커스 시 키보드 업
        EditText yourEditText= (EditText) findViewById(R.id.et_content);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(yourEditText, InputMethodManager.SHOW_IMPLICIT);

        ///서비스에서 소켓을 연결하므로 여기서 서비스를 호출하면 mainThread에서 소켓을 호출하게됨
        startServcie_Thread socket_thread = new startServcie_Thread();
        socket_thread.start();

        ///전송버튼
        Button btn_send = (Button)findViewById(R.id.btn_health_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_message = (EditText)findViewById(R.id.et_content);
                String message = et_message.getText().toString();
                //내가 보낸 메세지를 Listview에 추가
                ChatItem me_message = new ChatItem();
                ////시간을 나타내줌
                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // nowDate 변수에 값을 저장한다.
                String formatDate = sdfNow.format(date);
                ///시간을 더해주기 전에 아이템에 넣어줌
                me_message.item_content=message;

                message = message + ":" + formatDate;
                System.out.println(message+"메세지");
                System.out.println("sendtype: "+sendtype);
                System.out.println("who "+who);
                final String finalMessage = message;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(sendtype==0)
                        {
                            mService.SendMessage(finalMessage,who);
                        }else if(sendtype==1)
                        {
                            mService.InfoMessage(finalMessage,Integer.parseInt(who));/////who를 통해 보내므로 parseInt를 통해 값을 보내야됨
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
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    //서비스에서 아래의 콜백 함수를 호출하며, 콜백 함수에서는 액티비티에서 처리할 내용 입력
    private ClientSocketService.ICallback mCallback = new ClientSocketService.ICallback() {
        public void recvData() {
            mService.ChatServiceFunc();
            //처리할 일들..
        }

        public void ReceiveMessage(String line)
        {
            /////전송된 데이터를 구분자를 통해 분리함
            String who = line.split(":",3)[0];
            String message = line.split(":",3)[1];
            String date = line.split(":",3)[2];
        }

        @Override
        public void Knowroom(String room_no) {
            who = room_no;///////room_no를 who에다가 담음
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
        unbindService(mConnection);//서비스와 액티비티의 통신을 끊음
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
            }
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {////
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println(who+"broadcastReceiver who");
            String query = "SELECT * FROM ChatMessage WHERE is_looked=0 and room_id= '" + who + "'"+"ORDER BY datetime(message_date) DESC LIMIT 1;";//////변수를 통해 하려면 ''를 통해 처리 한 쿼리를 사용해야함
            JSONObject jsonObject=dBhelper.updatemessage(query);
            System.out.println(jsonObject);

            //분리한 데이터를 리스트뷰에 들어갈 아이템 객체로 변환 - 다른 사람이 보낸 메세지 타입
            if(jsonObject.length()!=0){//////JSONObject가 비었는지 판단 - 길이로 판단해야됨
                receiveitem = new ChatItem();
                receiveitem.item_content = jsonObject.optString("message_content");
                receiveitem.item_sender = jsonObject.optString("message_sender");
                receiveitem.item_date = jsonObject.optString("message_date");
                receiveitem.setType(1);
                handler.sendEmptyMessage(update_message);

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
