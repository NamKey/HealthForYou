package com.example.nam.healthforyou;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

public class Chatroom extends AppCompatActivity {
    //액티비티에서 선언.
    private ClientSocketService mService; //서비스 클래스
    private String who;//누구한테 보낼지
    ChatAdapter chatAdapter;
    int state;
    final static int update_message=1;
    ChatItem receiveitem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

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

        ////친구 이메일 인텐트로 받기
        Intent intent = getIntent();
        who = intent.getStringExtra("who");
        Button btn_send = (Button)findViewById(R.id.btn_health_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_message = (EditText)findViewById(R.id.et_content);
                String message = et_message.getText().toString();
                System.out.println(message);
                mService.SendMessage(message,who);

                //내가 보낸 메세지를 Listview에 추가
                ChatItem me_message = new ChatItem();
                me_message.item_content=message;
                me_message.item_sender="";
                chatAdapter.addItem(me_message);
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
            String who = line.split(":",2)[0];
            String message = line.split(":",2)[1];
            receiveitem = new ChatItem();
            receiveitem.item_content = message;
            receiveitem.item_sender = who;
            handler.sendEmptyMessage(update_message);
        }
    };

    //서비스 시작.
    public void startServiceMethod(){
        Intent Service = new Intent(this, ClientSocketService.class);
        startService(Service);
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
                    chatAdapter.addItem(receiveitem);
                    chatAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    };
}
