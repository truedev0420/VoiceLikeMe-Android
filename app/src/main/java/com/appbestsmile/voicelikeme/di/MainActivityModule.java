package com.appbestsmile.voicelikeme.di;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import com.appbestsmile.voicelikeme.activities.MainActivity;
import com.appbestsmile.voicelikeme.di.qualifiers.ActivityContext;
import com.appbestsmile.voicelikeme.di.scopes.ActivityScope;

@Module
public class MainActivityModule {
  @Provides
  @ActivityContext
  @ActivityScope
  Context provideActivityContext(MainActivity activity) {
    return activity;
  }
}
