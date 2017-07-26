package com.example.nam.healthforyou;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_main extends Fragment {

    public TextView tv;
    String strurl = "http://kakapo12.vps.phps.kr/mainactivity.php";
    HttpURLConnection con;

    //보여줄 데이터
    String id;
    int bpm;
    int res;
    int pre;
    int oxi;
    String time;
    JSONObject health_data;
    //데이터를 뿌려주는 TextView
    TextView heart_rate;
    TextView RIIV;
    TextView pressure;
    TextView oxygen;
    TextView date;
    //Fragment 이동시 저장시켜주는 부분

    DBhelper dbManager;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout main = (RelativeLayout) inflater.inflate(R.layout.frag_main,container,false);
        heart_rate =(TextView)main.findViewById(R.id.tv_heartrate);
        RIIV = (TextView)main.findViewById(R.id.tv_riiv);
        pressure = (TextView)main.findViewById(R.id.tv_pressure);
        oxygen = (TextView)main.findViewById(R.id.tv_spo2);
        date = (TextView)main.findViewById(R.id.tv_date);
        ////DB를 불러옴
        dbManager = new DBhelper(getActivity().getApplicationContext(), "healthforyou.db", null, 1);//DB생성
        String init=dbManager.PrintData("SELECT * FROM User_health;");
        //생각해야 될 부분

        /*
        1. SQLite와 서버 DB연동
        2. 연동 타이밍을 생각해야됨
          - 결과를 기록할 때 로컬에도 기록할 것인지
          - 아니면 메인부분에서만 기록할 것인지
        */

        if(init.equals("false"))//아무자료가 없으면 AsyncTask를 통해 자료를 받아옴
        {
            try {
                URL url = new URL(strurl);
                con = (HttpURLConnection)url.openConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }

            NetworkTask networkTask = new NetworkTask(strurl, null);
            networkTask.execute();
        }else{//자료가 있으면 최근 하나의 데이터를 SQlite에서 받아옴
            JSONObject local_healthdata=dbManager.PrintHealthData("SELECT * FROM User_health ORDER BY data_signdate asc limit 1;");
            //System.out.println(local_healthdata);
            try {
                bpm=local_healthdata.getInt("user_bpm");
                res=local_healthdata.getInt("user_res");
                pre=local_healthdata.getInt("user_pre");
                oxi=local_healthdata.getInt("user_oxi");
                time=local_healthdata.getString("data_signdate");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //텍스트뷰에 뿌려줌
            heart_rate.setText(bpm+" BPM");
            RIIV.setText(res+" 회/분");
            pressure.setText(pre+"mmHg");
            oxygen.setText(oxi+" %");
            date.setText(time);
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
            try {//서버DB에 아무것도 없는 상태라면 JSONException
                health_data = new JSONObject(s);
                bpm=health_data.getInt("bpm");//bpm을 기준으로 삼음
                res=health_data.getInt("res");
                pre=health_data.getInt("pre");
                oxi=health_data.getInt("oxi");
                time=health_data.getString("data_signdate");

                //DB에 자료를 넣어줌 - LocalDB(SQlite)
                dbManager.infoinsert(health_data);

                //텍스트뷰에 뿌려줌
                heart_rate.setText(bpm+" BPM");
                RIIV.setText(res+" 회/분");
                pressure.setText(pre+"mmHg");
                oxygen.setText(oxi+" %");
                date.setText(time);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
