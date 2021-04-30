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

import androidx.room.Ignore;

import net.whollynugatory.android.wildlife.Utils;

import java.util.HashMap;

public class EncounterSummary {

  public long Date;

  public String EncounterId;

  @Ignore
  public HashMap<String, TaskEntity> Tasks;

  public String TaskId;

  public boolean TaskIsSensitive;

  public String TaskName;

  public String TaskDescription;

  public String WildlifeAbbreviation;

  public String WildlifeSpecies;


  public EncounterSummary() {

    Date = 0;
    EncounterId = Utils.UNKNOWN_ID;
    TaskDescription = Utils.UNKNOWN_STRING;
    TaskId = Utils.UNKNOWN_ID;
    TaskIsSensitive = false;
    TaskName = Utils.UNKNOWN_STRING;
    Tasks = new HashMap<>();
    WildlifeAbbreviation = Utils.UNKNOWN_STRING;
    WildlifeSpecies = Utils.UNKNOWN_STRING;
  }
}
