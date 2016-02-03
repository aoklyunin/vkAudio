package com.vk.vktestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.TableRow;

public class AudioActivity extends FragmentActivity {

    int cnt = 0;
    String type;

    ViewPager pager;
    PagerAdapter pagerAdapter;

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
            DBHelper db = new DBHelper(this,type);
            db.loadAudioByTablleRowIdDialog(id,AudioActivity.this);
            bt.setText("Загружено");
            bt.setTextColor(0xFF00FF00);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        // узнаём, какой тип аудиозаписей нужно загрузить
        Intent intent = getIntent();
        type = intent.getStringExtra("type");

        DBHelper db = new DBHelper(this,type);
        cnt = db.getAudioCount(type);

        if (cnt % DBHelper.AUDIO_ROW_CNT!=0)
            cnt = cnt/DBHelper.AUDIO_ROW_CNT+1;
        else
            cnt = cnt/DBHelper.AUDIO_ROW_CNT;

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(pagerAdapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
        private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

            public MyFragmentPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                return PageFragment.newInstance(position,type);
            }

            @Override
            public int getCount() {
                return cnt;
            }

        }

    }
