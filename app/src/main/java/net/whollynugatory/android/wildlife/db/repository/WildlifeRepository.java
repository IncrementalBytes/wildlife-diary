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
import net.whollynugatory.android.wildlife.db.dao.TaskDao;
import net.whollynugatory.android.wildlife.db.dao.WildlifeDao;
import net.whollynugatory.android.wildlife.db.entity.CleanUpDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.StatisticsDetails;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeSummary;

import java.util.List;

public class WildlifeRepository {

  private static final String TAG = Utils.BASE_TAG + WildlifeRepository.class.getSimpleName();

  private final EncounterDao mEncounterDao;
  private final TaskDao mTaskDao;
  private final WildlifeDao mWildlifeDao;

  public WildlifeRepository(Application application) {

    Log.d(TAG, "++WildlifeRepository(Application)");
    WildlifeDatabase db = WildlifeDatabase.getInstance(application);
    mEncounterDao = db.encounterDao();
    mTaskDao = db.taskDao();
    mWildlifeDao = db.wildlifeDao();
  }

  public LiveData<List<CleanUpDetails>> getCleanUpItems() {

    return mEncounterDao.cleanUp();
  }

  public LiveData<List<EncounterDetails>> getEncounterDetails(String userId, String encounterId, boolean showSensitive) {

    return mEncounterDao.getEncounterDetails(userId, encounterId, showSensitive);
  }

  public LiveData<List<WildlifeSummary>> getMostEncountered(String userId, boolean showSensitive) {

    return mEncounterDao.getMostEncountered(userId, showSensitive);
  }

  public LiveData<List<EncounterEntity>> getNewEncounters(String userId, long timeStamp, boolean showSensitive) {

    return mEncounterDao.getNewEncounters(userId, timeStamp, showSensitive);
  }

  public LiveData<List<String>> getNewUnique(String userId, long timeStamp, boolean showSensitive) {

    return mEncounterDao.getNewUnique(userId, timeStamp, showSensitive);
  }

  public LiveData<StatisticsDetails> getStatistics(String userId, boolean showSensitive) {

    return mEncounterDao.getStatistics(userId, showSensitive);
  }

  public LiveData<List<TaskEntity>> getTasks() {

    return mTaskDao.getAll();
  }

  public LiveData<List<EncounterDetails>> getAllEncounterDetails(String userId, boolean showSensitive) {

    return mEncounterDao.getAllEncounterDetails(userId, showSensitive);
  }

  public LiveData<List<EncounterDetails>> getFirstEncountered(String userId, boolean showSensitive) {

    return mEncounterDao.getFirstEncountered(userId, showSensitive);
  }

  public LiveData<List<WildlifeEntity>> getAllWildlife() {

    return mWildlifeDao.getAll();
  }
}
