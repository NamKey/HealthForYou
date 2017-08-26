package com.example.nam.healthforyou;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.nam.healthforyou.Login.msCookieManager;

public class Setting extends AppCompatActivity implements View.OnClickListener{
    Context mContext;
    //SQLite
    DBhelper dBhelper;
    //Shared
    SharedPreferences session;
    SharedPreferences.Editor session_editor;

    SharedPreferences loginemail;
    SharedPreferences.Editor loginemail_editor;

    private Uri mImageCaptureUri;
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_CAMERA = 2;

    ImageView iv_myprofileImage;
    ByteArrayOutputStream stream;
    String myId;

    //통신부분
    HttpURLConnection con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);///뒤로가기 버튼
        mContext = getApplicationContext();
        stream = new ByteArrayOutputStream();
        SharedPreferences useremail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
        String loginemailid=useremail.getString("useremail","false");
        //나에 대한 정보를 SQlite에서 갖고옴
        dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);
        JSONObject mejson = dBhelper.getFriend(loginemailid);
        myId=mejson.optString("user_friend");
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

        Bitmap bitmap = new InternalImageManger(mContext).
                setFileName(myId).
                setDirectoryName("PFImage").
                load();

        //나의 프로필 이미지
        iv_myprofileImage = (ImageView)findViewById(R.id.iv_settingProfile);
        iv_myprofileImage.setImageBitmap(bitmap);
        iv_myprofileImage.setOnClickListener(this);
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

    private class profileSaveTask extends AsyncTask<String,String,String>
    {
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/uploadProfile.php";

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

    private void doTakePhotoAction()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    /**
     * 앨범에서 이미지 가져오기
     */
    private void doTakeAlbumAction()
    {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != RESULT_OK)
        {
            return;
        }

        switch(requestCode)
        {
            case CROP_FROM_CAMERA:
            {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
                // 임시 파일을 삭제합니다.
                final Bundle extras = data.getExtras();

                if(extras != null)
                {
                    Bitmap photo = extras.getParcelable("data");
                    try{
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, stream);//이미지를 JPEG 형식으로 압축
                    }catch(NullPointerException e)
                    {
                        Toast.makeText(mContext,"프로필 사진업로드 실패",Toast.LENGTH_SHORT).show();
                    }

                    byte[] byteArray = stream.toByteArray();//스트림을 통해 bytearray로 만들고
                    Glide.with(this).load(byteArray).into(iv_myprofileImage);//Glide를 통해 이미지뷰에 올림

                    /////나의 프로필에 대한 이미지를 InternalStorage에 저장
                    new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").save(photo);
                    photo.compress(Bitmap.CompressFormat.JPEG, 90, stream);//이미지를 stream으로 옮김 - TODO 테스트 필요 두번 압축하기 때문에
                    byte[] byteArrayForupload = stream.toByteArray();//스트림을 통해 bytearray로 만들고
                    String base64Image = Base64.encodeToString(byteArrayForupload,Base64.DEFAULT);

                    ///////byteArray로 보내는 이유는 Bitmap은 이미 메모리에 올라가 있으므로 Array로 바꿀시 접근이 더 용이
                    //////file로 보낼 시 file로 생성 후 보내야 되는 시간 필요

                    //업데이트 된 내용에 대한 upload 요청
                    JSONObject uploadprofile = new JSONObject();

                    long now = System.currentTimeMillis();
                    // 현재시간을 date 변수에 저장한다.
                    Date date = new Date(now);
                    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // nowDate 변수에 값을 저장한다.
                    final String formatDate = sdfNow.format(date);

                    profileSaveTask profileSaveTask = new profileSaveTask();///네트워크 부분 AsyncTask 로 기록 후 측정내역으로 넘어감

                    try {
                        uploadprofile.put("profile",base64Image);
                        uploadprofile.put("update",formatDate);
                        System.out.println(uploadprofile);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /////나의 프로필을 서버에 업로드 - MariaDB - HTTP -PHP -MariaDB
                    profileSaveTask.execute(uploadprofile.toString());

                    /////나의 프로필이 바뀐 날짜, 이미지의 경로를 DB에 저장 - SQlite
                    dBhelper.updateProfile(myId+"_Image",formatDate,myId);
                }

                // 임시 파일 삭제
                if(mImageCaptureUri.getPath()!=null)
                {
                    File f = new File(mImageCaptureUri.getPath());
                    if(f.exists())
                    {
                        f.delete();
                    }
                }


                break;
            }

            case PICK_FROM_ALBUM:
            {
                mImageCaptureUri = data.getData();
            }

            case PICK_FROM_CAMERA:
            {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image");
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_CAMERA);

                break;
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                doTakePhotoAction();
            }
        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                doTakeAlbumAction();
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setNeutralButton("카메라", cameraListener)
                .setNegativeButton("앨범", albumListener)
                .setPositiveButton("취소", cancelListener)
                .show();
    }
}
