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

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import androidx.annotation.StringRes;

public class Utils {

  public static final String ARG_ENCOUNTER_ENTITY = "encounter_entity";
  public static final String ARG_ENCOUNTER_ID = "encounter_id";
  public static final String ARG_FIREBASE_USER_ID = "firebase_user_id";
  public static final String ARG_USER = "user";
  public static final String BASE_TAG = "wildlife::";
  public static final String DATABASE_NAME = "wildlife.db";
  public static final String ENCOUNTER_ROOT = "Encounters";
  public static final String TASK_ROOT = "Tasks";
  public static final String UNKNOWN_DATE = "01/01/2000";
  public static final String UNKNOWN_ID = "000000000-0000-0000-0000-000000000000";
  public static final String UNKNOWN_STRING = "UNKNOWN";
  public static final String UNKNOWN_USER_ID = "0000000000000000000000000000";
  public static final String USERS_ROOT = "Users";
  public static final String WILDLIFE_ROOT = "Wildlife";

  /*
    Private Method(s)
   */
  private static boolean getBooleanPref(

    Context context, @StringRes int prefKeyId, boolean defaultValue) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getBoolean(prefKey, defaultValue);
  }

  private static int getIntPref(Context context, @StringRes int prefKeyId, int defaultValue) {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getInt(prefKey, defaultValue);
  }
}
