package com.example.nam.healthforyou;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_main extends Fragment {

    String strurl = "http://kakapo12.vps.phps.kr/mainactivity.php";
    HttpURLConnection con;

    //보여줄 데이터
    String id;
    int bpm;
    int res;

    String time;
    JSONObject health_data;

    //데이터를 뿌려주는 TextView
    TextView heart_rate;
    TextView RIIV;
    TextView date;
    TextView graphmessage;
    ImageView graph;
    //Fragment 이동시 저장시켜주는 부분
    final static int update_main=1;
    DBhelper dbManager;

    Handler main_handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what)
            {
                case update_main:
                {
                    //텍스트뷰에 뿌려줌
                    heart_rate.setText(bpm+" BPM");
                    RIIV.setText(res+" 회/분");
                    date.setText(time);
                    graphmessage.setText("맥박 그래프");
                    graph.setImageResource(R.drawable.image);
                    break;
                }
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout main = (RelativeLayout) inflater.inflate(R.layout.frag_main,container,false);

        heart_rate =(TextView)main.findViewById(R.id.tv_heartrate);
        RIIV = (TextView)main.findViewById(R.id.tv_riiv);
        date = (TextView)main.findViewById(R.id.tv_date);
        graph = (ImageView)main.findViewById(R.id.graphImage);
        graphmessage = (TextView)main.findViewById(R.id.graphmessage);
        ////DB를 불러옴
        dbManager = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB생성
        String init=dbManager.PrintData("SELECT * FROM User_health;");//유저의 건강정보 모두 받아오기
        //생각해야 될 부분

        /*
        1. SQLite와 서버 DB연동
        2. 연동 타이밍을 생각해야됨
          - 결과를 기록할 때 로컬에도 기록할 것인지
          - 아니면 메인부분에서만 기록할 것인지
        */

        System.out.println(init);//유저의 건강정보 모두 출력

        if(init.equals("false"))//SQlite에 아무자료가 없으면 AsyncTask를 통해 자료를 받아옴
        {
            try {
                URL url = new URL(strurl);
                con = (HttpURLConnection)url.openConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }

            NetworkTask networkTask = new NetworkTask(strurl, null);
            networkTask.execute();

        }else{//자료가 있으면 최근 데이터(limit 사용)를 SQlite에서 받아옴
            JSONObject local_healthdata=dbManager.PrintHealthData("SELECT * FROM User_health ORDER BY data_signdate desc limit 1;");
            System.out.println(local_healthdata);
            try {
                bpm=local_healthdata.getInt("user_bpm");
                res=local_healthdata.getInt("user_res");
                time=local_healthdata.getString("data_signdate");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //텍스트뷰에 뿌려줌
            main_handler.sendEmptyMessage(update_main);

        }

        // AsyncTask를 통해 HttpURLConnection 수행.
        return main;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpConnection requestHttpURLConnection = new RequestHttpConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            System.out.println("response"+s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                System.out.println("jsonArray"+jsonArray);
                for(int i=0;i<jsonArray.length();i++)
                {
                    health_data=new JSONObject(jsonArray.getString(i));
                    health_data.put("is_synced",1);
                    //DB에 자료를 넣어줌 - LocalDB(SQlite)
                    dbManager.infoinsert(health_data);
                    System.out.println(health_data);
                }

                //데이터를 SQlite에서 갖고와서 뿌려줌 - 인터넷이 연결 안됐을 때 도 생각?
                JSONObject local_healthdata=dbManager.PrintHealthData("SELECT * FROM User_health ORDER BY data_signdate desc limit 1;");
                System.out.println(local_healthdata);
                try {
                    bpm=local_healthdata.getInt("user_bpm");
                    res=local_healthdata.getInt("user_res");
                    time=local_healthdata.getString("data_signdate");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //텍스트를 핸들러를 통해 띄어줌
                main_handler.sendEmptyMessage(update_main);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
