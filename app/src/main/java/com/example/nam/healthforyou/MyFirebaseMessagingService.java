package com.example.nam.healthforyou;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    DBhelper dBhelper = new DBhelper(this, "healthforyou.db", null, 1);
    final static int FCMintent = 0;
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
            String bodymessage;
            JSONObject messageJSON = null;
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
                        who = messageJSON.optString("from");
                    }else{//그룹간의 대화
                        room_type="1"; //찾아갈때 방번호로 찾아감
                        who = messageJSON.optString("room_no");
                    }
                    sendNotification(who,bodymessage,room_type);//누구에게,메세지,방의 종류
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
    private void sendNotification(String who,String messageBody,String type) {
        try {
            JSONObject fcmJSON = new JSONObject(messageBody);
            System.out.println(fcmJSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("FCM",FCMintent);//////MainActivity를 실행하라는 intent
        intent.putExtra("WHO",who);
        intent.putExtra("TYPE",type);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.custom_progressbar_drawable)///아이콘 바꾸는 거 생각
                .setContentTitle(who)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
