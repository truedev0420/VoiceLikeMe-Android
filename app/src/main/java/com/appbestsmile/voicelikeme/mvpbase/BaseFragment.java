package com.appbestsmile.voicelikeme.mvpbase;

import android.content.Context;
import dagger.android.support.AndroidSupportInjection;
import com.appbestsmile.voicelikeme.theme.ThemedFragment;

public abstract class BaseFragment extends ThemedFragment implements IMVPView {

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    AndroidSupportInjection.inject(this);
  }
}
