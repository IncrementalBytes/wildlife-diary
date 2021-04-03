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
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.db.repository.EncounterDetailRepository;
import net.whollynugatory.android.wildlife.db.repository.EncounterRepository;
import net.whollynugatory.android.wildlife.db.repository.TaskRepository;
import net.whollynugatory.android.wildlife.db.repository.WildlifeRepository;
import net.whollynugatory.android.wildlife.db.view.EncounterDetails;

import java.util.List;

public class WildlifeViewModel extends AndroidViewModel {

  private final EncounterRepository mEncounterRepository;
  private final EncounterDetailRepository mEncounterDetailRepository;
  private final TaskRepository mTaskRepository;
  private final WildlifeRepository mWildlifeRepository;

  private final LiveData<List<EncounterEntity>> mAllEncounters;
  private final LiveData<List<TaskEntity>> mAllTasks;
  private final LiveData<List<WildlifeEntity>> mAllWildlife;
  private final LiveData<Integer> mEncountersCount;
  private final LiveData<List<EncounterDetails>> mEncounterDetails;
  private final LiveData<Integer> mTasksCount;
  private final LiveData<Integer> mWildlifeCount;
  private final LiveData<List<String>> mTaskNames;
  private final LiveData<List<String>> mAbbreviations;
  private final LiveData<List<String>> mWildlifeNames;
  private final LiveData<List<EncounterDetails>> mRecentDetails;
  private final LiveData<List<EncounterDetails>> mRecentDetailsGrouped;

  public WildlifeViewModel(Application application) {
    super(application);

    mEncounterRepository = new EncounterRepository(application);
    mEncounterDetailRepository = new EncounterDetailRepository(application);
    mTaskRepository = new TaskRepository(application);
    mWildlifeRepository = new WildlifeRepository(application);

    mAllEncounters = mEncounterRepository.getAll();
    mAllTasks = mTaskRepository.getAll();
    mAllWildlife = mWildlifeRepository.getAll();
    mEncountersCount = mEncounterRepository.count();
    mEncounterDetails = mEncounterDetailRepository.getRecent();
    mTasksCount = mTaskRepository.count();
    mWildlifeCount = mWildlifeRepository.count();
    mTaskNames = mTaskRepository.getNames();
    mAbbreviations = mWildlifeRepository.getAbbreviations();
    mWildlifeNames = mWildlifeRepository.getNames();
    mRecentDetails = mEncounterDetailRepository.getRecent();
    mRecentDetailsGrouped = mEncounterDetailRepository.getRecentAndGroupByEncounter();
  }

  public LiveData<Integer> encounterCount() {

    return mEncountersCount;
  }

  public LiveData<List<EncounterDetails>> getEncounterDetails(String encounterId) {

    return mEncounterDetailRepository.getByEncounterId(encounterId);
  }

  public LiveData<List<EncounterDetails>> getRecentEncounterDetails() {

    return mRecentDetails;
  }

  public LiveData<List<EncounterDetails>> getRecentEncounterDetailsGrouped() {

    return mRecentDetailsGrouped;
  }

  public LiveData<List<EncounterEntity>> getEncounters() {

    return mAllEncounters;
  }

  public LiveData<List<String>> getTaskNames() {

    return mTaskNames;
  }

  public LiveData<List<TaskEntity>> getTasks() {

    return mAllTasks;
  }

  public LiveData<List<WildlifeEntity>> getWildlife() {

    return mAllWildlife;
  }

  public LiveData<List<String>> getWildlifeAbbreviations() {

    return mAbbreviations;
  }

  public LiveData<List<String>> getWildlifeNames() {

    return mWildlifeNames;
  }

  public void insertEncounter(EncounterEntity encounterEntity) {

    mEncounterRepository.insert(encounterEntity);
  }

  public void insertTask(TaskEntity taskEntity) {

    mTaskRepository.insert(taskEntity);
  }

  public void insertWildlife(WildlifeEntity wildlifeEntity) {

    mWildlifeRepository.insert(wildlifeEntity);
  }

  public LiveData<Integer> taskCount() {

    return mTasksCount;
  }

  public LiveData<Integer> wildlifeCount() {

    return mWildlifeCount;
  }
}
