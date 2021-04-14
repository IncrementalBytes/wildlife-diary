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
import androidx.room.Query;

import net.whollynugatory.android.wildlife.db.view.SummaryDetails;

@Dao
public interface SummaryDao {

  @Query("SELECT COUNT(DISTINCT EncounterId) AS TotalEncounters,  " +
    "    COUNT(DISTINCT WildlifeId) AS SpeciesEncountered,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'd91114ce-6b33-4798-804a-0e0ca6adca0d') AS Banded,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '5195279b-b057-4d17-aace-82e371d9eb44') AS ForceFed,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '695ec329-b99e-444f-a582-c43a3a35d10c') AS Gavaged,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '25f14681-7b98-4a35-8759-ba668c353eb1') AS HandledEuthanasia,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '1219a543-cafd-4513-8e1e-e41c3be64862') AS HandledExam,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '175720d7-79d3-4c7c-be33-2dd33db277fb') AS HandledMedication,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '0525c4b8-37e4-4b95-bdc6-d1df62f7d670') AS HandledSubcutaneous,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'fc0e2bdf-00c0-4da4-855b-857c233effa6') AS MedicationOcular,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '25a40f6a-fa23-4331-9fd8-ed8d0bfbb780') AS MedicationOral,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '98bf72f8-f388-4a6a-962e-b3f4cc94f174') AS Subcutaneous,  " +
    "    (SELECT WildlifeSpecies FROM " +
    "        (SELECT WildlifeSpecies, WildlifeId, COUNT(*) AS Count  " +
    "            FROM EncounterDetails  " +
    "            GROUP BY WildlifeId  " +
    "            ORDER BY Count DESC  " +
    "            LIMIT 1)) AS MostEncountered  " +
    "    FROM EncounterDetails")
  LiveData<SummaryDetails> getSummary();

  @Query("SELECT COUNT(DISTINCT EncounterId) AS TotalEncounters,  " +
    "    COUNT(DISTINCT WildlifeId) AS SpeciesEncountered,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'd91114ce-6b33-4798-804a-0e0ca6adca0d') AS Banded,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '5195279b-b057-4d17-aace-82e371d9eb44') AS ForceFed,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '695ec329-b99e-444f-a582-c43a3a35d10c') AS Gavaged,  " +
    "    (SELECT 0) AS HandledEuthanasia,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '1219a543-cafd-4513-8e1e-e41c3be64862') AS HandledExam,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '175720d7-79d3-4c7c-be33-2dd33db277fb') AS HandledMedication,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '0525c4b8-37e4-4b95-bdc6-d1df62f7d670') AS HandledSubcutaneous,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == 'fc0e2bdf-00c0-4da4-855b-857c233effa6') AS MedicationOcular,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '25a40f6a-fa23-4331-9fd8-ed8d0bfbb780') AS MedicationOral,  " +
    "    (SELECT COUNT(*) FROM encounter_table WHERE task_id == '98bf72f8-f388-4a6a-962e-b3f4cc94f174') AS Subcutaneous,  " +
    "    (SELECT WildlifeSpecies FROM " +
    "        (SELECT WildlifeSpecies, WildlifeId, COUNT(*) AS Count  " +
    "            FROM EncounterDetails  " +
    "            GROUP BY WildlifeId  " +
    "            ORDER BY Count DESC  " +
    "            LIMIT 1)) AS MostEncountered  " +
    "    FROM EncounterDetails WHERE IsSensitive = 0")
  LiveData<SummaryDetails> getSummarySanitized();
}
