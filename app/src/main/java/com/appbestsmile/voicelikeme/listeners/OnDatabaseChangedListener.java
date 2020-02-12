package com.appbestsmile.voicelikeme.listeners;

public interface OnDatabaseChangedListener {
  void onNewDatabaseEntryAdded();

  void onDatabaseEntryRenamed();
}