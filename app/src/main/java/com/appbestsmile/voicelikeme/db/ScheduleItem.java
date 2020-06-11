package com.appbestsmile.voicelikeme.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "schedules")
public class ScheduleItem implements Parcelable {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String name;
  private byte isPlay;
  private byte isRepeat;
  private String weekdays;
  private String time;

  public ScheduleItem() {
  }

  private ScheduleItem(Parcel in) {
    name = in.readString();
    isPlay = in.readByte();
    isRepeat = in.readByte();
    weekdays = in.readString();
    time = in.readString();
  }

  public String getName(){ return name; }

  public void setName(String name) { this.name = name; }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public byte getIsRepeat() {
    return isRepeat;
  }

  public void setIsRepeat(byte isRepeat) {
    this.isRepeat = isRepeat;
  }

  public byte getIsPlay() {
    return isPlay;
  }

  public void setIsPlay(byte isPlay) { this.isPlay = isPlay; }

  public void setWeekdays(String weekdays) { this.weekdays = weekdays; }

  public String getWeekdays() { return weekdays;  }

  public void setTime(String time){ this.time = time; }

  public String getTime() { return time; }

  public static final Creator<ScheduleItem> CREATOR =
      new Creator<ScheduleItem>() {
        public ScheduleItem createFromParcel(Parcel in) {
          return new ScheduleItem(in);
        }

        public ScheduleItem[] newArray(int size) {
          return new ScheduleItem[size];
        }
      };

  @Override public void writeToParcel(Parcel dest, int flags) {
    //dest.writeInt(mId);
    dest.writeString(name);
    dest.writeByte(isPlay);
    dest.writeByte(isRepeat);
    dest.writeString(weekdays);
    dest.writeString(time);
  }

  @Override public int describeContents() {
    return 0;
  }
}