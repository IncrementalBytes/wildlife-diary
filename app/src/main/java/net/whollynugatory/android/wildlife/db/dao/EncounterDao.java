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
package net.whollynugatory.android.wildlife.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.SummaryDetails;
import net.whollynugatory.android.wildlife.db.entity.WildlifeSummary;

import java.util.List;

@Dao
public interface EncounterDao {

  @Query("SELECT Wildlife.friendly_name AS FriendlyName, " +
    "  Wildlife.id AS WildlifeId, " +
    "  COUNT(Wildlife.abbreviation) AS EncounterCount " +
    "FROM encounter_table " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id == encounter_table.wildlife_id " +
    "WHERE user_id == :userId " +
    "GROUP BY Wildlife.abbreviation " +
    "ORDER BY EncounterCount DESC")
  LiveData<List<WildlifeSummary>> getMostEncountered(String userId);

  @Query("SELECT COUNT(DISTINCT encounter_id) AS TotalEncounters, " +
    "    COUNT(DISTINCT wildlife_id) AS TotalSpeciesEncountered, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%d91114ce-6b33-4798-804a-0e0ca6adca0d%')) AS TotalBanded, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%5195279b-b057-4d17-aace-82e371d9eb44%')) AS TotalForceFed, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%695ec329-b99e-444f-a582-c43a3a35d10c%')) AS TotalGavage, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%25f14681-7b98-4a35-8759-ba668c353eb1%')) AS TotalHandledEuthanasia, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%1219a543-cafd-4513-8e1e-e41c3be64862%')) AS TotalHandledExam, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%d27b32b3-8403-4552-8d2b-7723d6777fac%')) AS TotalHandledForceFed, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%c1ebbb56-4c65-42fb-beb8-c628393ffc3a%')) AS TotalHandledGavage, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%175720d7-79d3-4c7c-be33-2dd33db277fb%')) AS TotalHandledMedication, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%0525c4b8-37e4-4b95-bdc6-d1df62f7d670%')) AS TotalHandledSubcutaneous, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%fc0e2bdf-00c0-4da4-855b-857c233effa6%')) AS TotalOcularMedicated, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%25a40f6a-fa23-4331-9fd8-ed8d0bfbb780%')) AS TotalOralMedicated, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_ids LIKE ('%98bf72f8-f388-4a6a-962e-b3f4cc94f174%')) AS TotalSubcutaneous, " +
    "    (SELECT FriendlyName FROM " +
    "        (SELECT wt.friendly_name AS FriendlyName, wildlife_id, COUNT(*) AS Count " +
    "            FROM encounter_table " +
    "            INNER JOIN wildlife_table AS wt ON wt.id == encounter_table.wildlife_id " +
    "            WHERE user_id == :userId " +
    "            GROUP BY wildlife_id " +
    "            ORDER BY Count DESC) " +
    "            LIMIT 1) AS MostEncountered " +
    "    FROM encounter_table " +
    "    INNER JOIN wildlife_table AS Wildlife ON Wildlife.id == encounter_table.wildlife_id " +
    "    WHERE user_id == :userId")
  LiveData<SummaryDetails> getSummary(String userId);

  @Query("SELECT Encounter.date AS Date, " +
    "Encounter.encounter_id AS EncounterId, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.abbreviation AS WildlifeAbbreviation, " +
    "Encounter.task_ids AS TaskIds " +
    "FROM encounter_table AS Encounter " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id = Encounter.wildlife_id " +
    "WHERE user_id == :userId " +
    "GROUP BY Encounter.encounter_id " +
    "ORDER BY Date DESC, EncounterId")
  LiveData<List<EncounterDetails>> getTotalEncounters(String userId);

  @Query("SELECT Wildlife.friendly_name AS FriendlyName, " +
    "  Wildlife.id AS WildlifeId, " +
    "  COUNT(Wildlife.abbreviation) AS EncounterCount " +
    "FROM encounter_table " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id == encounter_table.wildlife_id " +
    "WHERE user_id == :userId " +
    "GROUP BY Wildlife.abbreviation " +
    "ORDER BY EncounterCount ASC")
  LiveData<List<WildlifeSummary>> getUniqueEncountered(String userId);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<EncounterEntity> encounterEntityList);
}
