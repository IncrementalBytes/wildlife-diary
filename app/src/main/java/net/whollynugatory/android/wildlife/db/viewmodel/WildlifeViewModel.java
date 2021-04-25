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

import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.EncounterSummary;
import net.whollynugatory.android.wildlife.db.entity.SummaryDetails;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.db.repository.WildlifeRepository;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;

import java.util.List;

public class WildlifeViewModel extends AndroidViewModel {

  private final WildlifeRepository mWildlifeRepository;

  public WildlifeViewModel(Application application) {
    super(application);

    mWildlifeRepository = new WildlifeRepository(application);
  }

  public LiveData<EncounterDetails> getEncounterDetails(String encounterId) {

    return mWildlifeRepository.getEncounterDetailsById(encounterId);
  }

  public LiveData<List<EncounterSummary>> getEncounterSummaries(String userId) {

    return mWildlifeRepository.getEncounterSummaries(userId);
  }

  public LiveData<SummaryDetails> getSummaryDetails() {

    return mWildlifeRepository.getSummaryDetails();
  }

  public LiveData<List<TaskEntity>> getTasks() {

    return mWildlifeRepository.getAllTasks();
  }

  public LiveData<List<WildlifeEntity>> getWildlife() {

    return mWildlifeRepository.getAllWildlife();
  }

  public void insertEncounter(EncounterEntity encounterEntity) {

    mWildlifeRepository.insert(encounterEntity);
  }

  public void insertTask(TaskEntity taskEntity) {

    mWildlifeRepository.insert(taskEntity);
  }

  public void insertWildlife(WildlifeEntity wildlifeEntity) {

    mWildlifeRepository.insert(wildlifeEntity);
  }
}
