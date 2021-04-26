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
package net.whollynugatory.android.wildlife;

import android.os.AsyncTask;
import android.util.Log;

import net.whollynugatory.android.wildlife.db.dao.WildlifeDao;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.ui.MainActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class InsertWildlifeAsync extends AsyncTask<Void, Void, Void> {

  private static final String TAG = Utils.BASE_TAG + InsertWildlifeAsync.class.getSimpleName();

  private final WildlifeDao mDao;
  private final List<WildlifeEntity> mWildlifeEntityList;
  private final WeakReference<MainActivity> mWeakReference;

  public InsertWildlifeAsync(MainActivity context, WildlifeDao wildlifeDao, List<WildlifeEntity> wildlifeEntityList) {

    mDao = wildlifeDao;
    mWildlifeEntityList = new ArrayList<>(wildlifeEntityList);
    mWeakReference = new WeakReference<>(context);
  }

  @Override
  protected Void doInBackground(final Void... params) {

    mDao.insertAll(mWildlifeEntityList);
    return null;
  }

  protected void onPostExecute(Void nothingReally) {

    Log.d(TAG, "++onPostExecute()");
    MainActivity activity = mWeakReference.get();
    if (activity == null) {
      Log.e(TAG, "MainActivity is null or detached.");
      return;
    }

    activity.wildlifeInsertionComplete();
  }
}
