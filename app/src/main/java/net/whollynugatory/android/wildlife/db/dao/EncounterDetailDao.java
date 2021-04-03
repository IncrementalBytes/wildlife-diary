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

import net.whollynugatory.android.wildlife.db.view.EncounterDetails;

import java.util.List;

@Dao
public interface EncounterDetailDao {

  @Query("SELECT * FROM EncounterDetails WHERE EncounterId = :encounterId")
  LiveData<List<EncounterDetails>> getByEncounterId(String encounterId);

  @Query("SELECT * FROM EncounterDetails ORDER BY Date DESC LIMIT 50")
  LiveData<List<EncounterDetails>> getRecent();

  @Query("SELECT * FROM EncounterDetails GROUP BY EncounterId ORDER BY Date DESC LIMIT 50")
  LiveData<List<EncounterDetails>> getRecentAndGroupByEncounter();
}
