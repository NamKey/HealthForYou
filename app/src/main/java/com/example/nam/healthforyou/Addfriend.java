package com.example.nam.healthforyou;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.nam.healthforyou.Login.msCookieManager;

public class Addfriend extends AppCompatActivity {
    //통신부분
    HttpURLConnection con;
    EditText et_email;
    DBhelper dBhelper;
    List<JSONObject> friendlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        dBhelper = new DBhelper(getApplicationContext(), "healthforyou.db", null, 1);//DB 접근

        et_email = (EditText)findViewById(R.id.et_find_friendbyemail);
        Button btn_addconfirm = (Button)findViewById(R.id.btn_addconfirm);///친구추가를 요청할 경우
        btn_addconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendemail=et_email.getText().toString();///입력된 값을 받아와서
                JSONObject email = new JSONObject();
                try {
                    email.put("email",friendemail);//JSON에 넣어주고
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AddfriendTask addfriendTask = new AddfriendTask();//AsyncTask를 통해
                addfriendTask.execute(email.toString());
                //////친구를 추가하고 기존에 있는 친구인지 체크
            }
        });

        Button btn_cancel = (Button)findViewById(R.id.btn_cancel);////친구추가 취소
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//////채팅화면으로 감
            }
        });
    }

    public class AddfriendTask extends AsyncTask<String,String,String>
    {
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch(s){
                case "0":
                    Toast.makeText(Addfriend.this,"존재하지 않는 아이디입니다",Toast.LENGTH_SHORT).show();
                    break;

                case "1":
                    Toast.makeText(Addfriend.this,"이미 등록된 친구입니다",Toast.LENGTH_SHORT).show();
                    break;

                case "7979":
                    Toast.makeText(Addfriend.this,"나는 나의 친구입니다.",Toast.LENGTH_SHORT).show();
                    break;

                default:///////등록성공 메시지, SQLite에 등록록
                    Toast.makeText(Addfriend.this,"친구가 성공적으로 등록되었습니다",Toast.LENGTH_SHORT).show();

                    try {///친구목록을 로컬 DB에 저장
                        JSONObject frienddata = new JSONObject(s);
                        System.out.println(frienddata);
                        dBhelper.friendinsert(frienddata);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    finish();//추가페이지 종료
                    break;

            }/////결과를 받는 부분
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/addfriend.php";

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
                System.out.println(result);

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
}
