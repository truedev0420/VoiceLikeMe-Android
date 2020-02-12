package com.appbestsmile.voicelikeme.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import com.appbestsmile.voicelikeme.audiorecording.RecordFragment;
import com.appbestsmile.voicelikeme.di.scopes.FragmentScope;

/**
 * Created by arjun on 12/1/17.
 */

@Module
abstract class RecordFragmentBuilderModule {
  @FragmentScope
  @ContributesAndroidInjector(modules = {RecordFragmentModule.class})
  abstract RecordFragment contributeRecordFragment();
}
