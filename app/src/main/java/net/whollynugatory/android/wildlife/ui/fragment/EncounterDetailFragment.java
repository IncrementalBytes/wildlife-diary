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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.view.EncounterDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class EncounterDetailFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDetailFragment.class.getSimpleName();

  private TextView mDateText;
  private TextView mWildlifeText;
  private ListView mTaskList;

  private String mEncounterId;

  public static EncounterDetailFragment newInstance(String encounterId) {

    Log.d(TAG, "++newInstance(String)");
    EncounterDetailFragment fragment = new EncounterDetailFragment();
    Bundle arguments = new Bundle();
    arguments.putString(Utils.ARG_ENCOUNTER_ID, encounterId);
    fragment.setArguments(arguments);
    return fragment;
  }
  /*
      Fragment Override(s)
    */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);

    wildlifeViewModel.getEncounterDetails(mEncounterId).observe(
      getViewLifecycleOwner(),
      encounterDetailsList -> {

        mWildlifeText.setText(
          String.format(
            Locale.US,
            getString(R.string.format_wildlife),
            encounterDetailsList.get(0).WildlifeSpecies,
            encounterDetailsList.get(0).WildlifeAbbreviation));
        mDateText.setText(Utils.displayDate(encounterDetailsList.get(0).Date));

        ArrayList<String> tasks = new ArrayList<>();
        boolean showSensitive = Utils.getShowSensitive(getContext());
        for (EncounterDetails encounterDetails : encounterDetailsList) {
          if (encounterDetails.IsSensitive) {
            if (showSensitive && !tasks.contains(encounterDetails.TaskName)) {
              tasks.add(encounterDetails.TaskName);
            }
          } else if (!tasks.contains(encounterDetails.TaskName)) {
            tasks.add(encounterDetails.TaskName);
          }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
          getActivity(),
          android.R.layout.simple_list_item_checked, // TODO: consider other type of layout? (needs researching)
          new ArrayList<>(tasks));
        mTaskList.setAdapter(dataAdapter);
      });
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null) {
      if (arguments.containsKey(Utils.ARG_ENCOUNTER_ID)) {
        mEncounterId = arguments.getString(Utils.ARG_ENCOUNTER_ID);
      } else {
        mEncounterId = Utils.UNKNOWN_ID;
      }
    } else {
      Log.e(TAG, "Arguments were null.");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_encounter_details, container, false);
    mDateText = view.findViewById(R.id.encounter_details_text_date);
    mWildlifeText = view.findViewById(R.id.encounter_details_text_wildlife);
    mTaskList = view.findViewById(R.id.encounter_details_list_tasks);
    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d(TAG, "++onDetach()");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
  }
}
