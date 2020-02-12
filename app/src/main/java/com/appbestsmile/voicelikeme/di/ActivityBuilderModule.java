package com.appbestsmile.voicelikeme.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import com.appbestsmile.voicelikeme.activities.MainActivity;
import com.appbestsmile.voicelikeme.activities.PlayListActivity;
import com.appbestsmile.voicelikeme.activities.SettingsActivity;
import com.appbestsmile.voicelikeme.di.scopes.ActivityScope;

/**
 * Created by arjun on 12/1/17.
 */

@Module
abstract class ActivityBuilderModule {
  @ActivityScope
  @ContributesAndroidInjector(modules = {MainActivityModule.class, RecordFragmentBuilderModule.class})
  abstract MainActivity contributeMainActivity();

  @ActivityScope
  @ContributesAndroidInjector(modules = {PlayListActivityModule.class, PlayListFragmentBuilderModule.class})
  abstract PlayListActivity contributePlayListActivity();

  @ContributesAndroidInjector()
  abstract SettingsActivity contributeSettingsActivity();
}
