package com.example.nam.healthforyou;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by NAM on 2017-08-08.
 */

public class ClientSocketService extends Service {

    private String html = "";
    private Handler mHandler;

    private Socket socket;

    private BufferedReader networkReader;
    private BufferedWriter networkWriter;
    private PrintWriter networkPrintwriter;
    boolean endflag = false;
    private Context mContext;
    String line="";
    private String ip = "115.71.232.242"; //SERVER IP
    private int port = 9999; // PORT번호
    public ClientSocketService() {
        super();
    }
    public DBhelper dBhelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        dBhelper = new DBhelper(mContext, "healthforyou.db", null, 1);
    }

    //서비스 바인더 내부 클래스 선언
    public class ClientSocketServiceBinder extends Binder {
        ClientSocketService getService() {
            return ClientSocketService.this; //현재 서비스를 반환.
        }
    }

    private final IBinder mBinder = new ClientSocketServiceBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ////소켓 설정 - 서비스는 화면이 없는 액티비티라고 이해한다면 MainThread에서 Socket을 호출하면 안됨
        SocketThread socketThread = new SocketThread();
        socketThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        return mBinder;
    }

    //콜백 인터페이스 선언 - 액티비티에 있는 메쏘드를 선언해줌
    public interface ICallback {
        public void recvData(); //액티비티에서 선언한 콜백 함수.
        public void ReceiveMessage(String params);
        public void Knowroom(String room_no);
    }

    private ICallback mCallback;

    //액티비티에서 콜백 함수를 등록하기 위함.
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    //액티비티에서 서비스 함수를 호출하기 위한 함수 생성
    public void ChatServiceFunc(){
        SharedPreferences useremail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
        String user=useremail.getString("useremail","false");
        if(!user.equals("false"))/////////서버에 접속
        {
            networkPrintwriter.println(user);
            networkPrintwriter.flush();
            Receiving();/////상대방이 보낸 메세지를 받는 부분
        }else{
            System.out.println("디버깅 요망 문제가 있어요");
        }
    }

    ///메세지를 받는 쓰레드 객체 실행 - 액티비티에서 서비스를 실행하는 부분
    public void Receiving(){
        InputThread inputThread = new InputThread(socket,networkReader);
        inputThread.start();
    }

    public void SendMessage(String message,String who_receive){
        networkPrintwriter.println("/to "+who_receive+" "+message);
        networkPrintwriter.flush();
    }

    public void RequestRoom(String member)
    {
        networkPrintwriter.println("/makeroom "+member);
        networkPrintwriter.flush();
    }

    public void InfoMessage(String message,int room_no)
    {
        networkPrintwriter.println("/inform"+" "+room_no+" "+message);
        networkPrintwriter.flush();
    }



    //*********서비스에서 액티비티 함수 호출은..

    //mCallback.recvData();

    public void setSocket(String ip, int port) throws IOException {

        try {
            socket = new Socket(ip, port);
        }catch (IOException e) {
         System.out.println("서버 재가동 요망");
        }finally{
            //System.out.println("서버 재가동 요망");
        }

    }
    /******************************************************************
     5. 서버로부터 문자열을 읽어 들여 액티비티로 넘겨줄 생각
     ******************************************************************/
    public class InputThread extends Thread{

        private Socket sock = null;
        private BufferedReader br = null;
        public InputThread(Socket sock, BufferedReader br){
            this.sock = sock;
            this.br = br;
        }

        public void run(){
            try{
                String line = null;
                while((line = br.readLine())!=null){
                    System.out.println(line);
                    //mCallback.ReceiveMessage(line);////입력을 받으면 액티비티로 값을 넘겨줌
                    if(line.contains("room_no"))
                    {
                        System.out.println("방완성");
                        System.out.println("방번호 : "+line.substring(8));
                        mCallback.Knowroom(line.substring(8));
                    }else{
                        dBhelper.messageinsert(line);///로컬 DB에 socket을 통해 온값 저장
                        ///////LocalDB에 저장
                        ///////데이터베이스가 바뀌었음을 브로드캐스트 리시버에게 보냄
                        Intent intent = new Intent();
                        intent.setAction("com.example.nam.healthforyou.DATABASE_CHANGED");
                        mContext.sendBroadcast(intent);
                    }

                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    ///Socket연결을 해주는 쓰레드
    public class SocketThread extends Thread{
        @Override
        public void run() {
            ////Socket 설정
            try {
                setSocket(ip,port);
                networkPrintwriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                //System.out.println("서버 재가동 요망");
            }

            ////서버에 접속
            ChatServiceFunc();/////
        }
    }

}
