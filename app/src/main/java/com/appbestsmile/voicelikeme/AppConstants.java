package com.appbestsmile.voicelikeme;

import android.content.Context;

import com.appbestsmile.voicelikeme.global.AppPreference;
import com.walid.speex.Speex;

public class AppConstants {
  public static final String ACTION_PAUSE = "com.appbestsmile.voicelikeme.PAUSE";
  public static final String ACTION_RESUME = "com.appbestsmile.voicelikeme.RESUME";
  public static final String ACTION_STOP = "com.appbestsmile.voicelikeme.STOP";
  public static final String ACTION_IN_SERVICE = "com.appbestsmile.voicelikeme.ACTION_IN_SERVICE";

  public static final String APP_DATA_FOLDER = "SoundRecorder";

//  public static Context applicationContext;

  public static int PLAY_COUNT = 5;

  public static Speex speex;
}
