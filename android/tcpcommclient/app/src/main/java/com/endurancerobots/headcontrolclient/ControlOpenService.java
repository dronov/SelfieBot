package com.endurancerobots.headcontrolclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.concurrent.TimeUnit;

public class ControlOpenService extends Service {
    NotificationManager nm;

    public ControlOpenService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
//  do not work
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        sendNotif();
//        return super.onStartCommand(intent, flags, startId);
//    }

    @Override
    public void onStart(Intent intent, int startId) {
        sendNotif();
        super.onStart(intent, startId);
    }

    private void sendNotif() {
        // 1-я часть
        Notification notif = new Notification(R.mipmap.ic_launcher, "Pull to control The Head",
                System.currentTimeMillis());

        // 3-я часть
        Intent intent = new Intent(this, TcpClient.class);
        intent.putExtra(TcpClient.FILE_NAME, "somefile");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // 2-я часть
        notif.setLatestEventInfo(this, "Head", "Push to control", pIntent);

        // ставим флаг, чтобы уведомление пропало после нажатия
        notif.flags |= Notification.FLAG_AUTO_CANCEL;

        // отправляем
        nm.notify(1, notif);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
