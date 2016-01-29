package com.vk.vktestapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

public class AudioActivity extends Activity implements View.OnTouchListener {
    ViewFlipper flipper;
    float fromPosition;
    TableLayout tableLayout;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private int getId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //return Utils.generateViewId();
            return -1;
        } else {
            return View.generateViewId();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        boolean flg = data.getBooleanExtra("BOOL", false);
        if (flg) {
            int id = data.getIntExtra("ID", 0);
            TableRow tr = (TableRow) findViewById(id);
            Button bt = (Button) tr.getChildAt(4);
            DBHelper db = new DBHelper(this);

            db.loadAudioByTablleRowIdDialog(id,AudioActivity.this);

            bt.setText("Загружено");
            bt.setTextColor(0xFF00FF00);

        }
    }

    // создаём i-ю строчку таблицы по audio
    private void createTableRow(AudioRec audio, int i) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT));
        DBHelper db = new DBHelper(this);
        int id = getId();
        db.setTableRowId(id, audio.getID());
        tableRow.setId(id);
        tableLayout.addView(tableRow, i);

        View.OnClickListener lsn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("CLICKLISTENER","Сработало");
                TableRow tr = (TableRow) v.getParent();
                DBHelper db = new DBHelper(AudioActivity.this);
                AudioRec audio = db.getAudioByTableRowID(tr.getId());
                audio.setActivity(AudioActivity.this);
                audio.downloadDialog();
                /*
                DBHelper db = new DBHelper(getApplicationContext());
                Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
                TableRow tr = (TableRow) v.getParent();

                intent.putExtra("ID", tr.getId());
                AudioRec audio = db.getAudioByTableRowID(tr.getId());
                intent.putExtra("LABEL",audio.getArtist()+"-"+audio.getTitle());
                startActivityForResult(intent,1);
                */
            }
        };

        TextView text = new TextView(this);
        text.setWidth(200);
        text.setPadding(5, 5, 5, 5);
        text.setText(audio.getID() + "");
        text.setTextSize(25);
        tableRow.addView(text, 0);


        Button btnPlay = new Button(this);
        btnPlay.setText("Проиграть");
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper db = new DBHelper(AudioActivity.this);
                TableRow tr = (TableRow) v.getParent();
                db.loadAudioByTablleRowIdDialogAndPlay(tr.getId(), AudioActivity.this);
            }
        });
        tableRow.addView(btnPlay, 1);


        text = new TextView(this);
        text.setWidth(800);
        text.setPadding(5, 5, 5, 5);
        text.setText(audio.getTitle());
        text.setTextSize(25);
        text.setOnClickListener(lsn);
        tableRow.addView(text, 2);


        text = new TextView(this);
        text.setWidth(800);
        text.setPadding(5, 5, 5, 5);
        text.setText(audio.getArtist());
        text.setTextSize(25);
        text.setOnClickListener(lsn);
        tableRow.addView(text, 3);

        Button btn = new Button(this);
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
                DBHelper db = new DBHelper(AudioActivity.this);
                db.inverseIsLoaded((String) tw.getText());
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
                        Toast.makeText(AudioActivity.this, "Хотим удалить загруженное", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        tableRow.addView(btn, 4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        // Устанавливаем listener касаний, для последующего перехвата жестов
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainLayout.setOnTouchListener(this);

        DBHelper db = new DBHelper(this);
        Intent intent = getIntent();
        List<AudioRec> audios;
        int audioCnt;
        if (intent.getStringExtra("type").equals(AudioRec.AUDIO_MY)) {
            audioCnt = db.getAudioMyCount();
            audios = db.getAudioMy();
            Log.e("AUDIO ACTIVITY", "кол-во " + audios.size());
        } else {
            audioCnt = db.getAudioRecomendCount();
            audios = db.getAudioRecommend();
        }

        tableLayout = new TableLayout(this);
        flipper = (ViewFlipper) findViewById(R.id.flipper);


        int i = 13;
        for (AudioRec audio : audios) {
            if (i > 12) {
                /* линейная разметкка */
                LinearLayout linLayout = new LinearLayout(this);
                // установим вертикальную ориентацию
                linLayout.setOrientation(LinearLayout.VERTICAL);
                // создаем LayoutParams
                LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                linLayout.setLayoutParams(linLayoutParam);
                tableLayout = new TableLayout(this);
                // создаем LayoutParams
                TableLayout.LayoutParams tableLayoutParam = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.MATCH_PARENT);
                tableLayout.setLayoutParams(tableLayoutParam);
                linLayout.addView(tableLayout);
                flipper.addView(linLayout);
                i = 0;
            }
            createTableRow(audio, i++);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // обработка нажатия на экран
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // Пользователь нажал на экран, т.е. начало движения
                // fromPosition - координата по оси X начала выполнения операции
                fromPosition = event.getX();
                break;
            case MotionEvent.ACTION_UP: // Пользователь отпустил экран, т.е. окончание движения
                float toPosition = event.getX();
                // если слайдинг вправо
                if (fromPosition > toPosition) {
                    // задаёт анимации для входа и выхода вьюх
                    flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.flipin_right));
                    flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.flipout_right));
                    // следующая вьюха
                    flipper.showNext();
                    // если слайдинг влево
                } else if (fromPosition < toPosition) {
                    // задаёт анимации для входа и выхода вьюх
                    flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.flipin_left));
                    flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.flipout_left));
                    // предыдущая вьюха
                    flipper.showPrevious();
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Audio Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.vk.vktestapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Audio Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.vk.vktestapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
