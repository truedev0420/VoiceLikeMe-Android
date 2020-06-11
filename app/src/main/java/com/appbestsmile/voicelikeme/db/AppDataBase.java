package com.appbestsmile.voicelikeme.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = { RecordingItem.class, ScheduleItem.class }, version = 1, exportSchema = false)

public abstract class AppDataBase extends RoomDatabase {
  public abstract RecordItemDao recordItemDao();
  public abstract ScheduleItemDao scheduleItemDao();

  private static final String DATABASE_NAME = "saved_recordings.db";

  private static AppDataBase appDataBaseInstance;
  private RecordItemDataSource recordItemDataSource;
  private ScheduleItemDataSource scheduleItemDataSource;

  public static AppDataBase getInstance(Context context) {
    if (appDataBaseInstance == null) {
      appDataBaseInstance = Room.databaseBuilder(context, AppDataBase.class, DATABASE_NAME).build();
      appDataBaseInstance.recordItemDataSource =
          new RecordItemDataSource(appDataBaseInstance.recordItemDao());

      appDataBaseInstance.scheduleItemDataSource =
              new ScheduleItemDataSource(appDataBaseInstance.scheduleItemDao());

    } return appDataBaseInstance;
  }

  public RecordItemDataSource getRecordItemDataSource() {
    return recordItemDataSource;
  }
  public ScheduleItemDataSource getScheduleItemDataSource() { return scheduleItemDataSource; }
}
