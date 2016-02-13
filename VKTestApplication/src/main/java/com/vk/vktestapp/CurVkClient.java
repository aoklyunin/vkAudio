package com.vk.vktestapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;


public class CurVkClient {
    private static final String[] sMyScope = new String[]{
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.PHOTOS,
            VKScope.NOHTTPS,
            VKScope.MESSAGES,
            VKScope.DOCS,
            VKScope.AUDIO,
            VKScope.ADS
    };
    private boolean flgFirstLogin;
    private boolean isResumed = false;
    private Activity mainActivity;
    public void setFirstLogin(boolean flg){flgFirstLogin=flg;}
    public void setIsResumed(boolean flg){isResumed=flg;}


    void findAudios(int cnt,String query){

        DBHelper db = new DBHelper(mainActivity);
        db.setStringConf(DBHelper.KEY_AUDIO_FIND_CONF,query);
        db.deleteAudioByType(AudioRec.AUDIO_FIND);
        // запрос на мои аудиозаписи
        VKParameters params = new VKParameters();
        params.put(VKApiConst.COUNT, cnt);
        params.put(VKApiConst.Q,query);
        params.put(VKApiConst.SORT,2);
        VKRequest request = VKApi.audio().search(params);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                new AudioResponseProcessor(response,AudioRec.AUDIO_FIND).execute();
            }

            @Override
            public void onError(VKError error) {
                getVkError(error);
                super.onError(error);
            }
        });

    }

    void alertFindAudioDialog(){
        // подготавливаем диалог диалог
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        // получаем описание диалога
        final View dialogView = inflater.inflate(R.layout.dialog_find_audio, null);
        dialogBuilder.setView(dialogView);
        // получаем вьюхи элементов управления
        final EditText findCount = (EditText) dialogView.findViewById(R.id.edit1);
        final EditText findText   = (EditText) dialogView.findViewById(R.id.edit2);

        findCount.setText("100");
        // задаём заголовок диалога
        dialogBuilder.setTitle("Поиск");
        // задаём текст диалога
        dialogBuilder.setMessage("Введите параметры поиска");
        // кнопка положительного ответа
        dialogBuilder.setPositiveButton("Поиск", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                findAudios(Integer.parseInt(findCount.getText().toString()),
                        findText.getText().toString());
            }
        });
        // кнопка отрицательного ответа
        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        // создаём диалог и показываем его
        AlertDialog b = dialogBuilder.create();
        b.show();

    }

    void alertFindAudioChooseDialog(){
        // подготавливаем диалог диалог
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        // задаём заголовок диалога
        dialogBuilder.setTitle("Поиск");
        // задаём текст диалога
        DBHelper db = new DBHelper(mainActivity);
        dialogBuilder.setMessage("Последний запрос был: "+db.getStringConf(DBHelper.KEY_AUDIO_FIND_CONF)+". Что сделать?");
        // кнопка положительного ответа
        dialogBuilder.setPositiveButton("Последний поиск", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent intent = new Intent(mainActivity, AudioActivity.class);
                intent.putExtra("type", AudioRec.AUDIO_FIND);
                mainActivity.startActivity(intent);
            }
        });
        // кнопка отрицательного ответа
        dialogBuilder.setNegativeButton("Новый поиск", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                alertFindAudioDialog();
            }
        });
        // создаём диалог и показываем его
        AlertDialog b = dialogBuilder.create();
        b.show();

    }

    // загрузка аудио
    public void loadAudio(int audioMyCnt,int audioRecCnt){
        if (audioMyCnt!=0 && audioRecCnt!=0) {
            // запрос на мои аудиозаписи
            VKParameters params = new VKParameters();
            params.put(VKApiConst.COUNT, audioMyCnt);
            VKRequest requestAudioMy = VKApi.audio().get(params);
            // запрос на рекомендованные аудиозаписи
            params = new VKParameters();
            params.put(VKApiConst.COUNT, audioRecCnt);
            VKRequest requestAudioRec = VKApi.audio().getRecommendations(params);
            VKBatchRequest batch = new VKBatchRequest(requestAudioMy, requestAudioRec);
            batch.executeWithListener(new VKBatchRequest.VKBatchRequestListener() {
                @Override
                public void onComplete(VKResponse[] responses) {
                    new AudioResponseProcessor(responses).execute();
                }

                @Override
                public void onError(VKError error) {
                    getVkError(error);
                    super.onError(error);
                }
            });
        }else{
            VKParameters params = new VKParameters();
            VKRequest request;
            final String type;
            if(audioMyCnt>0) {
                params.put(VKApiConst.COUNT, audioMyCnt);
                request = VKApi.audio().get(params);
                type = AudioRec.AUDIO_MY;
            }else{
                params.put(VKApiConst.COUNT, audioRecCnt);
                request = VKApi.audio().getRecommendations(params);
                type = AudioRec.AUDIO_RECOMMEND;
            }
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    new AudioResponseProcessor(response,type).execute();
                }

                @Override
                public void onError(VKError error) {
                    getVkError(error);
                    super.onError(error);
                }
            });
        }
    }

    class AudioResponseProcessor extends AsyncTask<Void, Integer, Integer> {
        VKResponse[] responses;
        VKResponse response;
        String type;
        DBHelper db;
        int sizeMy;
        int sizeRec;
        int size;
        ProgressDialog barProgressDialog;
        boolean flgResponseArr;

        AudioResponseProcessor(VKResponse[] responses){
            this.responses = responses;
            db = new DBHelper(mainActivity);
            sizeMy =  ((VKList<VKApiAudio>) responses[0].parsedModel).size();
            sizeRec = ((VKList<VKApiAudio>) responses[1].parsedModel).size();
            flgResponseArr = true;
        }
        AudioResponseProcessor(VKResponse response,String type){
            this.response = response;
            this.type = type;
            db = new DBHelper(mainActivity);
            size = ((VKList<VKApiAudio>) response.parsedModel).size();
            flgResponseArr = false;
        }
        // сам фоновый процесс
        @Override
        protected Integer doInBackground(Void... params) {
            if (flgResponseArr) {
                for (int i = 0; i < sizeMy; i++) {
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>) responses[0].parsedModel).get(i);
                    AudioRec audio = new AudioRec(0, vkApiAudio.id, vkApiAudio.owner_id,
                            vkApiAudio.title, vkApiAudio.artist, vkApiAudio.duration,
                            vkApiAudio.url, vkApiAudio.genre, AudioRec.MUST_LOAD, AudioRec.AUDIO_MY, "", 0);
                    db.addAudioRec(audio);
                    publishProgress(i);
                }
                for (int i = 0; i < sizeRec; i++) {
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>) responses[1].parsedModel).get(i);
                    AudioRec audio = new AudioRec(0, vkApiAudio.id, vkApiAudio.owner_id,
                            vkApiAudio.title, vkApiAudio.artist, vkApiAudio.duration,
                            vkApiAudio.url, vkApiAudio.genre, AudioRec.DONT_LOAD, AudioRec.AUDIO_RECOMMEND, "", 0);
                    db.addAudioRec(audio);
                    publishProgress(i + sizeMy);
                }
            }else{
                for (int i = 0; i < size; i++) {
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>) response.parsedModel).get(i);
                    AudioRec audio = new AudioRec(0, vkApiAudio.id, vkApiAudio.owner_id,
                            vkApiAudio.title, vkApiAudio.artist, vkApiAudio.duration,
                            vkApiAudio.url, vkApiAudio.genre, 0, type, "", 0);
                    if (type==AudioRec.AUDIO_MY)
                        audio.setIsLoad(AudioRec.MUST_LOAD);
                    else
                        audio.setIsLoad(AudioRec.DONT_LOAD);
                    db.addAudioRec(audio);
                    publishProgress(i);
                }
            }
            return 0;
        }

        // инициализация процесса
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            barProgressDialog = new ProgressDialog(mainActivity);
            barProgressDialog.setTitle("Добавление аудиозаписей в БД...");
            barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
            barProgressDialog.setProgress(0);
            if (flgResponseArr)
                barProgressDialog.setMax(sizeMy + sizeRec);
            else
                barProgressDialog.setMax(size);
            barProgressDialog.show();
        }
        // здесь надо обрабатывать все команды для UI
        protected void onProgressUpdate(Integer... progress) {
            barProgressDialog.setProgress(progress[0]);
        }

        @Override
        // что нужно сделать в конце процесса
        protected void onPostExecute(Integer param) {
            barProgressDialog.dismiss();
            Toast.makeText(mainActivity,"Загрузка завершена",Toast.LENGTH_SHORT).show();
            if (type==AudioRec.AUDIO_FIND){
                Intent intent = new Intent(mainActivity,AudioActivity.class);
                intent.putExtra("type",AudioRec.AUDIO_FIND);
                mainActivity.startActivity(intent);
            }
        }
    }

    // конструктор
    CurVkClient(Context c){
        mainActivity = (Activity)c;
        flgFirstLogin = true;
        // запускаем API Вконтакте
        VKSdk.wakeUpSession(c, new VKCallback<VKSdk.LoginState>() {
            @Override
            // функция-обработчик ответов логина ВК
            public void onResult(VKSdk.LoginState res) {
                if (isResumed) {
                    signInVk();
                }
            }

            // функция-обработчик ошибок
            @Override
            public void onError(VKError error) {
                getVkError(error);
            }
        });
    }

    // функция выхода из ВК
    public void logout(){VKSdk.logout();}
    // обработка ошибок VK
    public void getVkError(VKError error) {
        switch (error.errorCode) {
            case -102:
                Toast.makeText(mainActivity, "Ошибка авторизации: " + error.toString(), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(mainActivity, error.errorMessage, Toast.LENGTH_SHORT).show();
                break;
        }
    }
    // функция входа в ВК
    public void signInVk() {
        if(flgFirstLogin) {
            if (VKSdk.isLoggedIn()) {
                Toast.makeText(mainActivity, "вы уже авторизированы", Toast.LENGTH_SHORT).show();
            } else {
                VKSdk.login(mainActivity, sMyScope);
            }
            flgFirstLogin = false;
        }
    }
    // Дилог добавления аудиозаписей в БД
    public void alertAudioInDBDialog(){
        VKRequest requestAudioMy  = VKApi.audio().get();
        VKRequest requestAudioRec = VKApi.audio().getRecommendations();
        VKBatchRequest batch = new VKBatchRequest(requestAudioMy, requestAudioRec);
        batch.executeWithListener(new VKBatchRequest.VKBatchRequestListener() {
            @Override
            public void onComplete(VKResponse[] responses) {
                super.onComplete(responses);
                processAudioCnt(responses);
            }

            @Override
            public void onError(VKError error) {
                getVkError(error);
                super.onError(error);
            }
        });
    }
    // обрабатываем кол-во аудио на загрузку
    public boolean processAudioCnt(VKResponse[] responses){
        try {
            JSONObject j = responses[0].json.getJSONObject("response");
            int cntAudioMy = j.getInt("count");
            j = responses[1].json.getJSONObject("response");
            int cntAudioRec  = j.getInt("count");
            showLoadAudioDialog(cntAudioMy,cntAudioRec);
            Log.e("AUDIO_CNT","Доступно " + cntAudioMy  + " моих аудиозаписей");
            Log.e("AUDIO_CNT","Доступно " + cntAudioRec + " рекомендованных аудиозаписей");
            return true;
        } catch (JSONException e) {
            Log.e("AUDIO_CNT", "Ошибка получения кол-ва аудиозаписей:" + e.getMessage());
            return false;
        }
    }
    // диалог выбора загрузки
    public void showLoadAudioDialog(final int cntAduioMy, final int cntAudioRec) {
        // подготавливаем диалог диалог
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        // получаем описание диалога
        final View dialogView = inflater.inflate(R.layout.audio_in_bd_dialog, null);
        dialogBuilder.setView(dialogView);
        // получаем вьюхи элементов управления
        final EditText edtAudioMy    = (EditText) dialogView.findViewById(R.id.edit1);
        final EditText edtAudioRec   = (EditText) dialogView.findViewById(R.id.edit2);
        final CheckBox checkAudioMy  = (CheckBox) dialogView.findViewById(R.id.checkBox1);
        final CheckBox checkAudioRec = (CheckBox) dialogView.findViewById(R.id.checkBox2);
        // вешаем обработчики нажатия на checkBox
        checkAudioMy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtAudioMy.setEnabled(true);
                } else {
                    edtAudioMy.setEnabled(false);
                }
            }
        });
        checkAudioRec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtAudioRec.setEnabled(true);
                } else {
                    edtAudioRec.setEnabled(false);
                }
            }
        });
        // задаём начальные значения
        edtAudioRec.setText(cntAudioRec + "");
        edtAudioMy.setText(cntAduioMy + "");
        // задаём заголовок диалога
        dialogBuilder.setTitle("Добавление в БД");
        // задаём текст диалога
        dialogBuilder.setMessage("Всего:\n   " + cntAduioMy + " моих аудиозаписей\n   " + cntAudioRec + " рекомендованных");
        // кнопка положительного ответа
        dialogBuilder.setPositiveButton("Загрузить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int cntMy = 0;
                int cntRec = 0;
                if (checkAudioMy.isChecked())  cntMy = Integer.parseInt(edtAudioMy.getText().toString());
                if (checkAudioRec.isChecked()) cntRec = Integer.parseInt(edtAudioRec.getText().toString());
                if ( cntMy!=0||cntRec!=0)
                    loadAudio(cntMy,cntRec);
                else
                    Toast.makeText(mainActivity,"Не выбрано ни одной аудиозаписи",Toast.LENGTH_SHORT).show();
            }
        });
        // кнопка отрицательного ответа
        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        // создаём диалог и показываем его
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
