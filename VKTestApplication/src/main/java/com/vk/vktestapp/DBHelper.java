package com.vk.vktestapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "VK";
    public static final String TABLE_CONTACTS = "audioRecTable";
    public static final String KEY_ID = "id";
    public static final String KEY_AUDIO_ID = "audioId";
    public static final String KEY_OWNER_ID = "ownerId";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_URL = "url";
    public static final String KEY_GENRE_ID = "genreId";
    public static final String KEY_IS_LOADED = "isLoaded";
    public static final String KEY_TYPE = "type";
    public static final String KEY_PATH_TO_SAVE = "pathToSave";
    public static final String KEY_TABLE_ROW_ID = "tableRowID";
    private Activity activity;

    // коструктор
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public DBHelper(Activity activity) {
        super(activity, DATABASE_NAME, null, DATABASE_VERSION);
        this.activity = activity;
    }


    // создание таблицы
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_AUDIO_ID + " INTEGER,"
                + KEY_OWNER_ID + " INTEGER,"
                + KEY_ARTIST + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_DURATION + " INTEGER,"
                + KEY_URL + " TEXT,"
                + KEY_GENRE_ID + " INTEGER,"
                + KEY_IS_LOADED + " INTEGER,"
                + KEY_TYPE + " TEXT,"
                + KEY_PATH_TO_SAVE + " TEXT,"
                + KEY_TABLE_ROW_ID + " INTEGER" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // обновление таблицы
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    // добавляем аудиозапись
    public void addAudioRec(AudioRec audio) {
        SQLiteDatabase db = this.getWritableDatabase();
        // проверяем, нет ли уже записи с таким же audio_id
        try {
            String count = "SELECT count(*) FROM "+ TABLE_CONTACTS + " WHERE " + KEY_AUDIO_ID+"="+String.valueOf(audio.getAudioId());
            Cursor mcursor = db.rawQuery(count, null);
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount != 0) {
                Log.d("SQL DB", "Найдено " + icount + " записей с заданным audio_id");
            } else {
                Log.d("SQL DB", "Записей с заданным кол-во audio_id не найдено");
                // получаем значения из
                ContentValues values = audio.getDBValues();
                db.insert(TABLE_CONTACTS, null, values);
            }
            mcursor.close();
        }catch (SQLException e){
            Log.e("SQL DB",e.getMessage());
        }
        db.close();
    }

    // меняем значение isLoad на противоположное (нужно загружать/не нужно)
    public void inverseIsLoaded(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        String execStr = "UPDATE " + TABLE_CONTACTS + " SET " +
                KEY_IS_LOADED + " = (" + KEY_IS_LOADED + "+1) % 2 WHERE id = "+id;
        Log.d("SQL DB",execStr);
        try {
            db.execSQL(execStr);
        }catch (SQLException e){
            Log.d("SQL DB", e.getMessage());
        }
        db.close();
    }

    public AudioRec getAudioRec(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] {
                        KEY_ID, KEY_AUDIO_ID, KEY_OWNER_ID,
                        KEY_ARTIST, KEY_TITLE, KEY_DURATION,
                        KEY_URL, KEY_GENRE_ID, KEY_IS_LOADED,
                        KEY_TYPE,KEY_PATH_TO_SAVE,KEY_TABLE_ROW_ID}, KEY_ID + "=?",
                        new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        AudioRec audio = new AudioRec(cursor);
        return audio;
    }

    public List<AudioRec> getAllAudioRecs() {
        List<AudioRec> audioList = new ArrayList<AudioRec>();
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AudioRec audio =  new AudioRec(cursor);
                audioList.add(audio);
            } while (cursor.moveToNext());
        }
        db.close();
        return audioList;
    }

    public List<AudioRec> getAudioMy() {
        List<AudioRec> audioList = new ArrayList<AudioRec>();
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS+ " WHERE " + KEY_TYPE + " = '"+AudioRec.AUDIO_MY+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AudioRec audio =  new AudioRec(cursor);
                audioList.add(audio);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return audioList;
    }
    public List<AudioRec> getAudioRecommend() {
        List<AudioRec> audioList = new ArrayList<AudioRec>();
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS+ " WHERE " + KEY_TYPE + " = '"+AudioRec.AUDIO_RECOMMEND+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AudioRec audio =  new AudioRec(cursor);
                audioList.add(audio);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return audioList;
    }

    void loadAudioByTablleRowIdDialog(int id,Activity activity){
        try {
            AudioRec audio = getAudioByTableRowID(id);
            audio.setActivity(activity);
            String savePath = audio.loadDialog();
            setAudioIsLoaded(audio.getAudioId(), savePath);
        }catch (Exception e){
            Log.d("AUDIO",e.getMessage());
        }
    }
    void loadAudioByTablleRowIdDialogAndPlay(int id,Activity activity){
        try {
            AudioRec audio = getAudioByTableRowID(id);
            audio.setActivity(activity);
            String savePath = audio.loadDialogAndPlay();
            setAudioIsLoaded(audio.getAudioId(), savePath);
        }catch (Exception e){
            Log.d("AUDIO",e.getMessage());
        }
    }




    public int updateAudioRec(AudioRec audio) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ARTIST, audio.getArtist());
        values.put(KEY_TITLE, audio.getTitle());

        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(audio.getID()) });
    }

    public void deleteAudioRec(AudioRec audio) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?", new String[]{String.valueOf(audio.getID())});
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, null, null);
        db.close();
    }

    public int getAudioRecomendCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS+ " WHERE " + KEY_TYPE + " = '"+AudioRec.AUDIO_RECOMMEND+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        db.close();
        return cnt;
    }
    public int getAudioMyCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS+ " WHERE " + KEY_TYPE + " = '"+AudioRec.AUDIO_MY+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        db.close();
        return cnt;
    }

    public int getCountMustLoad(){
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM "+ TABLE_CONTACTS + " WHERE " + KEY_IS_LOADED+"="+AudioRec.MUST_LOAD;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int cnt = mcursor.getInt(0);
        mcursor.close();
        db.close();
        return cnt;
    }


    public AudioRec getAudioByTableRowID(int id){
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS+
                " WHERE "+ KEY_TABLE_ROW_ID+"="+id;
        SQLiteDatabase db = this.getWritableDatabase();
        AudioRec audio = null;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            audio = new AudioRec(cursor);
        }else {
            Log.e("SQL DB", "Не найдено ни одной записи на загрузку");
        }
        db.close();
        return audio;
    }

    public AudioRec getFirstAudioToLoad(){
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS+
                             " WHERE "+ KEY_IS_LOADED+"="+AudioRec.MUST_LOAD+
                             " LIMIT 1";
        Log.e("SQL","queyLoad: "+selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        AudioRec audio = null;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            audio = new AudioRec(cursor);
        }else {
            Log.e("SQL DB", "Не найдено ни одной записи на загрузку");
        }
        cursor.close();
        db.close();
        return audio;
    }
    void setAudioIsLoaded(int audioId,String savePath){
        SQLiteDatabase db = this.getReadableDatabase();
        String execStr = "UPDATE " + TABLE_CONTACTS + " SET " +
                KEY_IS_LOADED + " = 2, "+ KEY_PATH_TO_SAVE+" = '"+savePath+"' WHERE "+ KEY_AUDIO_ID +" = "+audioId;
        Log.e("SQL", "isLoadQuery: " + execStr);
        try {
            db.execSQL(execStr);
        }catch (SQLException e){
            Log.d("SQL DB", e.getMessage());
        }
        db.close();
    }
    void setAudioIsLoadedTR(int rowId,String savePath){
        SQLiteDatabase db = this.getReadableDatabase();
        String execStr = "UPDATE " + TABLE_CONTACTS + " SET " +
                KEY_IS_LOADED + " = 2, "+ KEY_PATH_TO_SAVE+" = '"+savePath+"' WHERE "+ KEY_TABLE_ROW_ID +" = "+rowId;
        Log.e("SQL", "isLoadQuery: " + execStr);
        try {
            db.execSQL(execStr);
        }catch (SQLException e){
            Log.d("SQL DB", e.getMessage());
        }
        db.close();
    }

    public int loadFirstAudio(ProgressBar firstBar, TextView barText){
        try {
            AudioRec audio = getFirstAudioToLoad();
            String savePath = audio.load(firstBar,barText);
            setAudioIsLoaded(audio.getAudioId(), savePath);
            return audio.getTableRowID();
        }catch (Exception e){
            return -1;
        }
    }

    public void loadAudioByTablleRowId(ProgressBar firstBar, TextView barText,int id){
        try {
            AudioRec audio = getAudioByTableRowID(id);
            String savePath = audio.load(firstBar, barText);
            setAudioIsLoaded(audio.getAudioId(), savePath);
        }catch (Exception e){
            Log.d("AUDIO",e.getMessage());
        }
    }

    public void setTableRowId(int rowId,int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String execStr = "UPDATE " + TABLE_CONTACTS + " SET " +
                KEY_TABLE_ROW_ID + " = " + rowId + " WHERE "+ KEY_ID +" = "+id;
        Log.d("SQL DB",execStr);
        try {
            db.execSQL(execStr);
        }catch (SQLException e){
            Log.d("SQL DB", e.getMessage());
        }
        db.close();
    }

    public void loadAudioById(ProgressBar firstBar, TextView barText,int id){

    }

}