package com.appbestsmile.voicelikeme.playlist;

import com.appbestsmile.voicelikeme.db.RecordingItem;
import com.appbestsmile.voicelikeme.mvpbase.IMVPPresenter;

public interface PlayListPresenter<V extends PlayListMVPView> extends IMVPPresenter<V> {
  void onViewInitialised();

  void renameFile(int position, String value);

  void deleteFile(int position);

  RecordingItem getListItemAt(int position);

  void onListItemClick(int position);

  void onListItemClick(int position, int playCount);

  void onListItemLongClick(int position);

  void onListItemLongClick(String voice_name);

  int getListItemCount();

  void shareFileClicked(int position);

  void renameFileClicked(int position);

  void deleteFileClicked(int position);

  void mediaPlayerStopped();

  void replayFileClicked(int position);

  void replayFile(int position, int value);

  void scheduleFileClicked(int position);
}
