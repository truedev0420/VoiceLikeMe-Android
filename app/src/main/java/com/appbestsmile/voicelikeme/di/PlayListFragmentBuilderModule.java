package com.appbestsmile.voicelikeme.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import com.appbestsmile.voicelikeme.di.scopes.FragmentScope;
import com.appbestsmile.voicelikeme.playlist.PlayListFragment;

/**
 * Created by arjun on 12/1/17.
 */

@Module
abstract class PlayListFragmentBuilderModule {
  @FragmentScope
  @ContributesAndroidInjector(modules = {PlayListFragmentModule.class})
  abstract PlayListFragment contributePlayListFragment();
}
