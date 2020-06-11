package com.appbestsmile.voicelikeme.db;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class ScheduleItemDataSource {
  private ScheduleItemDao scheduleItemDao;

  public ScheduleItemDataSource(ScheduleItemDao scheduleItemDao) {
    this.scheduleItemDao = scheduleItemDao;
  }

  public Single<List<ScheduleItem>> getAllSchedules() {
    return Single.fromCallable(() -> scheduleItemDao.getAllSchedules()).subscribeOn(Schedulers.io());
  }

  public Single<Boolean> insertNewScheduleItemAsync(ScheduleItem scheduleItem) {
    return Single.fromCallable(() -> scheduleItemDao.insertNewScheduleItem(scheduleItem) > 1)
        .subscribeOn(Schedulers.io());
  }

  public long insertNewScheduleItem(ScheduleItem scheduleItem) {
    return scheduleItemDao.insertNewScheduleItem(scheduleItem);
  }

  public Single<Boolean> deleteRecordItemAsync(ScheduleItem scheduleItem) {
    return Single.fromCallable(() -> scheduleItemDao.deleteScheduleItem(scheduleItem) > 1)
        .subscribeOn(Schedulers.io());
  }

  public int deleteRecordItem(ScheduleItem scheduleItem) {
    return scheduleItemDao.deleteScheduleItem(scheduleItem);
  }

  public Single<Boolean> updateRecordItemAsync(ScheduleItem scheduleItem) {
    return Single.fromCallable(() -> scheduleItemDao.updateScheduleItem(scheduleItem) > 1)
        .subscribeOn(Schedulers.io());
  }

  public int updateRecordItem(ScheduleItem scheduleItem) {
    return scheduleItemDao.updateScheduleItem(scheduleItem);
  }

  public int getRecordingsCount() {
    return scheduleItemDao.getCount();
  }
}
