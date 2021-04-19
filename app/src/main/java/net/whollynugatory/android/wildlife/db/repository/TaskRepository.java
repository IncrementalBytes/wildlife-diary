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
import net.whollynugatory.android.wildlife.db.dao.TaskDao;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;

import java.util.List;

public class TaskRepository {

  private static final String TAG = Utils.BASE_TAG + TaskRepository.class.getSimpleName();

  private final TaskDao mDao;
  private final LiveData<List<TaskEntity>> mAllTasks;

  public TaskRepository(Application application) {

    Log.d(TAG, "++TaskRepository(Application)");
    WildlifeDatabase db = WildlifeDatabase.getInstance(application);
    mDao = db.taskDao();
    mAllTasks = mDao.getAll();
  }

  public LiveData<List<TaskEntity>> getAll() {

    return mAllTasks;
  }

  public void insert(TaskEntity taskEntity) {

    WildlifeDatabase.databaseWriteExecutor.execute(() -> mDao.insert(taskEntity));
  }
}
