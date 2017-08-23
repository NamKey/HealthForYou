package com.example.nam.healthforyou;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.example.nam.healthforyou.Login.msCookieManager;

public class Setting extends AppCompatActivity {
    //SQLite
    DBhelper dBhelper;
    //Shared
    SharedPreferences session;
    SharedPreferences.Editor session_editor;

    SharedPreferences loginemail;
    SharedPreferences.Editor loginemail_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);///뒤로가기 버튼

        SharedPreferences useremail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
        String loginemailid=useremail.getString("useremail","false");
        //나에 대한 정보를 SQlite에서 갖고옴
        dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);
        JSONObject mejson = dBhelper.getFriend(loginemailid);

        //나에 대한 정보 세팅
        TextView tv_settingName = (TextView)findViewById(R.id.tv_settingName);
        tv_settingName.setText(mejson.optString("user_name"));
        TextView tv_settingEmail = (TextView)findViewById(R.id.tv_settingEmail);
        tv_settingEmail.setText(mejson.optString("user_friend"));

        Button btn_logout = (Button)findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AsyncTask를 통한 로그아웃 및 DB 초기화
                LogoutTask logoutTask = new LogoutTask();
                logoutTask.execute();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override//Setting에서는 로그아웃을 생각해 MainActivity를 finish 하므로 MainActivity를 다시 띄워줘야함
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    public class LogoutTask extends AsyncTask<Void,Void,Void>
    {
        HttpURLConnection con;
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //SQLITE DB에 있는 자료 삭제
            dBhelper.delete("delete from User_health;");//건강정보 삭제
            dBhelper.delete("delete from User_friend;");//친구목록 삭제
            dBhelper.delete("delete from ChatMessage;");//채팅 메세지 삭제
            dBhelper.delete("delete from GroupChat;");//그룹채팅방 정보 삭제

            //SharedPreference 삭제 - 모든 파일이 삭제됨
            session = getApplicationContext().getSharedPreferences("session",MODE_PRIVATE);//SharedPreference에 파일에 접근
            session_editor = session.edit();
            session_editor.remove("session");
            session_editor.apply();

            loginemail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
            loginemail_editor = loginemail.edit();
            loginemail_editor.remove("useremail");
            loginemail_editor.apply();

            //로그인 액티비티로 이동
            Intent intent = new Intent(Setting.this,Login.class);
            intent.putExtra("is_login",0);
            startActivity(intent);
            finish();
        }



        @Override
        protected Void doInBackground(Void... params) {
            String strUrl="http://kakapo12.vps.phps.kr/logoutcheck.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);

                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                    con.setRequestProperty("Cookie", TextUtils.join(",",msCookieManager.getCookieStore().getCookies()));
                    System.out.println(msCookieManager.getCookieStore().getCookies()+"Request");
                }

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

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
                //System.out.println(result);

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(con!=null)
                {
                    con.disconnect();
                }
            }
            return null;
        }
    }//Async End
}
