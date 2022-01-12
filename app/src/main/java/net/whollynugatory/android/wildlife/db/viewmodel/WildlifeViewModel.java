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
package net.whollynugatory.android.wildlife.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import net.whollynugatory.android.wildlife.db.entity.CleanUpDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.StatisticsDetails;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeSummary;
import net.whollynugatory.android.wildlife.db.repository.WildlifeRepository;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;

import java.util.List;

public class WildlifeViewModel extends AndroidViewModel {

  private final WildlifeRepository mWildlifeRepository;

  public WildlifeViewModel(Application application) {
    super(application);

    mWildlifeRepository = new WildlifeRepository(application);
  }

  public LiveData<List<CleanUpDetails>> getCleanUpItems() {

    return mWildlifeRepository.getCleanUpItems();
  }

  public LiveData<List<EncounterDetails>> getEncounterDetails(String userId, String encounterId, boolean showSensitive) {

    return mWildlifeRepository.getEncounterDetails(userId, encounterId, showSensitive);
  }

  public LiveData<List<WildlifeSummary>> getMostEncountered(String userId, boolean showSensitive) {

    return mWildlifeRepository.getMostEncountered(userId, showSensitive);
  }

  public LiveData<List<EncounterEntity>> getNewEncounters(String userId, long timeStamp, boolean showSensitive) {

    return mWildlifeRepository.getNewEncounters(userId, timeStamp, showSensitive);
  }

  public LiveData<List<String>> getNewUnique(String userId, long timeStamp, boolean showSensitive) {

    return mWildlifeRepository.getNewUnique(userId, timeStamp, showSensitive);
  }

  public LiveData<StatisticsDetails> getStatisticsDetails(String userId, boolean showSensitive) {

    return mWildlifeRepository.getStatisticsDetails(userId, showSensitive);
  }

  public LiveData<List<TaskEntity>> getTasks() {

    return mWildlifeRepository.getTasks();
  }

  public LiveData<List<EncounterDetails>> getAllEncounterDetails(String userId, boolean showSensitive) {

    return mWildlifeRepository.getAllEncounterDetails(userId, showSensitive);
  }

  public LiveData<List<EncounterDetails>> getFirstEncountered(String userId, boolean showSensitive) {

    return mWildlifeRepository.getFirstEncountered(userId, showSensitive);
  }

  public LiveData<List<WildlifeEntity>> getWildlife() {

    return mWildlifeRepository.getAllWildlife();
  }
}
