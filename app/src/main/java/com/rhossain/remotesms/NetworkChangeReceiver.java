package com.rhossain.remotesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Log.d("Network", "onReceive: NOT_CONNECTED");
            } else {
                db_viewmodel dbv = new db_viewmodel(context);
                List<Queue> queues = dbv.getQueues();
                for (Queue q: queues
                     ) {
                    SMS sms = new SMS(q.sender,q.body,q.dt);
                    TelegramBot bot = new TelegramBot(context.getString(R.string.bot_token));
                    List<db_receiver> receivers = dbv.getReceivers();
                    for(db_receiver receiver:receivers){
                        SendMessage request = new SendMessage(receiver.chatId,"From:\n" +sms.getSender()+"\n\nBody:\n" + sms.getBody() + "\n\nDatetime:\n" + new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z").format(new Date(sms.getDatetime())))
                                .parseMode(ParseMode.HTML)
                                .disableWebPagePreview(true)
                                .disableNotification(true)
                                .replyToMessageId(1)
                                .replyMarkup(new ForceReply());
                        SendResponse sendResponse = bot.execute(request);
                        boolean ok = sendResponse.isOk();
                        Message message = sendResponse.message();
                        if(ok){
                            dbv.delete(q);
                        }
                    }
                }
            }
        }
    }
}
