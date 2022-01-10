/*
 * Copyright 2022 Ryan Ward
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

import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.CleanUpDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.StatisticsDetails;
import net.whollynugatory.android.wildlife.db.entity.WildlifeSummary;

import java.util.List;

@Dao
public interface EncounterDao {

  @Query("SELECT id AS Id, name AS Name, '" + Utils.TASK_ROOT + "' AS Type FROM task_table " +
    "WHERE id NOT IN (SELECT task_id FROM encounter_table WHERE task_id IS NOT NULL) " +
    "UNION ALL " +
    "SELECT id AS Id, friendly_name AS Name, '" + Utils.WILDLIFE_ROOT + "' AS Type FROM wildlife_table " +
    "WHERE id NOT IN ( SELECT wildlife_id FROM encounter_table WHERE wildlife_id IS NOT NULL)")
  LiveData<List<CleanUpDetails>> cleanUp();

  @Query("DELETE FROM encounter_table")
  void deleteAll();

  @Query("SELECT Encounter.date AS Date, " +
    "Encounter.encounter_id AS EncounterId, " +
    "Encounter.wildlife_id AS WildlifeId, " +
    "Encounter.user_id AS UserId, " +
    "Encounter.id AS Id, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.abbreviation AS WildlifeAbbreviation, " +
    "Wildlife.image_attribution AS ImageAttribution, " +
    "Wildlife.image_src AS ImageUrl, " +
    "Tasks.name AS TaskName, " +
    "Tasks.id AS TaskId, " +
    "Tasks.is_sensitive AS TaskIsSensitive, " +
    "Tasks.description AS TaskDescription " +
    "FROM encounter_table AS Encounter " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id = Encounter.wildlife_id " +
    "INNER JOIN task_table AS Tasks ON Tasks.id = Encounter.task_id " +
    "WHERE user_id == :userId AND (TaskIsSensitive == 0 OR TaskIsSensitive == :showSensitive) " +
    "ORDER BY Date DESC, EncounterId")
  LiveData<List<EncounterDetails>> getAllEncounterDetails(String userId, boolean showSensitive);

  @Query("SELECT Encounter.date AS Date, " +
    "Encounter.encounter_id AS EncounterId, " +
    "Encounter.wildlife_id AS WildlifeId, " +
    "Encounter.user_id AS UserId, " +
    "Encounter.id AS Id, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.abbreviation AS WildlifeAbbreviation, " +
    "Wildlife.image_attribution AS ImageAttribution, " +
    "Wildlife.image_src AS ImageUrl, " +
    "Tasks.name AS TaskName, " +
    "Tasks.id AS TaskId, " +
    "Tasks.is_sensitive AS TaskIsSensitive, " +
    "Tasks.description AS TaskDescription " +
    "FROM encounter_table AS Encounter " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id = Encounter.wildlife_id " +
    "INNER JOIN task_table AS Tasks ON Tasks.id = Encounter.task_id " +
    "WHERE user_id == :userId AND encounter_id = :encounterId AND (TaskIsSensitive == 0 OR TaskIsSensitive == :showSensitive) " +
    "ORDER BY Date DESC, EncounterId")
  LiveData<List<EncounterDetails>> getEncounterDetails(String userId, String encounterId, boolean showSensitive);

  @Query("SELECT wildlife_id AS WildlifeId," +
    "COUNT(encounter_id) AS EncounterCount, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.image_attribution AS ImageAttribution, " +
    "Wildlife.image_src AS ImageUrl " +
    "FROM (" +
    "  SELECT DISTINCT wildlife_id, encounter_id, task_id " +
    "  FROM encounter_table " +
    "  INNER JOIN task_table AS Tasks ON Tasks.id = task_id " +
    "  WHERE user_id == :userId AND (is_sensitive == 0 OR is_sensitive == :showSensitive) " +
    "  GROUP BY wildlife_id, encounter_id" +
    ")" +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id == wildlife_id " +
    "GROUP BY wildlife_id " +
    "ORDER BY EncounterCount DESC")
  LiveData<List<WildlifeSummary>> getMostEncountered(String userId, boolean showSensitive);

  @Query("SELECT * FROM encounter_table " +
    "INNER JOIN task_table AS Tasks ON Tasks.id = task_id " +
    "WHERE user_id == :userId AND date > :timeStamp AND (is_sensitive == 0 OR is_sensitive == :showSensitive)")
  LiveData<List<EncounterEntity>> getNewEncounters(String userId, long timeStamp, boolean showSensitive);

  @Query("SELECT * FROM (" +
    "SELECT wildlife_id " +
    "FROM encounter_table " +
    "INNER JOIN task_table AS Tasks ON Tasks.id = task_id " +
    "WHERE user_id == :userId AND date > :timeStamp AND (is_sensitive == 0 OR is_sensitive == :showSensitive) " +
    "EXCEPT " +
    "SELECT wildlife_id AS Count " +
    "FROM encounter_table " +
    "INNER JOIN task_table AS Tasks ON Tasks.id = task_id " +
    "WHERE user_id == :userId AND date > :timeStamp AND (is_sensitive == 0 OR is_sensitive == :showSensitive))")
  LiveData<List<String>> getNewUnique(String userId, long timeStamp, boolean showSensitive);

  @Query("SELECT COUNT(DISTINCT encounter_id) AS TotalEncounters, " +
    "COUNT(DISTINCT wildlife_id) AS TotalSpeciesEncountered, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = 'd91114ce-6b33-4798-804a-0e0ca6adca0d' AND user_id == :userId) AS TotalBanded, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '5195279b-b057-4d17-aace-82e371d9eb44' AND user_id == :userId) AS TotalForceFed, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '695ec329-b99e-444f-a582-c43a3a35d10c' AND user_id == :userId) AS TotalGavage, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '25f14681-7b98-4a35-8759-ba668c353eb1' AND user_id == :userId) AS TotalHandledEuthanasia, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '1219a543-cafd-4513-8e1e-e41c3be64862' AND user_id == :userId) AS TotalHandledExam, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = 'd27b32b3-8403-4552-8d2b-7723d6777fac' AND user_id == :userId) AS TotalHandledForceFed, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = 'c1ebbb56-4c65-42fb-beb8-c628393ffc3a' AND user_id == :userId) AS TotalHandledGavage, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '175720d7-79d3-4c7c-be33-2dd33db277fb' AND user_id == :userId) AS TotalHandledMedication, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '0525c4b8-37e4-4b95-bdc6-d1df62f7d670' AND user_id == :userId) AS TotalHandledSubcutaneous, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = 'fc0e2bdf-00c0-4da4-855b-857c233effa6' AND user_id == :userId) AS TotalOcularMedicated, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '25a40f6a-fa23-4331-9fd8-ed8d0bfbb780' AND user_id == :userId) AS TotalOralMedicated, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '98bf72f8-f388-4a6a-962e-b3f4cc94f174' AND user_id == :userId) AS TotalSubcutaneous, " +
    "(SELECT COUNT(*) FROM encounter_table WHERE task_id = '88c20461-e306-447c-92bd-196bfbfa9458' AND user_id == :userId) AS TotalSyringeFed, " +
    "(SELECT COUNT(*) FROM encounter_table INNER JOIN task_table AS tasks ON tasks.id == task_id WHERE user_id == :userId AND (tasks.is_sensitive == 0 OR tasks.is_sensitive == :showSensitive)) AS TotalTasks, " +
    "(SELECT WildlifeSpecies " +
    " FROM (" +
    "   SELECT wildlife_id AS WildlifeId, " +
    "     COUNT(encounter_id) AS EncounterCount, " +
    "     Wildlife.friendly_name AS WildlifeSpecies " +
    "   FROM (" +
    "     SELECT DISTINCT wildlife_id, encounter_id " +
    "     FROM encounter_table " +
    "     WHERE user_id == :userId " +
    "     GROUP BY wildlife_id, encounter_id" +
    "   )" +
    " INNER JOIN wildlife_table AS Wildlife ON Wildlife.id == wildlife_id " +
    " GROUP BY wildlife_id " +
    " ORDER BY EncounterCount DESC" +
    " LIMIT 1)" +
    ") AS MostEncountered " +
    "FROM encounter_table " +
    "INNER JOIN wildlife_table AS Wildlife ON Wildlife.id == encounter_table.wildlife_id " +
    "INNER JOIN task_table AS tasks ON tasks.id == task_id " +
    "WHERE user_id == :userId AND (tasks.is_sensitive == 0 OR tasks.is_sensitive == :showSensitive)")
  LiveData<StatisticsDetails> getStatistics(String userId, boolean showSensitive);

  @Query("SELECT MIN(Encounter.date) AS Date, " +
    "Encounter.encounter_id AS EncounterId, " +
    "Encounter.wildlife_id AS WildlifeId, " +
    "Encounter.user_id AS UserId, " +
    "Encounter.id AS Id, " +
    "Wildlife.friendly_name AS WildlifeSpecies, " +
    "Wildlife.abbreviation AS WildlifeAbbreviation, " +
    "Wildlife.image_attribution AS ImageAttribution, " +
    "Wildlife.image_src AS ImageUrl, " +
    "'' AS TaskName, " +
    "'' AS TaskId, " +
    "'' AS TaskIsSensitive, " +
    "'' AS TaskDescription " +
    "FROM encounter_table AS Encounter " +
    "JOIN wildlife_table AS Wildlife ON Wildlife.id == Encounter.wildlife_id " +
    "INNER JOIN task_table AS tasks ON tasks.id == task_id " +
    "WHERE user_id == :userId AND (tasks.is_sensitive == 0 OR tasks.is_sensitive == :showSensitive) " +
    "GROUP BY WildlifeId " +
    "ORDER BY Encounter.date DESC")
  LiveData<List<EncounterDetails>> getFirstEncountered(String userId, boolean showSensitive);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(EncounterEntity encounterEntity);
}
