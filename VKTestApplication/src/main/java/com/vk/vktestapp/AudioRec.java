package com.vk.vktestapp;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
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

    public static final String saveFolder = "/storage/emulated/0/VkMusic/SingleLoad/";
    public static final int DONT_LOAD = 0;
    public static final int MUST_LOAD = 1;
    public static final int IS_LOAD = 2;

    public static final int columnSize = 11;
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
    private String type; // рекомендованные, мои аудиозаписи или пустая аудиозапись
    private String savePath;


    class AudioDownloader extends AsyncTask<Void, Integer, Integer> {
        ProgressBar bar;
        TextView text;
        AudioDownloader(ProgressBar bar,TextView text){
            this.bar = bar;
            this.text = text;
        }
        // сам фоновый процесс
        @Override
        protected Integer doInBackground(Void... params) {
            loadFile( saveFolder+artist + "-" + title + ".mp3", url);
            return null;
        }

        // инициализация процесса
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            bar.setProgress(progress[0]);
            text.setText("Идёт загрузка: "+progress[0]+"%");
        }

        @Override
        // что нужно сделать в конце процесса
        protected void onPostExecute(Integer param) {
            savePath = saveFolder+artist + "-" + title + ".mp3";
            isLoaded = IS_LOAD;
            // скрываем progressBar
            bar.setVisibility(View.INVISIBLE);
            // скрываем текст
            text.setVisibility(View.INVISIBLE);
        }

    }

    //тестовая запись
    AudioRec(int id, int audio_id,int owner_id, String artist,String title,int duration,String url,int genre_id,int isLoaded,String type,String savePath){
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
    }
    AudioRec(){
        new AudioRec(0,0,0,"","",0,"",0,0,"emppty","");
    }

    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public int getID(){return id;}
    public void setType(String type){ this.type = type;}
    public String getUrl(){ return url;}
    public int getAudioId(){return audio_id;}
    public int getIsLoaded(){return isLoaded;}
    public String getType(){return type;}
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
        return arr;
    }
    int load(ProgressBar bar,TextView text){
        new AudioDownloader(bar,text).execute();
        return audio_id;
    }
}
