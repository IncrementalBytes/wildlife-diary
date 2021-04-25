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
import net.whollynugatory.android.wildlife.db.entity.EncounterSummary;
import net.whollynugatory.android.wildlife.db.entity.SummaryDetails;

import java.util.List;

@Dao
public interface EncounterDao {

  @Query("SELECT Encounter.date AS Date, " +
    "Encounter.user_id AS UserId, " +
    "Encounter.encounter_id AS EncounterId, " +
    "Wildlife.id AS WildlifeId, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.abbreviation AS WildlifeAbbreviation, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == 'd91114ce-6b33-4798-804a-0e0ca6adca0d' AND encounter_id == :encounterId) AS IsBanded," +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '5195279b-b057-4d17-aace-82e371d9eb44' AND encounter_id == :encounterId) AS IsForceFed, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '695ec329-b99e-444f-a582-c43a3a35d10c' AND encounter_id == :encounterId) AS IsGavage, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '25f14681-7b98-4a35-8759-ba668c353eb1' AND encounter_id == :encounterId) AS IsHandledEuthanasia, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '1219a543-cafd-4513-8e1e-e41c3be64862' AND encounter_id == :encounterId) AS IsHandledExam, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == 'd27b32b3-8403-4552-8d2b-7723d6777fac' AND encounter_id == :encounterId) AS IsHandledForceFed, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == 'c1ebbb56-4c65-42fb-beb8-c628393ffc3a' AND encounter_id == :encounterId) AS IsHandledGavage, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '175720d7-79d3-4c7c-be33-2dd33db277fb' AND encounter_id == :encounterId) AS IsHandledMedication, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '0525c4b8-37e4-4b95-bdc6-d1df62f7d670' AND encounter_id == :encounterId) AS IsHandledSubcutaneous, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == 'fc0e2bdf-00c0-4da4-855b-857c233effa6' AND encounter_id == :encounterId) AS IsOcularMedication, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '25a40f6a-fa23-4331-9fd8-ed8d0bfbb780' AND encounter_id == :encounterId) AS IsOralMedication, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id == '98bf72f8-f388-4a6a-962e-b3f4cc94f174' AND encounter_id == :encounterId) AS IsSubcutaneous, " +
    "(SELECT description FROM task_table WHERE id == 'd91114ce-6b33-4798-804a-0e0ca6adca0d') AS DescriptionBanded , " +
    "(SELECT description FROM task_table WHERE id == '5195279b-b057-4d17-aace-82e371d9eb44') AS DescriptionForceFed, " +
    "(SELECT description FROM task_table WHERE id == '695ec329-b99e-444f-a582-c43a3a35d10c') AS DescriptionGavage, " +
    "(SELECT description FROM task_table WHERE id == '25f14681-7b98-4a35-8759-ba668c353eb1') AS DescriptionHandledEuthanasia, " +
    "(SELECT description FROM task_table WHERE id == '1219a543-cafd-4513-8e1e-e41c3be64862') AS DescriptionHandledExam, " +
    "(SELECT description FROM task_table WHERE id == 'd27b32b3-8403-4552-8d2b-7723d6777fac') AS DescriptionHandledForceFed, " +
    "(SELECT description FROM task_table WHERE id == 'c1ebbb56-4c65-42fb-beb8-c628393ffc3a') AS DescriptionHandledGavage, " +
    "(SELECT description FROM task_table WHERE id == '175720d7-79d3-4c7c-be33-2dd33db277fb') AS DescriptionHandledMedication, " +
    "(SELECT description FROM task_table WHERE id == '0525c4b8-37e4-4b95-bdc6-d1df62f7d670') AS DescriptionHandledSubcutaneous, " +
    "(SELECT description FROM task_table WHERE id == 'fc0e2bdf-00c0-4da4-855b-857c233effa6') AS DescriptionOcularMedication, " +
    "(SELECT description FROM task_table WHERE id == '25a40f6a-fa23-4331-9fd8-ed8d0bfbb780') AS DescriptionOralMedication, " +
    "(SELECT description FROM task_table WHERE id == '98bf72f8-f388-4a6a-962e-b3f4cc94f174') AS DescriptionSubcutaneous " +
    "FROM encounter_table AS Encounter " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id = Encounter.wildlife_id " +
    "INNER JOIN task_table AS Tasks ON Tasks.id = Encounter.task_id " +
    "WHERE Encounter.encounter_id == :encounterId " +
    "GROUP BY Encounter.encounter_id")
  LiveData<EncounterDetails> getEncounterDetailsById(String encounterId);

  @Query("SELECT Encounter.date AS Date, " +
    "Encounter.user_id AS UserId, " +
    "Encounter.encounter_id AS EncounterId, " +
    "Wildlife.id AS WildlifeId, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.abbreviation AS WildlifeAbbreviation " +
    "FROM encounter_table AS Encounter " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id = Encounter.wildlife_id " +
    "WHERE user_id == :userId " +
    "GROUP BY Encounter.encounter_id " +
    "ORDER BY Date DESC " +
    "LIMIT 50")
  LiveData<List<EncounterSummary>> getEncounterSummaries(String userId);

  @Query("SELECT COUNT(DISTINCT encounter_id) AS TotalEncounters, " +
    "    COUNT(DISTINCT wildlife_id) AS TotalSpeciesEncountered, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'd91114ce-6b33-4798-804a-0e0ca6adca0d') AS TotalBanded, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '5195279b-b057-4d17-aace-82e371d9eb44') AS TotalForceFed, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '695ec329-b99e-444f-a582-c43a3a35d10c') AS TotalGavage, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '25f14681-7b98-4a35-8759-ba668c353eb1') AS TotalHandledEuthanasia, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '1219a543-cafd-4513-8e1e-e41c3be64862') AS TotalHandledExam, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'd27b32b3-8403-4552-8d2b-7723d6777fac') AS TotalHandledForceFed, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'c1ebbb56-4c65-42fb-beb8-c628393ffc3a') AS TotalHandledGavage, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '175720d7-79d3-4c7c-be33-2dd33db277fb') AS TotalHandledMedication, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '0525c4b8-37e4-4b95-bdc6-d1df62f7d670') AS TotalHandledSubcutaneous, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'fc0e2bdf-00c0-4da4-855b-857c233effa6') AS TotalOcularMedicated, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '25a40f6a-fa23-4331-9fd8-ed8d0bfbb780') AS TotalOralMedicated, " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '98bf72f8-f388-4a6a-962e-b3f4cc94f174') AS TotalSubcutaneous, " +
    "    (SELECT FriendlyName FROM " +
    "        (SELECT wt.friendly_name AS FriendlyName, wildlife_id, COUNT(*) AS Count " +
    "            FROM encounter_table " +
    "            INNER JOIN wildlife_table AS wt ON wt.id == encounter_table.wildlife_id " +
    "            GROUP BY wildlife_id " +
    "            ORDER BY Count DESC) " +
    "            LIMIT 1) AS MostEncountered " +
    "    FROM encounter_table " +
    "    INNER JOIN wildlife_table AS Wildlife ON Wildlife.id == encounter_table.wildlife_id")
  LiveData<SummaryDetails> getSummaryDetails();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(EncounterEntity encounterEntity);
}
