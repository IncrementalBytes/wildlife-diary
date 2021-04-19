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
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.EncounterSummary;
import net.whollynugatory.android.wildlife.db.entity.SummaryDetails;

import java.util.List;

public class EncounterRepository {

  private static final String TAG = Utils.BASE_TAG + EncounterRepository.class.getSimpleName();

  private final EncounterDao mDao;
  private final LiveData<List<EncounterSummary>> mEncounterSummaryList;

  public EncounterRepository(Application application) {

    Log.d(TAG, "++EncounterRepository(Application)");
    WildlifeDatabase db = WildlifeDatabase.getInstance(application);
    mDao = db.encounterDao();
    mEncounterSummaryList = mDao.getEncounterSummaries();
  }

  public LiveData<EncounterDetails> getEncounterDetailsById(String encounterId) {

    return mDao.getEncounterDetailsById(encounterId);
  }

  public LiveData<List<EncounterSummary>> getEncounterSummaries() {

    return mEncounterSummaryList;
  }

  public LiveData<SummaryDetails> getSummaryDetails() {

    return mDao.getSummaryDetails();
  }

  public void insert(EncounterEntity encounterEntity) {

    Log.d(TAG, encounterEntity.toString());
    WildlifeDatabase.databaseWriteExecutor.execute(() -> mDao.insert(encounterEntity));
  }
}
