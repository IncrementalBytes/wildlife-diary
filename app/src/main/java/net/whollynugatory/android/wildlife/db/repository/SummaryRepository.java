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
import net.whollynugatory.android.wildlife.db.dao.SummaryDao;
import net.whollynugatory.android.wildlife.db.view.SummaryDetails;

public class SummaryRepository {

  private static final String TAG = Utils.BASE_TAG + SummaryRepository.class.getSimpleName();

  private final SummaryDao mDao;

  public SummaryRepository(Application application) {

    Log.d(TAG, "++SummaryRepository(Application)");
    WildlifeDatabase db = WildlifeDatabase.getInstance(application);
    mDao = db.summaryDao();
  }

  public LiveData<SummaryDetails> getRecentSummary(boolean includeSensitive) {

    return includeSensitive ? mDao.getSummary() : mDao.getSummarySanitized();
  }
}