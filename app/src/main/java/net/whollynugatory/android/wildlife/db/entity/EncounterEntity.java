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
    @ForeignKey(entity = WildlifeEntity.class, parentColumns = "id", childColumns = "wildlife_id")
  },
  indices = {
    @Index(value = "id"),
    @Index(value = "encounter_id"),
    @Index(value = "wildlife_id")
  }
)
public class EncounterEntity implements Serializable {

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  @Exclude
  public String Id;

  @ColumnInfo(name = "encounter_id")
  public String EncounterId;

  @ColumnInfo(name = "date")
  public long Date;

  @NonNull
  @ColumnInfo(name = "task_ids")
  public String TaskIds;

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
    TaskIds = Utils.UNKNOWN_ID;
    WildlifeId = Utils.UNKNOWN_ID;
    UserId = Utils.UNKNOWN_USER_ID;
  }

  @NonNull
  @Override
  public String toString() {

    return String.format(
      Locale.US,
      "{Date: %s, Id: %s, EncounterId: %s, TaskIds: %s, WildlifeId: %s}",
      Date,
      Id,
      EncounterId,
      TaskIds,
      WildlifeId);
  }

  @Ignore
  public boolean isValid() {

    if (Id.isEmpty() || Id.equals(Utils.UNKNOWN_ID)) {
      return false;
    }

    if (Date < 1) {
      return false;
    }

    if (EncounterId.isEmpty() || EncounterId.equals(Utils.UNKNOWN_ID)) {
      return false;
    }

    if (TaskIds.isEmpty() || TaskIds.contains(Utils.UNKNOWN_ID)) {
      return false;
    }

    if (WildlifeId.isEmpty() || WildlifeId.equals(Utils.UNKNOWN_ID)) {
      return false;
    }

    return !UserId.isEmpty() && !UserId.equals(Utils.UNKNOWN_ID);
  }
}
