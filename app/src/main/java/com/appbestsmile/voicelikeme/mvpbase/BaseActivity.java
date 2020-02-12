package com.appbestsmile.voicelikeme.mvpbase;

import android.os.Bundle;
import dagger.android.AndroidInjection;
import com.appbestsmile.voicelikeme.theme.ThemedActivity;

public abstract class BaseActivity extends ThemedActivity implements IMVPView {
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AndroidInjection.inject(this);
  }
}
