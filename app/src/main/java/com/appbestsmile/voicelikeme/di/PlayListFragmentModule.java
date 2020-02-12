package com.appbestsmile.voicelikeme.di;

import dagger.Module;
import dagger.Provides;
import com.appbestsmile.voicelikeme.di.scopes.FragmentScope;
import com.appbestsmile.voicelikeme.playlist.PlayListMVPView;
import com.appbestsmile.voicelikeme.playlist.PlayListPresenter;
import com.appbestsmile.voicelikeme.playlist.PlayListPresenterImpl;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by arjun on 12/1/17.
 */

@Module
class PlayListFragmentModule {

  @Provides
  @FragmentScope
  PlayListPresenter<PlayListMVPView> providePlayListPresenter(PlayListPresenterImpl<PlayListMVPView> playListPresenter) {
    return playListPresenter;
  }

  @Provides
  @FragmentScope
  CompositeDisposable provideCompositeDisposable() {
    return new CompositeDisposable();
  }
}
