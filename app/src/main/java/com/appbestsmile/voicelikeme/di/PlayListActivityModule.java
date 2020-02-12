package com.appbestsmile.voicelikeme.di;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import com.appbestsmile.voicelikeme.activities.PlayListActivity;
import com.appbestsmile.voicelikeme.di.qualifiers.ActivityContext;
import com.appbestsmile.voicelikeme.di.scopes.ActivityScope;

/**
 * Created by arjun on 12/1/17.
 */

@Module
class PlayListActivityModule {
  @Provides
  @ActivityContext
  @ActivityScope
  Context provideActivityContext(PlayListActivity activity) {
    return activity;
  }
}
