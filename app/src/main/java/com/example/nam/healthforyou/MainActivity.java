package com.example.nam.healthforyou;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import static com.example.nam.healthforyou.Login.msCookieManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;
    private static final String TAG ="MainActivity";
    //Back button
    private BackPressCloseHandler backPressCloseHandler;

    HttpURLConnection con;

    UiTask uiTask;

    DBhelper dBhelper;
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

    @Override////메뉴 세팅
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id)
        {
            case R.id.action_settings:
            {
                Intent intent = new Intent(this,Setting.class);
                startActivity(intent);
                finish();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    //메인 페이지를 불러옴
    setContentView(R.layout.activity_main);
    mContext = getApplicationContext();
    dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);
    //AsyncTask를 통해 미리 불러옴
    uiTask = new UiTask();
    uiTask.execute();

    //뒤로가는 부분
    backPressCloseHandler = new BackPressCloseHandler(this);

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

        String token=FirebaseInstanceId.getInstance().getToken();/////Firebase에서 Token을 받아오는 부분
        ///내 서버에 토큰을 저장을 해야함
        System.out.println(token+" tokentoken");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token",token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        savetokenTask savetokenTask = new savetokenTask();
        savetokenTask.execute(jsonObject.toString());

        //기존의 친구들 정보를 가지고 와야함
        List<JSONObject> friendList = dBhelper.getAllfriend();
        System.out.println(friendList+"보낼친구");
        //친구의 새로운 정보를 받아오는 부분
        syncfriendTask syncfriendTask = new syncfriendTask();
        syncfriendTask.execute(friendList.toString());
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

    public class UiTask extends  AsyncTask<Void,Void,Void>{
        @Override
        protected void onPostExecute(Void aVoid) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_result())
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_meas())
                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frag_container_, new Fragment_main())
                    .commit();

            Intent intent =getIntent();
            int FCMrequest = intent.getIntExtra("FCM",1001);
            if(FCMrequest==0)
            {
                String who=intent.getStringExtra("WHO");
                String type=intent.getStringExtra("TYPE");

                Fragment_chat fragment_chat = new Fragment_chat();//프래그먼트와 액티비티가 통신
                Bundle bundle = new Bundle();//번들에
                bundle.putString("WHO", who);//누구한테 온건지
                bundle.putString("TYPE",type);//방의 종류 0-개인채팅 , 1-그룹채팅

                fragment_chat.setArguments(bundle);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frag_container_, fragment_chat)
                        .commit();

                System.out.println(who);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }
    }

    public class savetokenTask extends AsyncTask<String,String,String>
    {
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);
            if(s.equals("true"))
            {

            }else{

            }
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/fcmtokenrequest.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);
                //쿠키매니저에 저장되어있는 세션 쿠키를 사용하여 통신
                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                    con.setRequestProperty("Cookie", TextUtils.join(",",msCookieManager.getCookieStore().getCookies()));
                    System.out.println(msCookieManager.getCookieStore().getCookies()+"Request");
                }
                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

                writer.write(params[0]);

                writer.flush();
                writer.close();
                os.close();

                con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine())!=null)
                {
                    if(sb.length()>0)
                    {
                        sb.append("\n");
                    }
                    sb.append(line);
                }

                //결과를 보여주는 부분 서버에서 true or false
                result = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(con!=null)
                {
                    con.disconnect();
                }
            }

            return result;
        }
    }

    public class syncfriendTask extends AsyncTask<String,String,String>
    {
        DBhelper dbhelper = new DBhelper(MainActivity.this, "healthforyou.db", null, 1);
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s+"결과");
            System.out.println("서비스 실행");
            if(s.equals("false"))///친구의 정보중 바뀐게 없음
            {

            }else{
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    for(int i=0;i<jsonArray.length();i++)
                    {
                        JSONObject profile_data=new JSONObject(jsonArray.getString(i));
                        String Id=profile_data.optString("user_friend");

                        byte[] a = Base64.decode(profile_data.optString("user_profile"),Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(a,0,a.length);////비트맵으로 변환
                        new InternalImageManger(mContext).setFileName(Id+"_Image").setDirectoryName("PFImage").save(bitmap);//저장
                        //파일의 이름, update 된 날짜, 사용자 정보
                        dbhelper.updateProfile(Id+"_Image",profile_data.optString("user_update"),profile_data.optString("user_friend"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("업데이트 완료");
            dbhelper.close();

        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/syncfriendinfo.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);
                //쿠키매니저에 저장되어있는 세션 쿠키를 사용하여 통신
                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                    con.setRequestProperty("Cookie", TextUtils.join(",",msCookieManager.getCookieStore().getCookies()));
                    System.out.println(msCookieManager.getCookieStore().getCookies()+"Request");
                }
                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

                writer.write(params[0]);

                writer.flush();
                writer.close();
                os.close();

                con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine())!=null)
                {
                    if(sb.length()>0)
                    {
                        sb.append("\n");
                    }
                    sb.append(line);
                }

                //결과를 보여주는 부분 서버에서 true or false
                result = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(con!=null)
                {
                    con.disconnect();
                }
            }

            return result;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent Service = new Intent(MainActivity.this, ClientSocketService.class);///메인액티비티가 destroy되면
        stopService(Service);//서비스를 중지시키고 socket연결을 종료
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                toast.cancel();
                Intent t = new Intent(activity, MainActivity.class);
                activity.startActivity(t);
                activity.moveTaskToBack(true);
                activity.finish();

                System.exit(0);
            }
        }

        public void showGuide() {
            toast = Toast.makeText(activity, "한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}


