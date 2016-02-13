package com.vk.vktestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class StartActivity extends FragmentActivity {
    private CurVkClient cVk; // переменная для работы с ВК
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

    public void  btnFindAudio(View view){
        cVk.alertFindAudioChooseDialog();
    }
    // конструктор активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //vk = new CustomVkClient();
        super.onCreate(savedInstanceState);
        // ставим фокус на activity_start
        setContentView(R.layout.activity_start);
        // создаём объект ВК клиента
        cVk = new CurVkClient(this);
        AudioRec.createFolders();
    }
    // функция вывода сообщения
    private void showMessage(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
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
    // обработка ответа от VK API
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
            }
        };
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    // конпка отображения моих аудиозаписей
    public void btnDisplayMyAudio(View view){
        Intent intent = new Intent(StartActivity.this, AudioActivity.class);
        intent.putExtra("type", AudioRec.AUDIO_MY);
        startActivity(intent);
    }
    // кнопка отображения рекомедуемых аудиозаписей
    public void btnDisplayRecommend(View view){
        Intent intent = new Intent(StartActivity.this, AudioActivity.class);
        intent.putExtra("type", AudioRec.AUDIO_RECOMMEND);
        startActivity(intent);
    }
    // кнопка отображения БД
    public void btnDisplayDB(View view){
        Intent intent = new Intent(StartActivity.this, DBActivity.class);
        startActivity(intent);
    }
    // кнопка удаления БД
    public void btnDeleteDB(View view){
        DBHelper db = new DBHelper(StartActivity.this);
        db.deleteAll();
    }
    // кнопка остановки сервиса загрузки
    public void btnStartService(View view){
        startService(new Intent(this, DownloadAudioService.class));
    }
    // кнопка запуска сервиса загрузки
    public void btnStopService(View view){
        stopService(new Intent(this, DownloadAudioService.class));
    }
    // кнопка загрузки аудио
    public void btnAudioInDB(View view){
        cVk.alertAudioInDBDialog();
    }
}