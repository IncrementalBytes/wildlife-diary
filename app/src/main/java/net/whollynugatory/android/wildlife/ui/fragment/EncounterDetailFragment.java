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

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.ui.TaskListItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class EncounterDetailFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDetailFragment.class.getSimpleName();

  private EncounterDetails mEncounterDetails;

  public static EncounterDetailFragment newInstance(EncounterDetails encounterDetails) {

    Log.d(TAG, "++newInstance(EncounterDetails)");
    EncounterDetailFragment fragment = new EncounterDetailFragment();
    Bundle arguments = new Bundle();
    arguments.putSerializable(Utils.ARG_ENCOUNTER_DETAILS, encounterDetails);
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
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null && arguments.containsKey(Utils.ARG_ENCOUNTER_DETAILS)) {
      mEncounterDetails = (EncounterDetails) arguments.getSerializable(Utils.ARG_ENCOUNTER_DETAILS);
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
    View view = inflater.inflate(R.layout.fragment_encounter_details, container, false);
    TextView wildlifeTextView = view.findViewById(R.id.encounter_details_wildlife);
    TextView abbreviationTextView = view.findViewById(R.id.encounter_details_abbreviation);
    TextView dateTextView = view.findViewById(R.id.encounter_details_date);
    ListView taskListView = view.findViewById(R.id.encounter_details_list_tasks);

    wildlifeTextView.setText(mEncounterDetails.WildlifeSpecies);
    abbreviationTextView.setText(mEncounterDetails.WildlifeAbbreviation);
    dateTextView.setText(Utils.fromTimestamp(mEncounterDetails.Date));

    List<String> taskIds = Utils.toTaskList(mEncounterDetails.TaskIds);
    List<TaskEntity> taskEntityList = Utils.getTaskList(getContext());
    List<TaskEntity> completedTaskList = new ArrayList<>();
    boolean showSensitive = Utils.getShowSensitive(getContext());
    for(String taskId : taskIds) {
      for (TaskEntity taskEntity : taskEntityList) {
        if (taskId.equals(taskEntity.Id) && (!taskEntity.IsSensitive || showSensitive)) {
          completedTaskList.add(taskEntity);
          break;
        }
      }
    }

    Log.d(TAG, "Task list is " + completedTaskList.size());
    ListAdapter customAdapter = new TaskListItemAdapter(getActivity(), 0, completedTaskList);
    taskListView.setAdapter(customAdapter);
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
