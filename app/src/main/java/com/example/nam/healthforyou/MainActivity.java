package com.example.nam.healthforyou;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG ="MainActivity";

    static {
        System.loadLibrary("native-lib");
    }
    private TextView tv_outPut;
    HttpURLConnection con;

    UiTask uiTask;

    ////날짜에 따른 요일 알려주는 메쏘드
    static public int getDateDay(String date, String dateType) throws Exception {

        String day = "" ;

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType, Locale.getDefault()) ;
        Date nDate = dateFormat.parse(date) ;

        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);

        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;

        return dayNum ;
    }

    static public int getDateMonth(String date, String dateType) throws Exception {

        String tempdate = date+"01";//////DB에서 긁어온 데이터가 월별로 GROUP BY 되어 있으므로
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType,Locale.getDefault()) ;
        Date nDate = dateFormat.parse(tempdate) ;

        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);

        int dayMonth = cal.get(Calendar.MONTH) ;

        return dayMonth+1;////MONTH는 JAVA에서 0~11이므로 1씩더해줘야됨 -> 1~12
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    //메인 페이지를 불러옴
    setContentView(R.layout.activity_main);
    //Fragment 2017.07.22 현재 측정 fragment의 layout과 복잡도로 인해 최초 전환시 깜빡임
    //AsyncTask를 통해 미리 불러옴
    uiTask = new UiTask();
    uiTask.execute();

    findViewById(R.id.btn_frag1_main).setOnClickListener(this);
    findViewById(R.id.btn_frag2_chat).setOnClickListener(this);
    findViewById(R.id.btn_frag3_meas).setOnClickListener(this);
    findViewById(R.id.btn_frag4_result).setOnClickListener(this);

    // 위젯에 대한 참조.

    // URL 설정.
    String strurl = "http://kakapo12.vps.phps.kr/mainactivity.php";

        try {
            URL url = new URL(strurl);
            con = (HttpURLConnection)url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }



    FirebaseInstanceId.getInstance().getToken();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_frag1_main:
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_main())
                    .commit();
                break;

            case R.id.btn_frag2_chat:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frag_container_, new Fragment_chat())
                        .commit();
                break;

            case R.id.btn_frag3_meas:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frag_container_, new Fragment_meas())
                        .commit();
                break;

            case R.id.btn_frag4_result:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frag_container_, new Fragment_result())
                        .commit();
                break;
        }

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
            tv_outPut.setText(s);
        }
    }

    public class UiTask extends  AsyncTask<Void,Void,Void>{
        @Override
        protected void onPostExecute(Void aVoid) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_result())
                    .commit();
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.frag_container_, new Fragment_chat())
//                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_meas())
                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_main())
                    .commit();
        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }
    }
}


