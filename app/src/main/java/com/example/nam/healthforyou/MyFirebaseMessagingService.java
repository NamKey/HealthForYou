package com.example.nam.healthforyou;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;

import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    DBhelper dBhelper = new DBhelper(this, "healthforyou.db", null, 1);
    final static int FCMintent = 0;
    Context context = MyFirebaseMessagingService.this;

    ByteArrayOutputStream stream;
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        Map<String, String> data = remoteMessage.getData();

        //you can get your text message here.


        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            /* Check if data needs to be processed by long running job */
            String message= data.get("body");
            System.out.println(message+"text");
            String who;
            String name;
            String bodymessage;
            JSONObject messageJSON = null;
            Bitmap bitmap=null;
            try {
                messageJSON = new JSONObject(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(messageJSON!=null)
            {
                if(messageJSON.optString("command").equals("/makeroom"))//방만들어 달라는 메세지와 보내는 메세지를 분류
                {
                    dBhelper.makeRoominsert(messageJSON);//방의 정보를 저장 room_no,chatmember
                }else{

                    bodymessage = messageJSON.optString("message");
                    try {///건강 데이터인 경우
                        JSONObject healthJSON = new JSONObject(bodymessage);
                        bodymessage = "건강 정보";
                    } catch (JSONException e) {//건강데이터가 아니면 그냥 메세지
                        bodymessage = messageJSON.optString("message");
                    }
                    String room_type;
                    if(messageJSON.optString("command").equals("/to")||messageJSON.optString("command").equals("/tohealth"))//개인간의 대화
                    {
                        room_type="0"; //찾아갈때 ID로 방을 찾아감
                        who = messageJSON.optString("from");////상대방의 id
                        name = messageJSON.optString("name");////상대방의 이름
                        bitmap = new InternalImageManger(context).
                                setFileName(who+"_Image").
                                setDirectoryName("PFImage").
                                load();
                    }else{//그룹간의 대화
                        room_type="1"; //찾아갈때 방번호로 찾아감
                        who = messageJSON.optString("room_no");////상대방의 id
                        name = messageJSON.optString("name");////상대방의 이름
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.teamchat);
                    }

                    Bitmap myBitmap=null;
                    if(bitmap!=null)
                    {
                        myBitmap=getCircularBitmap(resizeBitmap(bitmap));
                    }

                    sendNotification(who,name,bodymessage,room_type,myBitmap);//누구에게,메세지,방의 종류
                    dBhelper.messagejsoninsert(messageJSON);
                }
            }else{//Message가 오지 않았는데???
                System.out.println("fcm??무슨 문제 있습니까???");
            }
        }

        // Notification은 foreground에서만 Custom 할 수 있으므로 Data message로 보내야됨
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {///데이터를 받으면 DB에 넣어줌??
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String who,String name,String messageBody,String type,Bitmap myBitmap) {
        String message="";
        //건강정보 처리부분
        try {
            JSONObject fcmJSON = new JSONObject(messageBody);
            message="건강정보";
        } catch (JSONException e) {
            message=messageBody;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("FCM",FCMintent);//////MainActivity를 실행하라는 intent
        intent.putExtra("WHO",who);
        intent.putExtra("TYPE",type);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //TODO Custom NOtification
        //RemoteView
        /*final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.remoteview_notification);
        // build notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.cardiogram2)
                        .setContentTitle(who)
                        .setContentText(messageBody)
                        .setContent(rv)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        final Notification notification = mBuilder.build();

        Bitmap bitmap = new InternalImageManger(context).
                setFileName(who+"_Image").
                setDirectoryName("PFImage").
                load();
        Bitmap myBitmap=null;
        if(bitmap!=null)
        {
            myBitmap=getCircularBitmap(resizeBitmap(bitmap));
        }

        rv.setImageViewBitmap(R.id.remoteview_notification_icon,myBitmap);
        //rv.setImageViewResource(R.id.remoteview_notification_icon, R.drawable.cardiogram2);
        rv.setTextViewText(R.id.remoteview_notification_headline, who);
        rv.setTextViewText(R.id.remoteview_notification_short_message, messageBody);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification);*/

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.cardiogram2)
                .setLargeIcon(myBitmap)
                .setContentTitle(name)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    public static Bitmap getCircularBitmap(@NonNull Bitmap bitmap)
    {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    static public Bitmap resizeBitmap(Bitmap original) {

        int resizeWidth = 100;

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        int targetHeight = (int) (resizeWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, targetHeight, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }
}