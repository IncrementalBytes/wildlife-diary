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

import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import net.whollynugatory.android.wildlife.BuildConfig;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;

public class UserSettingsFragment extends PreferenceFragmentCompat {

  private static final String TAG = Utils.BASE_TAG + UserSettingsFragment.class.getSimpleName();

  /*
    Fragment Override(s)
   */
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    Log.d(TAG, "++onCreatePreferences(Bundle, String)");
    addPreferencesFromResource(R.xml.app_preferences);
    setupAppVersionPreference();
    setupShowSensitivePreference();
  }

  /*
    Private Method(s)
   */
  private void setupAppVersionPreference() {

    Log.d(TAG, "++setupAppVersionPreference()");
    EditTextPreference editTextPreference = findPreference(getString(R.string.pref_key_app_version));
    if (editTextPreference != null) {
      editTextPreference.setSummary(BuildConfig.VERSION_NAME);
    } else {
      Log.w(TAG, "AppVersion preference was not found.");
    }
  }

  private void setupShowSensitivePreference() {

    Log.d(TAG, "++setupShowSensitivePreference()");
    SwitchPreference switchPreference = findPreference(getString(R.string.pref_key_enable_sensitive));
    if (switchPreference != null) {
      switchPreference.setChecked(Utils.getShowSensitive(getContext()));
      switchPreference.setOnPreferenceChangeListener(
        (preference, newValue) -> {

          Log.d(TAG, "++setupShowSensitivePreference::onPreferenceChange()");
          Utils.setShowSensitive(getContext(), (boolean) newValue);
          return true;
        });
    } else {
      Log.w(TAG, "ShowSensitive preference was not found.");
    }
  }
}
