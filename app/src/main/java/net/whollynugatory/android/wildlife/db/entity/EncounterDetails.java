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

import net.whollynugatory.android.wildlife.Utils;

import java.io.Serializable;
import java.util.Locale;

/**
 * Class defining what tasks were performed during an encounter.
 */
public class EncounterDetails implements Serializable {

  public long Date;

  public String EncounterId;

  public String Id;

  public String ImageAttribution;

  public String ImageUrl;

  public String TaskDescription;

  public String TaskId;

  public boolean TaskIsSensitive;

  public String TaskName;

  public String UserId;

  public String WildlifeAbbreviation;

  public String WildlifeId;

  public String WildlifeSpecies;


  public EncounterDetails() {

    Date = 0;
    EncounterId = Utils.UNKNOWN_ID;
    ImageAttribution = Utils.DEFAULT_ATTRIBUTION;
    ImageUrl = Utils.UNKNOWN_STRING;
    Id = Utils.UNKNOWN_ID;
    TaskDescription = Utils.UNKNOWN_STRING;
    TaskId = Utils.UNKNOWN_ID;
    TaskIsSensitive = false;
    TaskName = Utils.UNKNOWN_STRING;
    UserId = Utils.UNKNOWN_USER_ID;
    WildlifeAbbreviation = Utils.UNKNOWN_STRING;
    WildlifeId = Utils.UNKNOWN_ID;
    WildlifeSpecies = Utils.UNKNOWN_STRING;
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
}
