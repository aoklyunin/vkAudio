package com.vk.vktestapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class DownloadAudioService extends Service {
    Timer mTimer;
    MyTimerTask mMyTimerTask;
    int cnt;
    int loadCnt;
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            DBHelper db = new DBHelper(getApplicationContext());
            cnt = db.getCountMustLoad();
            if (cnt==0){
                Log.e("SERVICE", "Все аудио загружены");
                stopSelf();
                cancel();
            }else{
                db.loadFirstAudio(null, null);
                Log.e("SERVICE", "Осталось загрузить: "+cnt);
            }
        }
    }

    public DownloadAudioService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
         mTimer.cancel();
         Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        if (new DBHelper(this).getCountMustLoad()>0){
            mTimer = new Timer();
            mMyTimerTask = new MyTimerTask();
            mTimer.schedule(mMyTimerTask, 500, 10000);
            loadCnt = 0;
        }
    }
}