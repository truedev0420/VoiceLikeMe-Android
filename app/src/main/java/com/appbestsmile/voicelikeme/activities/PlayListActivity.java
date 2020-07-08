package com.appbestsmile.voicelikeme.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.db.RecordingItem;
import com.appbestsmile.voicelikeme.mvpbase.BaseActivity;
import com.appbestsmile.voicelikeme.playlist.PlayListFragment;

import javax.inject.Inject;

public class PlayListActivity extends BaseActivity implements HasSupportFragmentInjector {

  @Inject DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_list);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setTitle(R.string.tab_title_saved_recordings);
//      actionBar.setDisplayHomeAsUpEnabled(true);
//      actionBar.setDisplayShowHomeEnabled(true);
    }
    setNavBarColor();

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // back button pressed
        onBackPressed();
      }
    });


    PlayListFragment playListFragment  = PlayListFragment.newInstance();

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.record_list_container, playListFragment)
          .commit();
    }

    // If PlayListActivity was started from Alarm Notification Receiver

    String voice_name = getIntent().getStringExtra("voice_name");
    if(voice_name != null){
      playListFragment.setNotiData(voice_name);
    }

    // Show RecordingActivity when record floatingactionbutton was clicked.
    FloatingActionButton btnRecord = (FloatingActionButton) findViewById(R.id.btnRecord);
    btnRecord.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
        startActivity(new Intent(PlayListActivity.this, MainActivity.class));
      }
    });
  }

  @Override public AndroidInjector<Fragment> supportFragmentInjector() {
    return dispatchingAndroidInjector;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
    // If you don't have res/menu, just create a directory named "menu" inside res
    getMenuInflater().inflate(R.menu.record_playlist_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.btnChat) {
      Intent intent = new Intent(this, ChatTopicActivity.class);
      startActivity(intent);
    }
    return super.onOptionsItemSelected(item);
  }
}
