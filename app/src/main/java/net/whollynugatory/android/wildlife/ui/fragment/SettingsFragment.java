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
package net.whollynugatory.android.wildlife.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import net.whollynugatory.android.wildlife.BuildConfig;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;

public class SettingsFragment extends PreferenceFragmentCompat {

  private static final String TAG = Utils.BASE_TAG + SettingsFragment.class.getSimpleName();

  public static SettingsFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new SettingsFragment();
  }

  /*
      Fragment Override(s)
   */
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    Log.d(TAG, "++onCreatePreferences(Bundle, String)");
    addPreferencesFromResource(R.xml.app_preferences);
    setupAppVersionPreference();
  }

  /*
    Private Method(s)
   */
  private void setupAppVersionPreference() {

    Log.d(TAG, "++setupAppVersionPreference()");
    EditTextPreference editTextPreference = findPreference(getString(R.string.pref_key_app_version));
    if (editTextPreference != null) {
      editTextPreference.setSummary(BuildConfig.VERSION_NAME);
    }
  }
}
