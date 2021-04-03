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
package net.whollynugatory.android.wildlife.db.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.WildlifeDatabase;
import net.whollynugatory.android.wildlife.db.dao.EncounterDao;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;

import java.util.List;

public class EncounterRepository {

  private static final String TAG = Utils.BASE_TAG + EncounterRepository.class.getSimpleName();

  private final EncounterDao mDao;
  private final LiveData<Integer> mCount;
  private final LiveData<List<EncounterEntity>> mAllEncounters;
//  private final LiveData<List<EncounterWithTasks>> mEncounterWithTasks;

  public EncounterRepository(Application application) {

    Log.d(TAG, "++EncounterRepository(Application)");
    WildlifeDatabase db = WildlifeDatabase.getInstance(application);
    mDao = db.encounterDao();
    mCount = mDao.count();
    mAllEncounters = mDao.getAll();
//    mEncounterWithTasks = mDao.getEncounterWithTasks();
  }

  public LiveData<Integer> count() {

    return mCount;
  }

  public LiveData<List<EncounterEntity>> getAll() {

    return mAllEncounters;
  }

//  public LiveData<List<EncounterWithTasks>> getEncounterWithTasks() {
//
//    return mEncounterWithTasks;
//  }

  public void insert(EncounterEntity encounterEntity) {

    Log.d(TAG, encounterEntity.toString());
    WildlifeDatabase.databaseWriteExecutor.execute(() -> mDao.insert(encounterEntity));
  }

  public void insertAll(List<EncounterEntity> encounterEntityList) {

    WildlifeDatabase.databaseWriteExecutor.execute(() -> mDao.insertAll(encounterEntityList));
  }
}
