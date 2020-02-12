package com.appbestsmile.voicelikeme.audiorecording;

import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.appbestsmile.voicelikeme.AppConstants;
import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.activities.PlayListActivity;
import com.appbestsmile.voicelikeme.audiovisualization.GLAudioVisualizationView;
import com.appbestsmile.voicelikeme.di.qualifiers.ActivityContext;
import com.appbestsmile.voicelikeme.mvpbase.BaseFragment;
import com.appbestsmile.voicelikeme.recordingservice.AudioRecordService;
import com.appbestsmile.voicelikeme.recordingservice.AudioRecorder;
import com.appbestsmile.voicelikeme.theme.ThemeHelper;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import pl.droidsonroids.gif.GifImageView;

import javax.inject.Inject;

public class RecordFragment extends BaseFragment implements AudioRecordMVPView {
  private static final String LOG_TAG = RecordFragment.class.getSimpleName();
  private FloatingActionButton mRecordButton = null;
  private FloatingActionButton mPauseButton = null;
  private GLAudioVisualizationView audioVisualization;

  private GifImageView mRecodingView = null;
  private TextView mTextStart = null;

  private TextView chronometer;
  private boolean mIsServiceBound = false;
  private AudioRecordService mAudioRecordService;
  private ObjectAnimator alphaAnimator;
  /*private FloatingActionButton mSettingsButton;*/
//  private FloatingActionButton mPlayListBtn;
  private FloatingActionButton mPlayListBtn;

  private ImageView mHelpButton;

  @Inject
  @ActivityContext
  public Context mContext;

  @Inject
  public AudioRecordPresenter<AudioRecordMVPView> audioRecordPresenter;

  public static RecordFragment newInstance() {
    return new RecordFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    audioRecordPresenter.onAttach(this);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View recordView = inflater.inflate(R.layout.fragment_record, container, false);
    initViews(recordView);
    bindEvents();
    return recordView;
  }

  private void bindEvents() {
    RxView.clicks(mRecordButton)
        .subscribe(o -> audioRecordPresenter.onToggleRecodingStatus());
    /*RxView.clicks(mSettingsButton)
        .subscribe(o -> startActivity(new Intent(mContext, SettingsActivity.class)));*/
    RxView.clicks(mPlayListBtn)
        .subscribe(o -> startActivity(new Intent(mContext, PlayListActivity.class)));
    RxView.clicks(mPauseButton)
        .subscribe(o -> audioRecordPresenter.onTogglePauseStatus());

    mHelpButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_website_url)));
        startActivity(browserIntent);
      }
    });
  }

  @SuppressLint("RestrictedApi")
  private void initViews(View recordView) {
    chronometer = recordView.findViewById(R.id.chronometer);

    audioVisualization = recordView.findViewById(R.id.visualizer_view);

//    mSettingsButton = recordView.findViewById(R.id.settings_btn);

    mPlayListBtn = recordView.findViewById(R.id.play_list_btn);
    mRecordButton = recordView.findViewById(R.id.btnRecord);
    mPauseButton = recordView.findViewById(R.id.btnPause);
    mTextStart = recordView.findViewById(R.id.start_text);
    mHelpButton =  recordView.findViewById(R.id.help_btn);

    mTextStart.setVisibility(View.VISIBLE);

    mRecodingView = recordView.findViewById(R.id.recording);
    mRecodingView.setVisibility(View.GONE);

    mPauseButton.setEnabled(false);

    alphaAnimator =
        ObjectAnimator.ofObject(chronometer, "alpha", new FloatEvaluator(), 0.2f);
    alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
    alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
    audioRecordPresenter.onViewInitialised();
  }

  private void setAsPauseBtn() {
    alphaAnimator.cancel();
    chronometer.setAlpha(1.0f);
    mPauseButton.setImageResource(R.drawable.ic_media_pause);
    mRecodingView.setImageResource(R.drawable.recording);
  }

  @Override
  public void setDefaultPauseStatus(){
    mPauseButton.setEnabled(false);
    mPauseButton.setImageResource(R.drawable.ic_pause_black_24dp);
  }

  private void setAsResumeBtn() {
    alphaAnimator.start();
    mPauseButton.setImageResource(R.drawable.ic_media_record);
    mRecodingView.setImageResource(R.drawable.recording_paused);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    audioVisualization.release();
    audioRecordPresenter.onDetach();
  }

  @Override public void refreshTheme(ThemeHelper themeHelper) {
    GLAudioVisualizationView.ColorsBuilder colorsBuilder =
        new GLAudioVisualizationView.Builder(mContext);
    colorsBuilder.setBackgroundColor(themeHelper.getPrimaryColor());
    colorsBuilder.setLayerColors(themeHelper.getLayerColor());
    audioVisualization.updateConfig(colorsBuilder);
//    chronometer.setTextColor(themeHelper.getLayerColor()[3]);

    mRecordButton.setRippleColor(themeHelper.getLayerColor()[3]);

    /*mSettingsButton.setRippleColor(themeHelper.getLayerColor()[3]);*/

    mPlayListBtn.setRippleColor(themeHelper.getLayerColor()[3]);
    mPauseButton.setRippleColor(themeHelper.getLayerColor()[3]);
  }

  @Override public void updateChronometer(String text) {
    chronometer.setText(text);
  }

  @Override public void togglePauseStatus() {
    if (!audioRecordPresenter.isPaused()) {
      setAsPauseBtn();
    } else {
      setAsResumeBtn();
    }
  }

  @Override
  public void pauseRecord() {
    mAudioRecordService.pauseRecord();
    togglePauseStatus();
  }

  @Override
  public void resumeRecord() {
    mAudioRecordService.resumeRecord();
    togglePauseStatus();
  }

  @Override public void toggleRecordButton() {
    mRecordButton.setImageResource(
        audioRecordPresenter.isRecording() ? R.drawable.ic_media_stop : R.drawable.ic_media_record);
  }

  @Override public void linkGLViewToHandler() {
    audioVisualization.linkTo(mAudioRecordService.getHandler());
  }

  @SuppressLint("RestrictedApi")
  @Override public void setPauseButtonVisible() {
    mPauseButton.setEnabled(true);
    mRecodingView.setVisibility(View.VISIBLE);
    mTextStart.setVisibility(View.GONE);
  }

  @SuppressLint("RestrictedApi")
  @Override public void setPauseButtonInVisible() {
    mPauseButton.setEnabled(false);
    mRecodingView.setVisibility(View.GONE);
    mTextStart.setVisibility(View.VISIBLE);

    mPauseButton.setImageResource(R.drawable.ic_pause_black_24dp);
  }

  @Override public void setScreenOnFlag() {
    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override public void clearScreenOnFlag() {
    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override public void startServiceAndBind() {
    Intent intent = new Intent(mContext, AudioRecordService.class);
    mContext.startService(intent);
    bindToService();
  }

  @Override
  public void bindToService() {
    Intent intent = new Intent(mContext, AudioRecordService.class);
    mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    registerLocalBroadCastReceiver();
  }

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      mIsServiceBound = true;
      mAudioRecordService =
          ((AudioRecordService.ServiceBinder) iBinder).getService();
      Log.i("Tesing", " " + mAudioRecordService.isRecording() + " recording");
      audioRecordPresenter.onServiceStatusAvailable(mAudioRecordService.isRecording(),
          mAudioRecordService.isPaused());
    }

    @Override public void onServiceDisconnected(ComponentName componentName) {
    }
  };

  @Override
  public void unbindFromService() {
    unRegisterLocalBroadCastReceiver();
    if (mIsServiceBound) {
      mIsServiceBound = false;
      mContext.unbindService(serviceConnection);
    }
  }

  @Override
  public Disposable subscribeForTimer(Consumer<AudioRecorder.RecordTime> recordTimeConsumer) {
    return mAudioRecordService.subscribeForTimer(recordTimeConsumer);
  }

  private void unRegisterLocalBroadCastReceiver() {
    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(serviceUpdateReceiver);
  }

  private void registerLocalBroadCastReceiver() {
    LocalBroadcastManager.getInstance(mContext)
        .registerReceiver(serviceUpdateReceiver, new IntentFilter(AppConstants.ACTION_IN_SERVICE));
  }

  @Override public void stopServiceAndUnBind() {
    Intent intent = new Intent(mContext, AudioRecordService.class);
    mContext.stopService(intent);
    unbindFromService();
  }

  private final BroadcastReceiver serviceUpdateReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (!intent.hasExtra(AppConstants.ACTION_IN_SERVICE)) return;
      String actionExtra = intent.getStringExtra(AppConstants.ACTION_IN_SERVICE);
      audioRecordPresenter.onServiceUpdateReceived(actionExtra);
    }
  };
}