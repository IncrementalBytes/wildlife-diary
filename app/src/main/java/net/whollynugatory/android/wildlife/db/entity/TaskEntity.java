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
import androidx.room.PrimaryKey;

import net.whollynugatory.android.wildlife.Utils;

import java.io.Serializable;
import java.util.Locale;

@Entity(tableName = "task_table")
public class TaskEntity implements Serializable {

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  public String Id;

  @NonNull
  @ColumnInfo(name = "name")
  public String Name;

  @ColumnInfo(name = "description")
  public String Description;

  public TaskEntity() {

    Id = Utils.UNKNOWN_ID;
    Name = Utils.UNKNOWN_STRING;
    Description = Utils.UNKNOWN_STRING;
  }

  @NonNull
  @Override
  public String toString() {

    return String.format(Locale.US, "{Id: %s, Name: %s}|", Id, Name);
  }
}
