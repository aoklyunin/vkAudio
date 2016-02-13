package com.vk.vktestapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "VK";
    public static final String TABLE_AUDIOS = "audioRecTable";
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
    public static final String TABLE_CONFS = "confTable";
    public static final String KEY_INT_VALUE     = "intValue";
    public static final String KEY_STRING_VALUE  = "stringValue";
    public static final String KEY_BOOLEAN_VALUE = "booleanValue";
    public static final String KEY_NAME = "keyName";
    public static final int AUDIO_ROW_CNT = 12;
    
    public static final String KEY_AUDIO_FIND_CONF = "keyAudioFindConf";

    private Activity activity;
    String type;

    public String getStringConf(String key){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONFS, new String[] {
                            KEY_ID, KEY_INT_VALUE, KEY_STRING_VALUE,
                        KEY_BOOLEAN_VALUE, KEY_NAME}, KEY_NAME + "=?",
                    new String[] { key }, null, null, null, null);
            if (cursor != null) {
                try {
                    cursor.moveToFirst();
                    String s = cursor.getString(2);
                    db.close();
                    return s;
                }catch (Exception e){
                    Log.e("DB_GET_VAL",e.getMessage());
                    db.close();
                    return "";
                }
            }else {
                Log.e("CONF_TABLE", "Не найдено записи с ключом: "+key);
                db.close();
                return "";
            }
    }
    public int getIntConf(String key){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONFS, new String[] {
                        KEY_ID, KEY_INT_VALUE, KEY_STRING_VALUE,
                        KEY_BOOLEAN_VALUE, KEY_NAME}, KEY_NAME + "=?",
                new String[] { key }, null, null, null, null);
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                int i = cursor.getInt(1);
                db.close();
                return i;
            }catch (Exception e){
                Log.e("DB_GET_VAL", e.getMessage());
                db.close();
                return 0;
            }
        }else {
            Log.e("CONF_TABLE","Не найдено записи с ключом: "+key);
            db.close();
            return 0;
        }
    }
    public void setStringConf(String name, String val){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_CONFS, KEY_NAME + " = ?", new String[]{name});
        // проверяем, нет ли уже записи с таким же audio_id
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.KEY_NAME, name);
            values.put(DBHelper.KEY_STRING_VALUE, val);
            db.insert(TABLE_CONFS, null, values);
        }catch (SQLException e){
            Log.e("SQL DB", e.getMessage());
        }
        db.close();
    }
    public void setIntConf(String name, int val){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_CONFS, KEY_NAME + " = ?", new String[]{name});
        // проверяем, нет ли уже записи с таким же audio_id
        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.KEY_NAME, name);
            values.put(DBHelper.KEY_INT_VALUE, val);
            db.insert(TABLE_CONFS, null, values);
        }catch (SQLException e){
            Log.e("SQL DB", e.getMessage());
        }
        db.close();
    }
    public void setIsLoad(String type,int isLoad){
        SQLiteDatabase db = this.getReadableDatabase();
        String execStr = "UPDATE " + TABLE_AUDIOS + " SET " +
                KEY_IS_LOADED + " = "+isLoad+" WHERE "+ KEY_IS_LOADED +" <> 2 AND " + KEY_TYPE+" = '"+type+"'";
        Log.e("SQL", "isLoadQuery: " + execStr);
        try {
            db.execSQL(execStr);
        }catch (SQLException e){
            Log.d("SQL DB", e.getMessage());
        }
        db.close();
    }

    public void deleteAudioByType(String type){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_AUDIOS, KEY_TYPE + " = ?", new String[]{type});
        db.close();
    }
    // коструктор от контекста
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // коструктор от активности
    public DBHelper(Activity activity) {
        super(activity, DATABASE_NAME, null, DATABASE_VERSION);
        this.activity = activity;
        this.type = AudioRec.AUDIO_MY;
        Log.e("SQL DB","вызван конструктор без указания типа аудиозаписи");
    }
    // конструктор от активности и типа аудио
    public DBHelper(Activity activity,String type) {
        super(activity, DATABASE_NAME, null, DATABASE_VERSION);
        this.activity = activity;
        this.type = type;
    }


    // создание таблицы
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_AUDIOS + "("
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
        CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONFS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_INT_VALUE + " INTEGER,"
                + KEY_STRING_VALUE + " TEXT,"
                + KEY_BOOLEAN_VALUE + " BOOLEAN,"
                + KEY_NAME + " TEXT )";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    // обновление таблицы
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIOS);
        onCreate(db);
    }
    // добавляем аудиозапись
    public void addAudioRec(AudioRec audio) {
        SQLiteDatabase db = this.getWritableDatabase();
        // проверяем, нет ли уже записи с таким же audio_id
        try {
            String count = "SELECT count(*) FROM "+ TABLE_AUDIOS + " WHERE " + KEY_AUDIO_ID+"="+String.valueOf(audio.getAudioId());
            Cursor mcursor = db.rawQuery(count, null);
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount != 0) {
                Log.d("SQL DB", "Найдено " + icount + " записей с заданным audio_id");
            } else {
                Log.d("SQL DB", "Записей с заданным кол-во audio_id не найдено");
                // получаем значения из
                ContentValues values = audio.getDBValues();
                db.insert(TABLE_AUDIOS, null, values);
            }
            mcursor.close();
        }catch (SQLException e){
            Log.e("SQL DB", e.getMessage());
        }
        db.close();
    }

    // меняем значение isLoad на противоположное (нужно загружать/не нужно)
    public void inverseIsLoaded(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        String execStr = "UPDATE " + TABLE_AUDIOS + " SET " +
                KEY_IS_LOADED + " = (" + KEY_IS_LOADED + "+1) % 2 WHERE id = "+id;
        Log.d("SQL DB",execStr);
        try {
            db.execSQL(execStr);
        }catch (SQLException e){
            Log.d("SQL DB", e.getMessage());
        }
        db.close();
    }

    public AudioRec getAudioRecById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_AUDIOS, new String[] {
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
        String selectQuery = "SELECT  * FROM " + TABLE_AUDIOS;

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

    public List<AudioRec> getAudio(String type) {
        List<AudioRec> audioList = new ArrayList<AudioRec>();
        String selectQuery = "SELECT  * FROM " + TABLE_AUDIOS+ " WHERE " + KEY_TYPE + " = '"+type+"'";

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

        return db.update(TABLE_AUDIOS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(audio.getID()) });
    }

    public void deleteAudioRec(AudioRec audio) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_AUDIOS, KEY_ID + " = ?", new String[]{String.valueOf(audio.getID())});
        db.close();
    }

    public void deleteAll() {
        // удаляем аудиозаписи
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_AUDIOS, null, null);
        // удаляем параметр предыдущего поиска
        db.delete(TABLE_CONFS, KEY_NAME + " = ?", new String[]{KEY_AUDIO_FIND_CONF});
        db.close();
        Toast.makeText(activity,"Аудиозаписи удалены",Toast.LENGTH_SHORT).show();
    }

    public int getAudioCount(String type) {
        String countQuery = "SELECT  * FROM " + TABLE_AUDIOS+ " WHERE " + KEY_TYPE + " = '"+type+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        db.close();
        return cnt;
    }

    public int getCountMustLoad(){
        SQLiteDatabase db = this.getWritableDatabase();
        String count = "SELECT count(*) FROM "+ TABLE_AUDIOS + " WHERE " + KEY_IS_LOADED+"="+AudioRec.MUST_LOAD;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int cnt = mcursor.getInt(0);
        mcursor.close();
        db.close();
        return cnt;
    }


    public AudioRec getAudioByTableRowID(int id){
        Log.e("DB_TYPE",type);
        String selectQuery = "SELECT * FROM " + TABLE_AUDIOS+
                " WHERE "+ KEY_TABLE_ROW_ID+"="+id+" AND "+KEY_TYPE+"='"+type+"'";
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
        String selectQuery = "SELECT * FROM " + TABLE_AUDIOS+
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
        String execStr = "UPDATE " + TABLE_AUDIOS + " SET " +
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
        String execStr = "UPDATE " + TABLE_AUDIOS + " SET " +
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

    public void setTableRowId(int rowId,int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String execStr = "UPDATE " + TABLE_AUDIOS + " SET " +
                KEY_TABLE_ROW_ID + " = " + rowId + " WHERE "+ KEY_ID +" = "+id;
        Log.d("SQL DB",execStr);
        try {
            db.execSQL(execStr);
        }catch (SQLException e){
            Log.d("SQL DB", e.getMessage());
        }
        db.close();
    }

    
    // создаём i-ю строчку таблицы по audio
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TableLayout addTableRow(TableLayout tableLayout,AudioRec audio, int i) {
        // строка таблицы
        TableRow tableRow = new TableRow(activity);
        // параметры строка таблицы
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                                           TableRow.LayoutParams.MATCH_PARENT));
        // получаем новый id для строки в таблице
        int id = View.generateViewId();
        setTableRowId(id, audio.getID());

        tableRow.setId(id);
        tableLayout.addView(tableRow, i);

        View.OnClickListener lsn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow tr = (TableRow) v.getParent();
                AudioRec audio = getAudioByTableRowID(tr.getId());
                audio.setActivity(activity);
                audio.downloadDialog();
            }
        };

        TextView text = new TextView(activity);
        text.setWidth(200);
        text.setPadding(5, 5, 5, 5);
        text.setText(audio.getID() + "");
        text.setTextSize(25);
        tableRow.addView(text, 0);

        Button btnPlay = new Button(activity);
        btnPlay.setText("Проиграть");
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow tr = (TableRow) v.getParent();
                loadAudioByTablleRowIdDialogAndPlay(tr.getId(), activity);
            }
        });
        tableRow.addView(btnPlay, 1);

        text = new TextView(activity);
        text.setWidth(800);
        text.setPadding(5, 5, 5, 5);
        text.setText(audio.getTitle());
        text.setTextSize(25);
        text.setOnClickListener(lsn);
        tableRow.addView(text, 2);

        text = new TextView(activity);
        text.setWidth(800);
        text.setPadding(5, 5, 5, 5);
        text.setText(audio.getArtist());
        text.setTextSize(25);
        text.setOnClickListener(lsn);
        tableRow.addView(text, 3);

        Button btn = new Button(activity);
        switch (audio.getIsLoaded()) {
            case AudioRec.DONT_LOAD:
                btn.setText("Не загружать");
                btn.setTextColor(0xFFFF0000);
                break;
            case AudioRec.MUST_LOAD:
                btn.setText("Загружать");
                btn.setTextColor(0xFF0000FF);
                break;
            case AudioRec.IS_LOAD:
                btn.setText("Загружено");
                btn.setTextColor(0xFF00FF00);
                break;
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow tr = (TableRow) v.getParent();
                TextView tw = (TextView) tr.getChildAt(1);
                inverseIsLoaded((String) tw.getText());
                Button btn = (Button) tr.getChildAt(4);
                switch ((String) btn.getText()) {
                    case "Загружать":
                        btn.setText("Не загружать");
                        btn.setTextColor(0xFFFF0000);
                        break;
                    case "Не загружать":
                        btn.setText("Загружать");
                        btn.setTextColor(0xFF0000FF);
                        break;
                    case "Загружено":
                        Toast.makeText(activity, "Хотим удалить загруженное", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        tableRow.addView(btn, 4);
        return  tableLayout;
    }
    // получить список аудиозаписей по номеру страницы
    List<AudioRec> getAudiosByPos(int pos,String type){
        List<AudioRec> audioList = getAudio(type);
        List<AudioRec> curAudioList = new ArrayList<AudioRec>();
        int size = audioList.size();
        if((pos+1)*AUDIO_ROW_CNT<size)
            for(int i=0;i<AUDIO_ROW_CNT;i++)
                curAudioList.add(audioList.get(i+pos*AUDIO_ROW_CNT));
        else
            for(int i=pos*AUDIO_ROW_CNT;i<size;i++)
                curAudioList.add(audioList.get(i));
        return curAudioList;
    }
    // создаём страницу по списку аудиозаписей
    View getAudioPage(List<AudioRec> audios){
        // создаем LayoutParams
        TableLayout tableLayout = new TableLayout(activity);
        TableLayout.LayoutParams tableLayoutParam = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                                                                 TableLayout.LayoutParams.MATCH_PARENT);
        tableLayout.setLayoutParams(tableLayoutParam);
        int i=0;
        for (AudioRec audio : audios)
            addTableRow(tableLayout, audio, i++);
        return tableLayout;
    }
    // создаём страницу по номеру и типу
    public View getPageByPos(int pos){
        List<AudioRec> audios = getAudiosByPos(pos,type);
        return getAudioPage(audios);
    }




}