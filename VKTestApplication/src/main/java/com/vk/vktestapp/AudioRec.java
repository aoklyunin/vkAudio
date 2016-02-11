package com.vk.vktestapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

/**
 * Created by Морковушка on 17.01.2016.
 */
public class AudioRec implements Serializable{

    public static final String saveFolder = "/storage/emulated/0/VkMusic/";
    public static final int DONT_LOAD = 0;
    public static final int MUST_LOAD = 1;
    public static final int IS_LOAD = 2;
    public static final String AUDIO_RECOMMEND = "Recommend";
    public static final String AUDIO_MY = "MyAudio";
    public static final String AUDIO_FIND = "AudioFind";
    public static final int columnSize = 12;
    public static final int MAX_WORD_LENGTH = 35;

    private static final long serialVersionUID = 1L;

    private int id;
    private int audio_id;       // id аудиозаписи
    private int owner_id; // id владельца
    private String artist; //  артист
    private String title; // заголовок
    private int duration; // длительность аудиозаписи
    private String url;   // путь
    private int genre_id; // номер жанра
    private int isLoaded; // загружена аудиозапись на устройство или нет
    private String type;  // рекомендованные, мои аудиозаписи или пустая аудиозапись
    private String savePath;
    private int tableRowID;
    private boolean flgLoadComplete;

    private MediaPlayer mediaPlayer;
    private SeekBar seek;
    private Activity activity;

    static void createFolder(String path){
        File folder = new File( path);
        boolean success = true;
        if (!folder.exists()) {
            Log.e("Folders", "Directory Does Not Exist, Create It");
            success = folder.mkdir();
        }
        if (success) {
            Log.e("Folders", "Directory Created: "+path);
        } else {
            Log.e("Folders", "Failed - Error: "+path);
        }
    }
    static void createFolders(){
        createFolder(saveFolder.substring(1,saveFolder.length()));
        createFolder(saveFolder+AUDIO_RECOMMEND);
        createFolder(saveFolder+AUDIO_MY);
        createFolder(saveFolder+AUDIO_FIND);
    }
    public String limitStr(String s){
        if(s.length()<=40)
            return s;
        else
            return s.substring(1,40)+"...";
    }
    class AudioDownloader extends AsyncTask<Void, Integer, Integer> {
        ProgressBar bar;
        TextView text;
        boolean flgService;
        ProgressDialog barProgressDialog;
        boolean flgPlay;
        int tryNum;

        AudioDownloader(boolean flgPlay, int tryNum){
            savePath = saveFolder+type+"/"+artist + "-" + title + ".mp3";
            savePath = savePath.replaceAll("'","");
            flgService  = false;
            this.flgPlay = flgPlay;
            DBHelper db = new DBHelper(activity);
            db.setAudioIsLoadedTR(tableRowID,savePath);
            TableRow tr = (TableRow) activity.findViewById(tableRowID);
            Button btn = (Button) tr.getChildAt(4);
            btn.setText("Загружено");
            btn.setTextColor(0xFF00FF00);
        }
        AudioDownloader(ProgressBar bar,TextView text,int tryNum){
            savePath = saveFolder+type+"/"+artist + "-" + title + ".mp3";
            savePath = savePath.replaceAll("'","");
            this.bar = bar;
            this.text = text;
            flgLoadComplete = false;
            flgService = true;
        }
        // сам фоновый процесс
        @Override
        protected Integer doInBackground(Void... params) {
            loadFile(savePath, url);
            return null;
        }

        // инициализация процесса
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(flgService){
                if(text!=null) {
                    // задаём текст полю
                    text.setText("Идёт загрузка..");
                    // делаем поле видимым
                    text.setVisibility(View.VISIBLE);
                    // делаем ProcessBar видимым
                    bar.setVisibility(View.VISIBLE);
                    // задаём максимум ProcessBar'у
                    bar.setMax(100);
                    bar.setProgress(0);
                }
            }else{
                barProgressDialog = new ProgressDialog(activity);
                barProgressDialog.setTitle("Загрузка аудиозаписи ...");
                barProgressDialog.setMessage(artist+" - "+title);
                barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                barProgressDialog.setProgress(0);
                barProgressDialog.setMax(100);
                barProgressDialog.show();
            }
        }

        // метод для загрузки файлов
        public void loadFile(String path, String urlPath) {
            try {
                // создаём url-адрес
                URL url = new URL(urlPath);
                // создаём url-соединение
                URLConnection conection = url.openConnection();
                // подключаемся
                conection.connect();
                // размер файла
                int len = conection.getContentLength();
                // создаём поток чтения
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                // создаём поток вывода
                OutputStream output = new FileOutputStream(path);
                // создаём буфер для чтения
                byte data[] = new byte[1024];
                // переменная для чтения из потока
                int count;
                // переменная для подсчёта процента скаченности файла
                int total = 0;
                // пока есть, что читать, читаем
                while ((count = input.read(data)) != -1) {
                    if (!url.equals("")) {
                        total += count;
                        if (len != 0) {
                            publishProgress((int) (total * 100 / len));
                        } else {
                            Log.e("DOWNLOAD", "размер файла 0");
                        }
                    }
                    output.write(data, 0, count);
                }
                // заполняем поток
                output.flush();
                // закрываем потоки
                output.close();
                input.close();
                // ловим ошибки
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
        }

        // здесь надо обрабатывать все команды для UI
        protected void onProgressUpdate(Integer... progress) {
            if (flgService){
                if (text!=null) {
                    bar.setProgress(progress[0]);
                    text.setText("Идёт загрузка: " + progress[0] + "%");
                }
            }else{
                barProgressDialog.setProgress(progress[0]);
            }
        }

        @Override
        // что нужно сделать в конце процесса
        protected void onPostExecute(Integer param) {
            isLoaded = IS_LOAD;
            if(flgService) {
                if (new File(savePath).length()>0) {
                    Log.e("Загрузка Аудио", "Загрузка завершена, попытка номер " + tryNum);
                    flgLoadComplete = true;
                }else{
                    if (tryNum<3)
                        new AudioDownloader(flgPlay,tryNum++).execute();
                    else
                        Log.e("Загрузка Аудио", "После трёх попыток не удалось загрузить аудио");
                }
            }else{
                barProgressDialog.dismiss();
                if (new File(savePath).length()>0) {
                    if (!flgPlay)
                        Toast.makeText(activity, "Загрузка завершена, попытка номер "+tryNum, Toast.LENGTH_SHORT).show();
                    else
                        play();
                    flgLoadComplete = true;
                }else{
                    if (tryNum<3)
                        new AudioDownloader(flgPlay,tryNum++).execute();
                    else
                        Toast.makeText(activity, "После трёх попыток не удалось загрузить аудио", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    public void downloadDialog(){
        if (isLoaded!=IS_LOAD) {
            // создаём диалог
            final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            // заголовок
            alert.setTitle("Загрузка");
            // текст
            alert.setMessage("Загрузить " + artist + " - " + title + "?");
            // кнопка остановки
            alert.setPositiveButton("Загрузить", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    loadDialog();
                }
            });
            alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            alert.show();
        }else{
            Toast.makeText(activity,"Уже загружено",Toast.LENGTH_SHORT).show();
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    void play() {
        // создаём диалог
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        // заголовок
        alert.setTitle("Проигрыватель");
        // текст
        alert.setMessage(artist+" - "+title);
        // линейная разметка
        LinearLayout linear=new LinearLayout(activity);
        linear.setOrientation(LinearLayout.VERTICAL);
        // ползунок
        seek=new SeekBar(activity);
        linear.addView(seek);
        // выводим на активность разметку
        alert.setView(linear);
        // музыкальный плеер
        mediaPlayer = MediaPlayer.create(activity, Uri.parse(savePath));
        mediaPlayer.start();
        // задаём максимум
        seek.setMax(mediaPlayer.getDuration());
        // кнопка остановки
        alert.setNegativeButton("Стоп", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mediaPlayer.stop();
            }
        });
        // обработка сворачивания диалога
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mediaPlayer.stop();
            }
        });
        // обработка передвижения ползунка
        seek.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mediaPlayer.isPlaying()) {
                    SeekBar sb = (SeekBar) v;
                    mediaPlayer.seekTo(sb.getProgress());
                }
                return false;
            }
        });
        startPlayProgressUpdater();
        alert.show();
    }
    // синхронизация ползунка с плеером
    public void startPlayProgressUpdater() {
        seek.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            seek.postDelayed(notification,1000);
        }else{
            mediaPlayer.pause();
        }
    }
    //тестовая запись
    AudioRec(int id, int audio_id,int owner_id, String artist,
             String title,int duration,String url,int genre_id,
             int isLoaded,String type,String savePath,int tableRowID){
        this.id = id;
        this.audio_id = audio_id;
        this.owner_id = owner_id;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.url = url;
        this.genre_id = genre_id;
        this.isLoaded = isLoaded;
        this.type = type;
        this.savePath = savePath;
        this.tableRowID = tableRowID;
    }
    AudioRec(){
        new AudioRec(0,0,0,"","",0,"",0,0,"emppty","",0);
    }
    AudioRec(Cursor cursor){
        this.id = cursor.getInt(0);
        this.audio_id = cursor.getInt(1);
        this.owner_id = cursor.getInt(2);
        this.artist = cursor.getString(3);
        this.title = cursor.getString(4);
        this.duration = cursor.getInt(5);
        this.url = cursor.getString(6);
        this.genre_id = cursor.getInt(7);
        this.isLoaded =  cursor.getInt(8);
        this.type = cursor.getString(9);
        this.savePath = cursor.getString(10);
        this.tableRowID = cursor.getInt(11);
    }
    public void setActivity(Activity activity){this.activity = activity;};
    public String getTitle(){return limitStr(title);}
    public String getArtist(){return limitStr(artist);}
    public int getID(){return id;}
    public void setType(String type){ this.type = type;}
    public String getUrl(){ return url;}
    public int getAudioId(){return audio_id;}
    public int getIsLoaded(){return isLoaded;}
    public String getType(){return type;}
    public boolean isLoadComplete(){return flgLoadComplete;}
    public void setTableRowID(int tableRoID){this.tableRowID = tableRowID;}
    public void setIsLoad(int isLoad){this.isLoaded = isLoad;}
    public ContentValues getDBValues(){
        ContentValues values = new ContentValues();
        values.put(DBHelper.KEY_AUDIO_ID, audio_id);
        values.put(DBHelper.KEY_OWNER_ID, owner_id);
        values.put(DBHelper.KEY_ARTIST, artist);
        values.put(DBHelper.KEY_TITLE, title);
        values.put(DBHelper.KEY_DURATION, duration);
        values.put(DBHelper.KEY_URL, url);
        values.put(DBHelper.KEY_GENRE_ID, genre_id);
        values.put(DBHelper.KEY_IS_LOADED, isLoaded);
        values.put(DBHelper.KEY_TYPE, type);
        values.put(DBHelper.KEY_PATH_TO_SAVE, savePath);
        values.put(DBHelper.KEY_TABLE_ROW_ID,tableRowID);
        return values;
    }
    public String [] getValues(){
        String [] arr = new String[columnSize];
        arr[0] = String.valueOf(id);
        arr[1] = String.valueOf(audio_id);
        arr[2] = String.valueOf(owner_id);
        arr[3] = artist;
        arr[4] = title;
        arr[5] = String.valueOf(duration);
        arr[6] = url;
        arr[7] = String.valueOf(genre_id);
        arr[8] = String.valueOf(isLoaded);
        arr[9] = type;
        arr[10] = savePath;
        arr[11] = String.valueOf(tableRowID);
        return arr;
    }
    String load(ProgressBar bar,TextView text){
        new AudioDownloader(bar,text,0).execute();
        return savePath;
    }
    String loadDialog(){
        new AudioDownloader(false,0).execute();
        return savePath;
    }
    String loadDialogAndPlay(){
        if (isLoaded!=IS_LOAD) {
            new AudioDownloader(true,0).execute();
        }else{
            play();
        }
        return savePath;
    }
    public int getTableRowID(){return tableRowID;}
}
