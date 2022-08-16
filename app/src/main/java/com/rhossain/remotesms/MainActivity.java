package com.rhossain.remotesms;

import static android.Manifest.permission.READ_PHONE_STATE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    SubscriptionManager localSubscriptionManager;
    CheckBox[] boxes;
    String very = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //syncSettings
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED){
            findViewById(R.id.ll_perm_sms).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_sync).setVisibility(View.INVISIBLE);
        }
        if(ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
            findViewById(R.id.ll_perm_sim).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_save).setEnabled(false);
        }
        else {
            work();
        }

        //AddAccount
        TextView vtxt = findViewById(R.id.vtxt);
        very = getIPAddress(true) + "\n" + randString(7);
        vtxt.setText(very);
    }

    public void hit_ser(View view) {
        if(!isServiceRunning("fgService")) {
            Intent intent = new Intent(this, fgService.class);
            startForegroundService(intent);
        }
    }
    private boolean isServiceRunning(String serviceName){
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = i
                    .next();

            if(runningServiceInfo.service.getClassName().equals(serviceName)){
                serviceRunning = true;

                if(runningServiceInfo.foreground)
                {
                    //service run in foreground
                }
            }
        }
        return serviceRunning;
    }

    //syncSettings
    public void work(){
        localSubscriptionManager = SubscriptionManager.from(this);
        int sims = localSubscriptionManager.getActiveSubscriptionInfoCount();
        Log.d("AJANAC", "sims: " + sims);
        LinearLayout ll = findViewById(R.id.ll_sims);
        new Thread(new Runnable() {
            @Override
            public void run() {
                db_viewmodel dbv = new db_viewmodel(MainActivity.this);
                db_settings settings = dbv.load_settings();
                String backsims = "";
                if (settings != null) {
                    //spi_sims.setSelection(settings.sendsim);
                    backsims = settings.backups;
                }
                boxes = new CheckBox[sims];
                for (int i = 0; i < sims; i++) {
                    boxes[i] = new CheckBox(MainActivity.this);
                    boxes[i].setText("SIM " + (i + 1));
                    if (!backsims.equals("")) {
                        String[] backs = backsims.split("#");
                        for (String back : backs) {
                            if (Integer.parseInt(back) == i) {
                                boxes[i].setChecked(true);
                                break;
                            }
                        }
                    }
                    int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ll.addView(boxes[finalI]);
                        }
                    });
                }
            }
        }).start();
    }

    public void hit_save(View view) {
        StringBuilder backsims = new StringBuilder();
        for(int i=0;i<boxes.length;i++){
            if(boxes[i].isChecked()){
                if(!backsims.toString().equals("")){
                    backsims.append("#");
                }
                backsims.append(i);
            }
        }
        db_viewmodel dbv = new db_viewmodel(this);
        db_settings dbs = new db_settings();
        dbs.slot = 1;
        dbs.backups = backsims.toString();
        dbv.insert_settings(dbs);
    }

    public void hit_perm_sms(View view) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS},0);
        }
        else{
            findViewById(R.id.ll_perm_sms).setVisibility(View.GONE);
            findViewById(R.id.ll_sync).setVisibility(View.VISIBLE);
        }
    }

    public void hit_perm_sim(View view) {
        if(ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{READ_PHONE_STATE},10);
        }
        else{
            findViewById(R.id.ll_perm_sim).setVisibility(View.GONE);
            findViewById(R.id.btn_save).setEnabled(true);

            work();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.ll_perm_sms).setVisibility(View.GONE);
            findViewById(R.id.ll_sync).setVisibility(View.VISIBLE);
        }
        else if(requestCode == 10 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            findViewById(R.id.ll_perm_sim).setVisibility(View.GONE);
            findViewById(R.id.btn_save).setEnabled(true);

            work();
        }
    }

    //AddAccount
    static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
    static String randString(int length){
        int leftLimit = 48; // letter 'a'
        int rightLimit = 83; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            if(randomLimitedInt > 57){
                randomLimitedInt += 39;
            }
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    public void hit_sent(View view) {
        ((Button)view).setText("Working!!");
        TelegramBot bot = new TelegramBot(this.getString(R.string.bot_token));
        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
        bot.execute(getUpdates, new Callback<GetUpdates, GetUpdatesResponse>() {
            @Override
            public void onResponse(GetUpdates request, GetUpdatesResponse response) {
                List<Update> updates = response.updates();
                boolean verified = false;
                for(Update update:updates){
                    Log.d("CHAT_ID", "onResponse: " + update.message().text());
                    if(update.message().text().equals(very)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"Verified",Toast.LENGTH_LONG).show();
                            }
                        });
                        db_viewmodel dbv = new db_viewmodel(MainActivity.this);
                        db_receiver receiver = new db_receiver();
                        receiver.chatId = update.message().chat().id();
                        dbv.insertReceiver(receiver);
                        verified = true;
                        SendMessage req = new SendMessage(receiver.chatId,"Congratulations!\nYour Telegram account is now verified as a SMS receiver account of your target device. From now on, all the incoming SMSs will be forwarded here to you. ;)")
                                .parseMode(ParseMode.HTML)
                                .disableWebPagePreview(true)
                                .disableNotification(true)
                                .replyToMessageId(1)
                                .replyMarkup(new ForceReply());
                        // sync
                        SendResponse sendResponse = bot.execute(req);
                        boolean ok = sendResponse.isOk();
                        Message message = sendResponse.message();
                    }
                }
                if(!verified){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"Could not verify",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                ((Button)view).setText("Verify!");
            }

            @Override
            public void onFailure(GetUpdates request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"Could not verify",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}