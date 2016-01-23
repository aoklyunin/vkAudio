package com.vk.vktestapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class AudioActivity extends AppCompatActivity {
    DBHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        db = new DBHelper(this);

        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout2);
        List<AudioRec> audios = db.getAllAudioRecs();
        int i = 0;
        for (AudioRec audio : audios) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));

            CheckBox cb = new CheckBox(this);
            cb.setPadding(5, 5, 5, 5);
            tableRow.addView(cb, 0);

            View.OnClickListener lst = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(), DialogActivity.class);
                    startActivity(intent);

                }
            };
            TextView text = new TextView(this);
            text.setMaxWidth(200);
            text.setPadding(5, 5, 5, 5);
            text.setText(audio.getID() + "");
            text.setTextSize(25);
            tableRow.addView(text, 1);

            text = new TextView(this);
            text.setMaxWidth(1000);
            text.setPadding(5, 5, 5, 5);
            text.setText(audio.getTitle());
            text.setTextSize(25);
            text.setOnClickListener(lst);
            tableRow.addView(text, 2);

            text = new TextView(this);
            text.setMaxWidth(1000);
            text.setPadding(5, 5, 5, 5);
            text.setText(audio.getArtist());
            text.setTextSize(25);
            text.setOnClickListener(lst);
            tableRow.addView(text, 3);
            tableLayout.addView(tableRow, i++);

            Button btn = new Button(this);
            switch( audio.getIsLoaded()){
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
            Button btnLoad = (Button)findViewById(R.id.load_audio_bd_button);
            btnLoad.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int id = db.loadFirstAudio((ProgressBar)findViewById(R.id.progress_bar_audio),
                                                    (TextView)findViewById(R.id.audio_text));
                }
            });
            text = new TextView(this);
            text.setMaxWidth(1000);
            text.setPadding(5, 5, 5, 5);
            text.setText(audio.getType());
            text.setTextSize(25);
            tableRow.addView(text, 5);
        }
    }


}
