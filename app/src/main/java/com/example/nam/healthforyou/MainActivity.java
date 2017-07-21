package com.example.nam.healthforyou;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static {
        System.loadLibrary("native-lib");
    }
    private TextView tv_outPut;
    HttpURLConnection con;
    SharedPreferences session;
    SharedPreferences.Editor session_editor;
    private String session_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //Fragment 첫페이지 불러옴
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.frag_container_, new Fragment_main())
            .commit();
    //메인 페이지를 불러옴
    setContentView(R.layout.activity_main);

    findViewById(R.id.btn_frag1_main).setOnClickListener(this);
    findViewById(R.id.btn_frag2_chat).setOnClickListener(this);
    findViewById(R.id.btn_frag3_meas).setOnClickListener(this);
    findViewById(R.id.btn_frag4_result).setOnClickListener(this);

    // Example of a call to a native metho

    //tv.setText(stringFromJNI());

    // 위젯에 대한 참조.

    // URL 설정.
    String strurl = "http://kakapo12.vps.phps.kr/mainactivity.php";

        try {
            URL url = new URL(strurl);
            con = (HttpURLConnection)url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }


    // AsyncTask를 통해 HttpURLConnection 수행.
    //NetworkTask networkTask = new NetworkTask(strurl, null);
    //networkTask.execute();
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
                //startActivity(new Intent(this,Measure.class));
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

    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }




}


