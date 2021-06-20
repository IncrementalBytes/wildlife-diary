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
package net.whollynugatory.android.wildlife.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.dao.EncounterDao;
import net.whollynugatory.android.wildlife.db.dao.TaskDao;
import net.whollynugatory.android.wildlife.db.dao.WildlifeDao;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
  entities = {EncounterEntity.class, TaskEntity.class, WildlifeEntity.class},
  version = 2)
@TypeConverters({Utils.class})
public abstract class WildlifeDatabase extends RoomDatabase {

  private static final String TAG = Utils.BASE_TAG + WildlifeDatabase.class.getSimpleName();
  private static final int NUMBER_OF_THREADS = 2;

  public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  public abstract EncounterDao encounterDao();
  public abstract TaskDao taskDao();
  public abstract WildlifeDao wildlifeDao();

  private static WildlifeDatabase sInstance;

  public static WildlifeDatabase getInstance(final Context context) {

    if (sInstance == null) {
      Log.d(TAG, "++getInstance(Context)");
      synchronized (WildlifeDatabase.class) {
        if (sInstance == null) {
          sInstance = Room.databaseBuilder(context.getApplicationContext(), WildlifeDatabase.class, Utils.DATABASE_NAME)
            .addCallback(sRoomDatabaseCallback)
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build();
        }
      }
    }

    return sInstance;
  }

  private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

    @Override
    public void onCreate(@NonNull SupportSQLiteDatabase db) {
      super.onCreate(db);

      Log.d(TAG, "++onCreate(SupportSQLiteDatabase)");
    }

    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db) {
      super.onOpen(db);

      Log.d(TAG, "++onOpen(SupportSQLiteDatabase)");

    }
  };

  static final Migration MIGRATION_1_2 = new Migration(1, 2) {

    @Override
    public void migrate(SupportSQLiteDatabase database) {

      Log.d(TAG, "++migrate(MIGRATION_1_2)");
      Log.d(TAG, "Adding new columns to wildlife_table");
      database.execSQL("CREATE TABLE wildlife_table_temp (" +
        "id TEXT PRIMARY KEY NOT NULL, " +
        "abbreviation TEXT NOT NULL, " +
        "friendly_name TEXT NOT NULL, " +
        "image_src TEXT DEFAULT '', " +
        "image_attribution TEXT DEFAULT '')");
      database.execSQL("INSERT INTO wildlife_table_temp SELECT id, abbreviation, friendly_name, '', '' FROM wildlife_table");
      database.execSQL("DROP TABLE wildlife_table");
      database.execSQL("ALTER TABLE wildlife_table_temp RENAME TO wildlife_table");
    }
  };
}
