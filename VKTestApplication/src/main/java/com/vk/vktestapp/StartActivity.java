package com.vk.vktestapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class StartActivity extends FragmentActivity {
    private TextView tv; // поле для дебага
    private ListView lv; // список аудиозаписей
    public ProgressBar firstBar; // ProcessBar
    public TextView barText; // индикация для ProcessBar'a
    public CurVkClient cVk; // переменная для работы с ВК

    // кнопка входа
    public void btnSignIn(View view){
        cVk.setFirstLogin(true);
        cVk.signInVk();
    }
    // кнопка выхода
    public void btnSignOut(View view) {
        cVk.logout();
        showMessage("вышли из ВК");
    }
    // конструктор активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //vk = new CustomVkClient();
        super.onCreate(savedInstanceState);
        // ставим фокус на activity_start
        setContentView(R.layout.activity_start);
        // привязываем текстовое поле
        tv = (TextView) this.findViewById(R.id.textView1);
        // делаем ему прокрутку
        tv.setMovementMethod(new ScrollingMovementMethod());
        // привязваем ProgressBar
        firstBar = (ProgressBar)findViewById(R.id.firstBar);
        // привязываем индикатор загрузки
        barText = (TextView)findViewById(R.id.barText);
        // привязываем лист
        lv = (ListView)findViewById(R.id.listView1);
        // создаём объект ВК клиента
        cVk = new CurVkClient(this);
    }
    // функция вывода сообщения
    private void showMessage(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    // кнопка загрузки аудио
    public void btnAudioInDB(View view){
        DBHelper db = new DBHelper(this);
        cVk.alertFillDBDialog();
        Toast.makeText(this,"Аудиозаписи загружены",Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        cVk.setIsResumed(true);
        cVk.signInVk();
    }
    @Override
    protected void onPause() {
        cVk.setIsResumed(false);
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                showMessage("авторизация прошла успешно");
            }
            @Override
            public void onError(VKError error) {
                cVk.getVkError(error);
                // User didn't pass Authorization
            }
        };
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void btnDisplayMyAudio(View view){
        Intent intent = new Intent(StartActivity.this, AudioActivity.class);
        //AudioRecWrapper wrapper = new AudioRecWrapper(cVk.getAudioMap());
        intent.putExtra("type", AudioRec.AUDIO_MY);
        startActivity(intent);

    }
    public void btnRecommend(View view){
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                cVk.getRecommendAudio(15);
                while(!cVk.getFlgGetRecommend()){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                StartActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(StartActivity.this, DBActivity.class);
                        //AudioRecWrapper wrapper = new AudioRecWrapper(cVk.getAudioMap());
                        //intent.putExtra("obj",wrapper );
                        startActivity(intent);
                    }
                });
            }
        }).start();
    }

    public void btnLoadAudio(View view){

    }

    public void btnFillDB(View view){

    }
    public void btnDisplayDB(View view){
        Log.d("1", "Reading all contacts..");
        DBHelper db = new DBHelper(this);
        List<AudioRec> audios = db.getAllAudioRecs();
        for (AudioRec audio : audios) {
            String log = "Id: "+audio.getID()+" ,Artist: " + audio.getArtist() + " ,Title: " + audio.getTitle();
            Log.d("1",log);
        }
    }
    public void btnDeleteDB(View view){
        DBHelper db = new DBHelper(this);
        db.deleteAll();
        Toast.makeText(this,"Аудиозаписи удалены",Toast.LENGTH_SHORT).show();
    }

    public void btnLoadFirstAudioDB(View v){
        DBHelper db = new DBHelper(this);
        db.loadFirstAudio((ProgressBar) findViewById(R.id.firstBar),
                          (TextView)    findViewById(R.id.barText));
    }
    public void btnStartService(View view){
        startService(new Intent(this, DownloadAudioService.class));
    }
    public void btnStopService(View view){
        stopService(new Intent(this, DownloadAudioService.class));
    }

}