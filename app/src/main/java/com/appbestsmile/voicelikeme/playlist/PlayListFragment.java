package com.appbestsmile.voicelikeme.playlist;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.FileObserver;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.alarm_manager.AlarmManagerDialog;
import com.appbestsmile.voicelikeme.db.RecordingItem;
import com.appbestsmile.voicelikeme.mvpbase.BaseFragment;
import com.appbestsmile.voicelikeme.recordingservice.Constants;
import com.appbestsmile.voicelikeme.theme.ThemeHelper;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.inject.Inject;


public class PlayListFragment extends BaseFragment implements PlayListMVPView {
  private static final String LOG_TAG = "PlayListFragment";

  private int MAX_REPLAY_LENGTH = 2;

  @Inject
  public PlayListAdapter mPlayListAdapter;

  @Inject
  public PlayListPresenter<PlayListMVPView> playListPresenter;

  private RecyclerView mRecordingsListView;
  private TextView emptyListLabel;
  private MediaPlayer mMediaPlayer;

  private String mNotiVoiceName;
  private String mNotiVoicePath;


  private RecordingItem currentRecordingItem;

  public static PlayListFragment newInstance() {
    return new PlayListFragment();
  }

  public void setNotiData(String voice_name, String voice_path){
    mNotiVoiceName = voice_name;
    mNotiVoicePath = voice_path;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    playListPresenter.onAttach(this);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);
    initViews(v);
    mMediaPlayer = new MediaPlayer();
    return v;
  }

  private void initViews(View v) {
    emptyListLabel = v.findViewById(R.id.empty_list_label);
    mRecordingsListView = v.findViewById(R.id.recyclerView);
    mRecordingsListView.setHasFixedSize(true);
    LinearLayoutManager llm = new LinearLayoutManager(getActivity());
    llm.setOrientation(LinearLayoutManager.VERTICAL);

    //newest to oldest order (database stores from oldest to newest)
    llm.setReverseLayout(true);
    llm.setStackFromEnd(true);

    mRecordingsListView.setLayoutManager(llm);
    //mRecordingsListView.setItemAnimator(new DefaultItemAnimator());
    mRecordingsListView.setAdapter(mPlayListAdapter);
    playListPresenter.onViewInitialised();


    if(mNotiVoiceName != null){
      playListPresenter.onListItemLongClick(mNotiVoiceName);
    }
  }

  private final FileObserver observer = new FileObserver(
      android.os.Environment.getExternalStorageDirectory().toString() + "/SoundRecorder") {
    // set up a file observer to watch this directory on sd card
    @Override public void onEvent(int event, String file) {
      if (event == FileObserver.DELETE) {
        // user deletes a recording file out of the app

        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
            + "/SoundRecorder"
            + file
            + "]";

        Log.d(LOG_TAG, "File deleted ["
            + android.os.Environment.getExternalStorageDirectory().toString()
            + "/SoundRecorder"
            + file
            + "]");

        // remove file from database and recyclerview
        mPlayListAdapter.removeOutOfApp(filePath);
      }
    }
  };

  @Override public void refreshTheme(ThemeHelper themeHelper) {

  }

  @Override public void onDestroy() {
    playListPresenter.onDetach();
    super.onDestroy();
  }

  @Override public void notifyListAdapter() {
    mPlayListAdapter.notifyDataSetChanged();
  }

  @Override public void setRecordingListVisible() {
    mRecordingsListView.setVisibility(View.VISIBLE);
  }

  @Override public void setRecordingListInVisible() {
    mRecordingsListView.setVisibility(View.GONE);
  }

  @Override public void setEmptyLabelVisible() {
    emptyListLabel.setVisibility(View.VISIBLE);
  }

  @Override public void setEmptyLabelInVisible() {
    emptyListLabel.setVisibility(View.GONE);
  }

  @Override public void startWatchingForFileChanges() {
    observer.startWatching();
  }

  @Override public void stopWatchingForFileChanges() {
    observer.stopWatching();
  }

  private int positionOfCurrentViewHolder = -1;
  private PlayListAdapter.RecordingsViewHolder recordingsViewHolder;

  Handler uiThreadHandler = new Handler();

  @Override public void updateProgressInListItem(Integer position) {
    if (position != positionOfCurrentViewHolder || recordingsViewHolder == null) {
      positionOfCurrentViewHolder = position;
      recordingsViewHolder =
          (PlayListAdapter.RecordingsViewHolder) mRecordingsListView.findViewHolderForAdapterPosition(
              position);
    }
    if (recordingsViewHolder != null && recordingsViewHolder.getAdapterPosition() == position) {
      uiThreadHandler.post(() -> recordingsViewHolder.updateProgressInSeekBar(position));
    } else {

      positionOfCurrentViewHolder = -1;
      recordingsViewHolder = null;

      Log.d("PlayListFragment", "ready to stop");
    }
  }

  @Override public void updateTimerInListItem(int position) {
    if (recordingsViewHolder != null) {
      uiThreadHandler.post(() -> recordingsViewHolder.updatePlayTimer(position));
    }
  }

  @Override public void notifyListItemChange(Integer position) {
    mPlayListAdapter.notifyItemChanged(position);
  }

  @Override public void showError(String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
  }

  @Override public void notifyListItemRemove(Integer position) {
    mPlayListAdapter.notifyItemRemoved(position);
  }

  @Override public void showFileOptionDialog(int position, RecordingItem recordingItem) {
    ArrayList<String> fileOptions = new ArrayList<>();
    fileOptions.add(getString(R.string.dialog_file_share));
    fileOptions.add(getString(R.string.dialog_file_rename));
    fileOptions.add(getString(R.string.dialog_file_delete));
    fileOptions.add(getString(R.string.dialog_file_replay));
    fileOptions.add(getString(R.string.dialog_file_schedule));

    final CharSequence[] items = fileOptions.toArray(new CharSequence[fileOptions.size()]);

    // File delete confirm
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(getString(R.string.dialog_title_options));
    builder.setItems(items, (dialog, listItem) -> {
      switch (listItem) {
        case 0:
          playListPresenter.shareFileClicked(position);
          break;
        case 1:
          playListPresenter.renameFileClicked(position);
          break;
        case 2:
          playListPresenter.deleteFileClicked(position);
          break;
        case 3:
          playListPresenter.replayFileClicked(position);
          break;
        case 4:
          playListPresenter.scheduleFileClicked(position);
          break;
      }
    });

    builder.setCancelable(true);
    builder.setNegativeButton(getString(R.string.dialog_action_cancel),
        (dialog, id) -> dialog.cancel());

    AlertDialog alert = builder.create();
    alert.show();
  }

  @Override
  public void shareFileDialog(String filePath) {

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());

    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_STREAM,
        Uri.fromFile(new File(filePath)));
    shareIntent.setType("*/*");
    getActivity().startActivity(Intent.createChooser(shareIntent, getString(R.string.send_to)));
  }

  @Override
  public void showRenameFileDialog(int position) {
    AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(getActivity());
    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_rename_file, null);
    final EditText input = view.findViewById(R.id.new_name);
    renameFileBuilder.setTitle(getString(R.string.dialog_title_rename));
    renameFileBuilder.setCancelable(true);
    renameFileBuilder.setPositiveButton(getString(R.string.dialog_action_ok),
        (dialog, id) -> {
          String value = input.getText().toString().trim() + Constants.AUDIO_RECORDER_FILE_EXT_WAV;
          playListPresenter.renameFile(position, value);
          dialog.cancel();
        });
    renameFileBuilder.setNegativeButton(getActivity().getString(R.string.dialog_action_cancel),
        (dialog, id) -> dialog.cancel());
    renameFileBuilder.setView(view);
    AlertDialog alert = renameFileBuilder.create();
    alert.show();
  }

  @Override
  public void showReplayFileDialog(int position) {
    AlertDialog.Builder replayFileBuilder = new AlertDialog.Builder(getActivity());
    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_replay_file, null);
    final EditText input = view.findViewById(R.id.replay_count);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_REPLAY_LENGTH)});
    input.setHint(getString(R.string.dialog_replay_hint));

    replayFileBuilder.setTitle(getString(R.string.dialog_title_replay));
    replayFileBuilder.setCancelable(true);
    replayFileBuilder.setPositiveButton(getString(R.string.dialog_action_ok),
            (dialog, id) -> {

              try{
                int value = Integer.parseInt(input.getText().toString().trim());
                playListPresenter.replayFile(position, value);
              }catch(Exception e){
                Log.d("Parsing Exception : ", e.toString());
              }
              dialog.cancel();
            });

    replayFileBuilder.setNegativeButton(getActivity().getString(R.string.dialog_action_cancel),
            (dialog, id) -> dialog.cancel());
    replayFileBuilder.setView(view);
    AlertDialog alert = replayFileBuilder.create();
    alert.show();
  }

  @Override
  public void showDeleteFileDialog(int position) {
    AlertDialog.Builder confirmDelete = new AlertDialog.Builder(getActivity());
    confirmDelete.setTitle(getString(R.string.dialog_title_delete));
    confirmDelete.setMessage(getString(R.string.dialog_text_delete));
    confirmDelete.setCancelable(true);
    confirmDelete.setPositiveButton(getString(R.string.dialog_action_yes),
        (dialog, id) -> {
          playListPresenter.deleteFile(position);
          dialog.cancel();
        });
    confirmDelete.setNegativeButton(getString(R.string.dialog_action_no),
        (dialog, id) -> dialog.cancel());
    AlertDialog alert = confirmDelete.create();
    alert.show();
  }

  @Override
  public void showScheduleFileDialog(int position) {

    String filePath = playListPresenter.getListItemAt(position).getFilePath();
    String fileName = playListPresenter.getListItemAt(position).getName();

    AlarmManagerDialog alarmManagerDialog = new AlarmManagerDialog(getActivity(), fileName, filePath, position);
    alarmManagerDialog.show();
  }

  @Override public void pauseMediaPlayer(int position) {
    mMediaPlayer.pause();
  }

  @Override public void resumeMediaPlayer(int position) {
    mMediaPlayer.start();
  }

  @Override public void stopMediaPlayer(int currentPlayingItem) {

    if (mMediaPlayer != null) {
      Log.d(LOG_TAG, "Stopping");
      mMediaPlayer.stop();
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  @Override public void startMediaPlayer(int position, RecordingItem recordingItem)
      throws IOException {

    mMediaPlayer = new MediaPlayer();
    mMediaPlayer.setDataSource(recordingItem.getFilePath());
    mMediaPlayer.prepare();
    mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
    mMediaPlayer.setOnCompletionListener(mp -> {

      playListPresenter.mediaPlayerStopped();

      currentRecordingItem.playCount--;

      if(currentRecordingItem.playCount > 0) {
        // Replay current recording item.
        try{
          playListPresenter.onListItemClick(position, currentRecordingItem.playCount);
        }catch(Exception e){
          Log.d(LOG_TAG, e.toString());
        }
      }
    });

    currentRecordingItem = recordingItem;
  }
}




