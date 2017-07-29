package com.example.nam.healthforyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.example.nam.healthforyou.Login.msCookieManager;

/**
 * Created by NAM on 2017-07-28.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);//인터넷의 변화를 띄워주는 부분
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
        int con = NetworkUtil.getConnectivityStatus(context);///네트워크의 상태를 받아옴
        if (con == NetworkUtil.TYPE_WIFI || con== NetworkUtil.TYPE_MOBILE) {///인터넷에 연결된 상태이면 WIFI ? LTE ?
            if( con == NetworkUtil.TYPE_WIFI)
            {
                Intent startIntent = new Intent(context, Syncdbservice.class);
                context.startService(startIntent);

            }else if(con == NetworkUtil.TYPE_MOBILE){
                //다이얼로그 - 연동하시겠습니까?
            }
        }else{//네트워크가 연결이 안된 경우

        }
    }
}
