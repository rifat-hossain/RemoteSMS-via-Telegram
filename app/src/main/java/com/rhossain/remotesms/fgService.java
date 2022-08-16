package com.rhossain.remotesms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class fgService extends Service {
    public static boolean isServiceRunning;
    private String CHANNEL_ID = "FG_SERVICE_CHANNEL";

    public fgService() {
        isServiceRunning = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        isServiceRunning = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");

        registerReceiver(receiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Remote SMS service is running")
                .setContentText("Monitoring for new SMS")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.design_default_color_primary))
                .build();

        startForeground(1, notification);
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String appName = getString(R.string.app_name);
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, appName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;
        stopForeground(true);

        //Intent broadcastIntent = new Intent(this, SMSReciever.class);
        //sendBroadcast(broadcastIntent);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
        @Override
        public void onReceive(Context context, Intent intent) {
            db_viewmodel dbv = new db_viewmodel(context); //accessing "room database" with this viewmodel
            new Thread(new Runnable() {
                @Override
                public void run() {
                    db_settings dbs = dbv.load_settings();
                    if(dbs == null || dbs.backups.equals("")){ //this section checks whether settings was set and if there is any sim selected for backup
                        return;
                    }
                    if (intent.getAction().equals(SMS_RECEIVED)) {
                        Log.d("AJANAC", "onReceive: SMS Recieved");
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            Object[] pdus = (Object[])bundle.get("pdus"); //pdus holds all the data of an sms
                            final SmsMessage[] messages = new SmsMessage[pdus.length];
                            for (int i = 0; i < pdus.length; i++) {
                                messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                            }
                            String[] backs = dbs.backups.split("#"); //sim numbers are separated with "#"
                            boolean bb = true;
                            for(String back: backs){
                                int sm = capturedSIM(bundle);
                                Log.d("Sim Ids", "run: " + back + " captured: " + sm);
                                if(Integer.parseInt(back) == sm){
                                    bb = false; //if the sim numbers match make this flag false
                                    break;
                                }
                            }
                            if (!bb && messages.length > -1) {
                                StringBuilder body = new StringBuilder();
                                for(SmsMessage mgs : messages){
                                    body.append(mgs.getMessageBody());
                                }
                                db_viewmodel dbv = new db_viewmodel(context);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<db_receiver> receivers = dbv.getReceivers();
                                        TelegramBot bot = new TelegramBot(context.getString(R.string.bot_token));
                                        for(db_receiver receiver:receivers){
                                            SendMessage request = new SendMessage(receiver.chatId,"From:\n" + messages[0].getOriginatingAddress() + "\n\nBody:\n" + body.toString() +"\n\nDatetime:\n"+ new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z").format(new Date(messages[0].getTimestampMillis())))
                                                    .parseMode(ParseMode.HTML)
                                                    .disableWebPagePreview(true)
                                                    .disableNotification(true)
                                                    .replyToMessageId(1)
                                                    .replyMarkup(new ForceReply());
                                            // sync
                                            SendResponse sendResponse = bot.execute(request);
                                            boolean ok = sendResponse.isOk();
                                            Message message = sendResponse.message();
                                            if(!ok){
                                                SMS sms = new SMS(messages[0].getOriginatingAddress(), body.toString(),messages[0].getTimestampMillis());
                                                Queue qsms = new Queue();
                                                qsms.sender = sms.getSender();
                                                qsms.body = sms.getBody();
                                                qsms.dt = sms.getDatetime();
                                                dbv.insertsms(qsms);
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }
                    }
                }
            }).start();
        }
        int capturedSIM(Bundle bundle){
            String TAG = "SIM ID";
            int simid = -1;
            if(bundle.containsKey("subscription")){
                simid = bundle.getInt("subscription") - 1;
                Log.d(TAG, "capturedSIM: " + simid + " #subscription");
            }
            if(simid >= 0 && simid < 5){
                return simid;
            }
            else{
                if(bundle.containsKey("simId")){
                    simid = bundle.getInt("simId");
                    Log.d(TAG, "capturedSIM: " + simid + " #simid");
                }
                else if(bundle.containsKey("com.android.phone.extra.slot")){
                    simid = bundle.getInt("com.android.phone.extra.slot");
                    Log.d(TAG, "capturedSIM: " + simid + " #com.android.phone.extra.slot");
                }
                else if(bundle.containsKey("slot")){
                    simid = bundle.getInt("slot");
                    Log.d(TAG, "capturedSIM: " + simid + " #slolt");
                }
                else{
                    String keyName = "";
                    for(String key: bundle.keySet()){
                        if(key.contains("sim"))
                            keyName = key;
                    }
                    simid = bundle.getInt(keyName);
                    Log.d(TAG, "capturedSIM: " + simid + " #" + keyName);
                }
                return simid;
            }
        }
    };
}