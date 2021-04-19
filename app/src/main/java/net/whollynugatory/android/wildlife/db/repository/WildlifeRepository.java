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
import net.whollynugatory.android.wildlife.db.dao.WildlifeDao;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;

import java.util.List;

public class WildlifeRepository {

  private static final String TAG = Utils.BASE_TAG + WildlifeRepository.class.getSimpleName();

  private final WildlifeDao mDao;
  private final LiveData<List<WildlifeEntity>> mAllWildlife;

  public WildlifeRepository(Application application) {

    Log.d(TAG, "++WildlifeRepository(Application)");
    WildlifeDatabase db = WildlifeDatabase.getInstance(application);
    mDao = db.wildlifeDao();
    mAllWildlife = mDao.getAll();
  }

  public LiveData<List<WildlifeEntity>> getAll() {

    return mAllWildlife;
  }

  public void insert(WildlifeEntity wildlifeEntity) {

    WildlifeDatabase.databaseWriteExecutor.execute(() -> mDao.insert(wildlifeEntity));
  }
}
