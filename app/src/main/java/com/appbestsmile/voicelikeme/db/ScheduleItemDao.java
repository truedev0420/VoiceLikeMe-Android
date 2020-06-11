package com.appbestsmile.voicelikeme.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao public interface ScheduleItemDao {
  @Query("Select * from schedules") List<ScheduleItem> getAllSchedules();

  @Insert long insertNewScheduleItem(ScheduleItem scheduleItem);

  @Update int updateScheduleItem(ScheduleItem scheduleItem);

  @Delete int deleteScheduleItem(ScheduleItem scheduleItem);

  @Query("Select count() from schedules") int getCount();
}
