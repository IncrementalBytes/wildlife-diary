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
package net.whollynugatory.android.wildlife.db.view;

import androidx.room.DatabaseView;
import androidx.room.Ignore;

import net.whollynugatory.android.wildlife.Utils;

@DatabaseView(
  "SELECT Encounter.date AS Date, " +
    "Encounter.user_id AS UserId, " +
    "Encounter.encounter_id AS EncounterId, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.abbreviation AS WildlifeAbbreviation, " +
    "Tasks.name AS TaskName, " +
    "Tasks.description AS TaskDescription " +
    "FROM encounter_table AS Encounter " +
    "INNER JOIN task_table AS Tasks ON Tasks.id = Encounter.task_id " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id = Encounter.wildlife_id"
)
public class EncounterDetails {

  public String Date;

  public String EncounterId;

  @Ignore
  public String Id;

  public String TaskDescription;

  public String TaskName;

  public String UserId;

  public String WildlifeAbbreviation;

  public String WildlifeSpecies;

  public EncounterDetails() {

    Id = Utils.UNKNOWN_ID;
    EncounterId = Utils.UNKNOWN_ID;
    WildlifeSpecies = Utils.UNKNOWN_STRING;
    WildlifeAbbreviation = Utils.UNKNOWN_STRING;
    Date = Utils.UNKNOWN_DATE;
    TaskName = Utils.UNKNOWN_STRING;
    TaskDescription = Utils.UNKNOWN_STRING;
    UserId = Utils.UNKNOWN_USER_ID;
  }
}
