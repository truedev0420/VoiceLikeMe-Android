package com.appbestsmile.voicelikeme.di;

import dagger.Module;
import dagger.Provides;
import com.appbestsmile.voicelikeme.audiorecording.AudioRecordMVPView;
import com.appbestsmile.voicelikeme.audiorecording.AudioRecordPresenter;
import com.appbestsmile.voicelikeme.audiorecording.AudioRecordPresenterImpl;
import com.appbestsmile.voicelikeme.di.scopes.FragmentScope;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by arjun on 12/1/17.
 */

@Module
class RecordFragmentModule {

  @Provides
  @FragmentScope
  AudioRecordPresenter<AudioRecordMVPView> provideAudioRecordPresenter(
      AudioRecordPresenterImpl<AudioRecordMVPView> audioRecordPresenter) {
    return audioRecordPresenter;
  }

  @Provides
  @FragmentScope
  CompositeDisposable provideCompositeDisposable() {
    return new CompositeDisposable();
  }
}
