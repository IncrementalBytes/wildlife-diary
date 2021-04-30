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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

public class Utils {

  public static final String ARG_ENCOUNTER_ID = "encounter_id";
  public static final String ARG_FIREBASE_USER_ID = "firebase_user_id";
  public static final String ARG_FOLLOWING_USER_ID = "following-id";
  public static final String BASE_TAG = "wildlife::";
  public static final String DATABASE_NAME = "wildlife.db";
  public static final String DATA_STAMPS_ROOT = "DataStamps";
  public static final String DEFAULT_FOLLOWING_USER_ID = "leqPZhhUQ1dMcwiGLxpr1fOv9d63";
  public static final String ENCOUNTER_ROOT = "Encounters";
  public static final String ARG_SUMMARY_ID = "summary_id";
  public static final String TASK_ROOT = "Tasks";
  public static final String UNKNOWN_ID = "000000000-0000-0000-0000-000000000000";
  public static final String UNKNOWN_STRING = "UNKNOWN";
  public static final String UNKNOWN_USER_ID = "0000000000000000000000000000";
  public static final String USERS_ROOT = "Users";
  public static final String WILDLIFE_ROOT = "Wildlife";

  public static long convertToLong(String dateString) {

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

  public static String displayDate(long dateInMilliseconds) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(dateInMilliseconds);
    return new SimpleDateFormat( "MM/dd/yyyy", Locale.US).format(calendar.getTime());
  }

  public static String getEncountersStamp(Context context) {

    return getStringPref(context, R.string.perf_key_stamp_encounters, Utils.UNKNOWN_ID);
  }

  public static String getTasksStamp(Context context) {

    return getStringPref(context, R.string.perf_key_stamp_tasks, Utils.UNKNOWN_ID);
  }

  public static boolean getShowSensitive(Context context) {

    return getBooleanPref(context, R.string.pref_key_enable_sensitive, false);
  }

  public static String getUserId(Context context) {

    return getStringPref(context, R.string.perf_key_user_id, Utils.UNKNOWN_USER_ID);
  }

  public static String getWildlifeStamp(Context context) {

    return getStringPref(context, R.string.perf_key_stamp_wildlife, Utils.UNKNOWN_ID);
  }

  public static void setEncountersStamp(Context context, String newEncountersStamp) {

    setStringPref(context, R.string.perf_key_stamp_encounters, newEncountersStamp);
  }

  public static void setShowSensitive(Context context, boolean showSensitive) {

    setBooleanPref(context, R.string.pref_key_enable_sensitive, showSensitive);
  }

  public static void setTasksStamp(Context context, String newTasksStamp) {

    setStringPref(context, R.string.perf_key_stamp_tasks, newTasksStamp);
  }

  public static void setUserId(Context context, String userId) {

    setStringPref(context, R.string.perf_key_user_id, userId);
  }

  public static void setWildlifeStamp(Context context, String newWildlifeStamp) {

    setStringPref(context, R.string.perf_key_stamp_wildlife, newWildlifeStamp);
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

  private static String getStringPref(Context context, @StringRes int preferenceKeyId, String defaultValue) {

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
  public static class SortByName implements Comparator<SpinnerItemState> {

    public int compare(SpinnerItemState a, SpinnerItemState b) {

      return a.getTitle().compareTo(b.getTitle());
    }
  }
}
