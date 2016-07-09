package com.lipl.ommcom.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.lipl.ommcom.R;
import com.lipl.ommcom.activity.HomeActivity;
import com.lipl.ommcom.activity.NewsDetailsActivity;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

public class MyAppService extends Service {
    public MyAppService() {
    }

    private Pubnub pubnub = null;
    private final String channel_name_breaking_news = "BREAKINGNEWS";
    private final String channel_name_news = "news";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Thread t = new Thread() {
            public void run() {
                subscribePubnub();
            };
        };
        t.start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void subscribePubnub() {
        pubnub = new Pubnub(Config.publish_key, Config.subscribe_key);
        try {
            pubnub.subscribe(channel_name_breaking_news, new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {

                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            //Looper.prepare();
                            System.out.println("MyApplication" + " SUBSCRIBE : " + channel + " : "
                                    + message.getClass() + " : " + message.toString());

                            if (message instanceof JSONObject) {
                                String status = "";
                                try {
                                    JSONObject jsonObject = (JSONObject) message;
                                    status = jsonObject.getString("status");
                                } catch (JSONException exception) {
                                    Log.e("MyApplication", "getStreamingMessage()", exception);
                                }

                                if (status != null && status.trim().length() > 0) {
                                    String text = status;
                                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    Intent intent = new Intent(MyAppService.this, HomeActivity.class);
                                    intent.putExtra("isFromNotification", true);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MyAppService.this, 1, intent, 0);
                                    Notification.Builder builder = new Notification.Builder(MyAppService.this);
                                    builder.setAutoCancel(true);
                                    builder.setTicker("Ommcom News");
                                    builder.setContentTitle("Breaking News");
                                    builder.setContentText(text);
                                    builder.setSmallIcon(R.mipmap.ic_launcher);
                                    builder.setContentIntent(pendingIntent);
                                    builder.setOngoing(true);
                                    //builder.setSubText(message);   //API level 16
                                    builder.build();

                                    Notification myNotication = builder.getNotification();
                                    manager.notify(11, myNotication);
                                }
                            }
                            //Looper.loop();
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println("SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );

            pubnub.subscribe(channel_name_news, new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {

                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            Looper.prepare();
                            System.out.println("MyApplication" + " SUBSCRIBE : " + channel + " : "
                                    + message.getClass() + " : " + message.toString());

                            if (message instanceof JSONObject) {
                                String status = "";
                                try {
                                    JSONObject jsonObject = (JSONObject) message;
                                    status = jsonObject.getString("status");
                                } catch (JSONException exception) {
                                    Log.e("MyApplication", "getStreamingMessage()", exception);
                                }

                                if (status.equalsIgnoreCase("")) {

                                } else {
                                    String news_slug = status;
                                    if (news_slug != null && news_slug.contains("*****#####*****")) {
                                        String[] nnn = news_slug.split("\\*\\*\\*\\*\\*#####\\*\\*\\*\\*\\*");
                                        String title = nnn[0];
                                        String slug = nnn[1];
                                        showNotificationForNews(title, slug);
                                    }
                                }
                            }
                            Looper.loop();
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println("SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        } catch (PubnubException e) {
            System.out.println(e.toString());
        }
    }

    private void showNotificationForNews(String text, String slug){
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(MyAppService.this, NewsDetailsActivity.class);
        intent.putExtra("slug", slug);
        intent.putExtra("isFromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyAppService.this, 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(MyAppService.this);
        builder.setAutoCancel(true);
        builder.setTicker("Ommcom News");
        builder.setContentTitle("Ommcom News");
        builder.setContentText(text);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        //builder.setSubText(message);   //API level 16
        builder.setNumber(100);
        builder.build();

        Notification myNotication = builder.getNotification();
        manager.notify(11, myNotication);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}