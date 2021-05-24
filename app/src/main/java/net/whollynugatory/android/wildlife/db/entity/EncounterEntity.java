/*
 * Copyright 2021 Ryan Ward
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.whollynugatory.android.wildlife.db.entity;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;

import net.whollynugatory.android.wildlife.Utils;

import java.io.Serializable;
import java.util.Locale;

@Entity(
  tableName = "encounter_table",
  foreignKeys = {
    @ForeignKey(entity = TaskEntity.class, parentColumns = "id", childColumns = "task_id"),
    @ForeignKey(entity = WildlifeEntity.class, parentColumns = "id", childColumns = "wildlife_id")
  },
  indices = {
    @Index(value = "id"),
    @Index(value = "encounter_id"),
    @Index(value = "task_id"),
    @Index(value = "wildlife_id")
  }
)
public class EncounterEntity implements Serializable {

  @Ignore
  @Exclude
  private static final String TAG = Utils.BASE_TAG + EncounterEntity.class.getSimpleName();

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  @Exclude
  public String Id;

  @ColumnInfo(name = "encounter_id")
  public String EncounterId;

  @ColumnInfo(name = "date")
  public long Date;

  @ColumnInfo(name = "number_in_group")
  public int NumberInGroup;

  @NonNull
  @ColumnInfo(name = "task_id")
  public String TaskId;

  @NonNull
  @ColumnInfo(name = "user_id")
  public String UserId;

  @NonNull
  @ColumnInfo(name = "wildlife_id")
  public String WildlifeId;

  public EncounterEntity() {

    Id = Utils.UNKNOWN_ID;
    EncounterId = Utils.UNKNOWN_ID;
    Date = 0;
    NumberInGroup = 0;
    TaskId = Utils.UNKNOWN_ID;
    WildlifeId = Utils.UNKNOWN_ID;
    UserId = Utils.UNKNOWN_USER_ID;
  }

  @NonNull
  @Override
  public String toString() {

    return String.format(
      Locale.US,
      "{Date: %s, Id: %s, EncounterId: %s, TaskId: %s, WildlifeId: %s}",
      Date,
      Id,
      EncounterId,
      TaskId,
      WildlifeId);
  }

  @Ignore
  @Exclude
  public boolean isValid() {

    if (Id.isEmpty() || Id.contains(Utils.UNKNOWN_ID)) {
      Log.e(TAG, "Unique identification missing.");
      return false;
    }

    if (Date < 1) {
      Log.e(TAG, "Date is wrong.");
      return false;
    }

    if (EncounterId.isEmpty() || EncounterId.contains(Utils.UNKNOWN_ID)) {
      Log.e(TAG, "Encounter identification missing.");
      return false;
    }

    if (NumberInGroup < 1) {
      Log.e(TAG, "Number in group is not set.");
      return false;
    }

    if (TaskId.isEmpty() || TaskId.contains(Utils.UNKNOWN_ID)) {
      Log.e(TAG, "Task identification missing.");
      return false;
    }

    if (WildlifeId.isEmpty() || WildlifeId.contains(Utils.UNKNOWN_ID)) {
      Log.e(TAG, "Wildlife identification missing.");
      return false;
    }

    return !UserId.isEmpty() && !UserId.contains(Utils.UNKNOWN_ID);
  }
}
