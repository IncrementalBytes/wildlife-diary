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
package net.whollynugatory.android.wildlife.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import net.whollynugatory.android.wildlife.db.entity.SummaryDetails;
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

  public void deleteByEncounterId(String encounterId) {

    mWildlifeRepository.deleteByEncounterId(encounterId);
  }

  public LiveData<List<EncounterDetails>> getEncounterDetails(String userId, String encounterId) {

    return mWildlifeRepository.getEncounterDetails(userId, encounterId);
  }

  public LiveData<List<EncounterDetails>> getEncountersByTaskName(String userId, String taskName) {

    return mWildlifeRepository.getEncountersByTaskName(userId, taskName);
  }

  public LiveData<List<EncounterDetails>> getTotalEncounters(String userId) {

    return mWildlifeRepository.getTotalEncounters(userId);
  }

  public LiveData<SummaryDetails> getSummary(String userId) {

    return mWildlifeRepository.getSummary(userId);
  }

  public LiveData<List<WildlifeSummary>> getMostEncountered(String userId) {

    return mWildlifeRepository.getMostEncountered(userId);
  }

  public LiveData<List<TaskEntity>> getTasks() {

    return mWildlifeRepository.getTasks();
  }

  public LiveData<List<WildlifeSummary>> getUniqueEncountered(String userId) {

    return mWildlifeRepository.getUniqueEncountered(userId);
  }

  public LiveData<List<WildlifeEntity>> getWildlife() {

    return mWildlifeRepository.getAllWildlife();
  }
}
