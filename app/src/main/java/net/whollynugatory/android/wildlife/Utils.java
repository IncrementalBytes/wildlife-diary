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
import android.util.Log;

import androidx.preference.PreferenceManager;

import androidx.annotation.StringRes;
import androidx.room.TypeConverter;

import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.db.entity.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Utils {

  private static final String TAG = Utils.BASE_TAG + Utils.class.getSimpleName();

  public static final String ARG_ENCOUNTER_ID = "encounter_id";
  public static final String ARG_FIREBASE_USER_ID = "firebase_user_id";
  public static final String ARG_MESSAGE = "message";
  public static final String ARG_DATA_TO_SYNC = "data_to_sync";
  public static final String ARG_SUMMARY_ID = "summary_id";
  public static final String BASE_TAG = "wildlife::";
  public static final String DATABASE_NAME = "wildlife.db";
  public static final String DATA_STAMPS_ROOT = "DataStamps";
  public static final String DEFAULT_ATTRIBUTION = "CC BY-SA 3.0 <https://creativecommons.org/licenses/by-sa/3.0>, via Wikimedia Commons";
  public static final String DEFAULT_FOLLOWING_USER_ID = "pHuFQzKKwJhI0KNTc6UoCvtMXEI2";
  public static final String ENCOUNTER_ROOT = "Encounters";
  public static final String TASK_ROOT = "Tasks";
  public static final String UNKNOWN_ID = "000000000-0000-0000-0000-000000000000";
  public static final String UNKNOWN_STRING = "UNKNOWN";
  public static final String UNKNOWN_USER_ID = "0000000000000000000000000000";
  public static final String USERS_ROOT = "Users";
  public static final String WILDLIFE_ROOT = "Wildlife";

  public static String combine(Object... paths) {

    String finalPath = "";
    for (Object path : paths) {
      String format = "%s/%s";
      if (path.getClass() == Integer.class) {
        format = "%s/%d";
      }

      finalPath = String.format(Locale.US, format, finalPath, path);
    }

    return finalPath;
  }

  @TypeConverter
  public static String fromTimestamp(long dateInMilliseconds) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(dateInMilliseconds);
    return new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime());
  }

  public static boolean getIsContributor(Context context) {

    return getBooleanPref(context, R.string.pref_key_is_contributor, false);
  }

  public static String getFollowingUserId(Context context) {

    return getStringPref(context, R.string.pref_key_following_user_id, Utils.UNKNOWN_USER_ID);
  }

  public static String getLocalEncountersStamp(Context context) {

    return getStringPref(context, R.string.pref_key_stamp_encounters, Utils.UNKNOWN_ID);
  }

  public static String getLocalTasksStamp(Context context) {

    return getStringPref(context, R.string.pref_key_stamp_tasks, Utils.UNKNOWN_ID);
  }

  public static String getLocalWildlifeStamp(Context context) {

    return getStringPref(context, R.string.pref_key_stamp_wildlife, Utils.UNKNOWN_ID);
  }

  public static boolean getShowSensitive(Context context) {

    return getBooleanPref(context, R.string.pref_key_enable_sensitive, false);
  }

  public static String getUserId(Context context) {

    return getStringPref(context, R.string.pref_key_user_id, Utils.UNKNOWN_USER_ID);
  }

  public static void setIsContributor(Context context, boolean canAdd) {

    setBooleanPref(context, R.string.pref_key_is_contributor, canAdd);
  }

  public static void setFollowingUserId(Context context, String newFollowingUserId) {

    setStringPref(context, R.string.pref_key_following_user_id, newFollowingUserId);
  }

  public static void setLocalEncountersStamp(Context context, String newEncountersStamp) {

    setStringPref(context, R.string.pref_key_stamp_encounters, newEncountersStamp);
  }

  public static void setLocalTasksStamp(Context context, String newTasksStamp) {

    setStringPref(context, R.string.pref_key_stamp_tasks, newTasksStamp);
  }

  public static void setLocalWildlifeStamp(Context context, String newWildlifeStamp) {

    setStringPref(context, R.string.pref_key_stamp_wildlife, newWildlifeStamp);
  }

  public static void setShowSensitive(Context context, boolean showSensitive) {

    setBooleanPref(context, R.string.pref_key_enable_sensitive, showSensitive);
  }

  public static void setUserId(Context context, String userId) {

    setStringPref(context, R.string.pref_key_user_id, userId);
  }

  @TypeConverter
  public static long toTimestamp(String dateString) {

    Calendar calendar = Calendar.getInstance();
    calendar.set(2000, 0, 1, 0, 0, 0);
    if (dateString.length() < 8) {
      String dateFormat = "MMddYYYY";
      dateString = dateString + dateFormat.substring(dateString.length());
    }

    int month = Integer.parseInt(dateString.substring(0, 2));
    int day = Integer.parseInt(dateString.substring(3, 5));
    int year = Integer.parseInt(dateString.substring(6, 10));
    month = month < 1 ? 1 : Math.min(month, 12);
    calendar.set(Calendar.MONTH, month - 1);
    calendar.set(Calendar.DATE, Math.min(day, calendar.getActualMaximum(Calendar.DATE)));
    year = (year < 1900) ? 1900 : Math.min(year, 2100);
    calendar.set(Calendar.YEAR, year);
    return calendar.getTimeInMillis();
  }

  /**
   * Changes remote data stamp value to force application for refresh on next data sync.
   * NOTE: only handling Encounters data for now.
   */
  public static void updateRemoteDataStamp(String dataStampRoot) {

    Log.d(TAG, "++updateRemoteDataStamp(String)");
    Map<String, Object> childUpdates = new HashMap<>();
    final String remoteDataStamp = UUID.randomUUID().toString();
    childUpdates.put(dataStampRoot, remoteDataStamp);
    FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).updateChildren(childUpdates)
      .addOnCompleteListener(task -> {

        if (!task.isSuccessful()) {
          Log.w(TAG, "Unable to update remote data stamp for changes.", task.getException());
        } else {
          Log.d(TAG, "Setting remote data stamp for " + dataStampRoot + " to " + remoteDataStamp);
        }
      });
  }

  /*
    Private Method(s)
   */
  private static boolean getBooleanPref(

    Context context, @StringRes int prefKeyId, boolean defaultValue) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getBoolean(prefKey, defaultValue);
  }

  public static String getStringPref(Context context, @StringRes int preferenceKeyId, String defaultValue) {

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(preferenceKeyId);
    return sharedPreferences.getString(prefKey, defaultValue);
  }

  private static void setBooleanPref(Context context, @StringRes int preferenceKeyId, boolean value) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putBoolean(context.getString(preferenceKeyId), value)
      .apply();
  }

  private static void setStringPref(Context context, @StringRes int preferenceKeyId, String preferenceValue) {

    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .putString(context.getString(preferenceKeyId), preferenceValue)
      .apply();
  }

  /*
    Public Class(es)
   */
  public static class SortByName implements Comparator<TaskEntity> {

    public int compare(TaskEntity a, TaskEntity b) {

      return a.Name.compareTo(b.Name);
    }
  }
}
