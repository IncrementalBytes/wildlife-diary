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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.TaskListItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class EncounterDetailFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDetailFragment.class.getSimpleName();

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
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null && arguments.containsKey(Utils.ARG_ENCOUNTER_ID)) {
      mEncounterId = arguments.getString(Utils.ARG_ENCOUNTER_ID);
      Log.d(TAG, "EncounterId: " + mEncounterId);
    } else {
      Log.e(TAG, "Arguments were null.");
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    return inflater.inflate(R.layout.fragment_encounter_details, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d(TAG, "onViewCreated()");
    TextView wildlifeTextView = view.findViewById(R.id.encounter_details_wildlife);
    TextView abbreviationTextView = view.findViewById(R.id.encounter_details_abbreviation);
    TextView dateTextView = view.findViewById(R.id.encounter_details_date);
    ListView taskListView = view.findViewById(R.id.encounter_details_list_tasks);
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getContext());
    wildlifeViewModel.getEncounterDetails(followingUserId, mEncounterId).observe(getViewLifecycleOwner(), encounters -> {

      List<TaskEntity> completedTaskList = new ArrayList<>();
      boolean showSensitive = Utils.getShowSensitive(getContext());
      for (EncounterDetails encounterDetails : encounters) {
        if (!encounterDetails.TaskIsSensitive || showSensitive) {
          TaskEntity taskEntity = new TaskEntity();
          taskEntity.Description = encounterDetails.TaskDescription;
          taskEntity.IsSensitive = encounterDetails.TaskIsSensitive;
          taskEntity.Name = encounterDetails.TaskName;
          completedTaskList.add(taskEntity);
        }
      }

      wildlifeTextView.setText(encounters.get(0).WildlifeSpecies);
      abbreviationTextView.setText(encounters.get(0).WildlifeAbbreviation);
      dateTextView.setText(Utils.fromTimestamp(encounters.get(0).Date));
      Log.d(TAG, "Task list is " + completedTaskList.size());
      ListAdapter customAdapter = new TaskListItemAdapter(getActivity(), 0, completedTaskList);
      taskListView.setAdapter(customAdapter);
    });
  }
}
