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

import org.json.JSONException;
import org.json.JSONObject;

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
    InputThread inputThread;
    JSONObject getjson;//Nullpointer 방지?
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
        void Knowroom(String room_no);
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
        inputThread = new InputThread(socket,networkReader);
        inputThread.start();
    }

    public void SendMessage(String who_receive,String message,String date){
        JSONObject sendptopJSON = new JSONObject();
        try {
            sendptopJSON.put("command","/to");///서버에 보낼 명령어
            sendptopJSON.put("who_receive",who_receive);///누가 받을 건지
            sendptopJSON.put("message",message);///어떤 내용인지
            sendptopJSON.put("date",date);///보낸 시간은
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(sendptopJSON.toString());
        networkPrintwriter.println(sendptopJSON.toString());//JSON을 String으로 보냄
        networkPrintwriter.flush();
    }

    /////건강데이터 보내는 Method
    public void SendHealthdata(String who_receive,JSONObject healthdata,String date){
        JSONObject sendptopJSON = new JSONObject();
        try {
            sendptopJSON.put("command","/tohealth");///서버에 보낼 명령어
            sendptopJSON.put("who_receive",who_receive);///누가 받을 건지
            sendptopJSON.put("message",healthdata.toString());///어떤 내용인지
            sendptopJSON.put("date",date);///보낸 시간은
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkPrintwriter.println(sendptopJSON.toString());//JSON을 String으로 보냄
        networkPrintwriter.flush();
    }

    public void SendHealthdata(int room_no,JSONObject healthdata,String date){
        JSONObject infomessageJSON = new JSONObject();
        try {
            infomessageJSON.put("command","/informhealth");///서버에 보낼 명령어
            infomessageJSON.put("room_no",String.valueOf(room_no));///어떤 방한테 메세지를 보낼건지
            infomessageJSON.put("message",healthdata.toString());
            infomessageJSON.put("date",date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        networkPrintwriter.println(infomessageJSON.toString());//JSON을 String으로 보냄
        networkPrintwriter.flush();
    }

    public void RequestRoom(String member)
    {
        JSONObject requestRoomJSON = new JSONObject();
        try {
            requestRoomJSON.put("command","/makeroom");///서버에 보낼 명령어
            requestRoomJSON.put("who_receive",member);///누가 받을 건지
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkPrintwriter.println(requestRoomJSON.toString());
        networkPrintwriter.flush();
    }

    public void InfoMessage(int room_no,String message,String date)
    {
        JSONObject infomessageJSON = new JSONObject();
        try {
            infomessageJSON.put("command","/inform");///서버에 보낼 명령어
            infomessageJSON.put("room_no",String.valueOf(room_no));///어떤 방한테 메세지를 보낼건지
            infomessageJSON.put("message",message);
            infomessageJSON.put("date",date);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        networkPrintwriter.println(infomessageJSON.toString());
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
                while((line = br.readLine())!=null){
                    System.out.println(line+"line");
                    //mCallback.ReceiveMessage(line);////입력을 받으면 액티비티로 값을 넘겨줌
                    JSONObject getjson = new JSONObject(line);
                    System.out.println(getjson);
                    //없는 값을 찾으려고 하면 JSON exception을 통해 while문을 빠져나감
                    if(getjson.getString("command").equals("/makeroom"))//TODO 방완성과 메세지를 받는 것을 구분해야함
                    {
                        if(mCallback!=null)//////이부분이 필요한 이유 - 채팅방과 연결되는 메쏘드 Knowroom은 mCallback 인터페이스를 통해 구현되는데
                        {//방을 만든 당사자는 채팅창에 있지만 나머지 사람은 채팅창에 있을지 모르기 때문에 null 예외처리를 통해 nullpointer exception을 막음
                            mCallback.Knowroom(getjson.getString("room_no"));//int로 넘겨주는 이유 String은 객체이므로 넘겨주는 것은 값이 아닌 주소이므로 값을 넘겨줘야됨
                        }
                        dBhelper.makeRoominsert(getjson);//방의 정보를 저장 room_no,chatmember
                    }
                    else{//TODO JSON으로 바꿀 예정 20170819
                        dBhelper.messagejsoninsert(getjson);
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
            }

            ////서버에 접속
            ChatServiceFunc();/////
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("destroy call");
        if(socket!=null)
        {
            try {
                    socket.shutdownOutput();//소켓의 아웃풋 종료
                    socket.shutdownInput();//소켓의 인풋 종료

                    socket.close();///소켓연결 종료

                    inputThread.interrupt();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
