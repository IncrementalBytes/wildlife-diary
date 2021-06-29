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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.WildlifeSummary;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.CleanUpAdapter;
import net.whollynugatory.android.wildlife.ui.EncounterAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + ListFragment.class.getSimpleName();

  public interface OnSimpleListListener {

    void onTaskListSet(String titleUpdate);

    void onUnknownList();
  }

  private OnSimpleListListener mCallback;

  private int mSummaryId;

  public static ListFragment newInstance(int summaryId) {

    Log.d(TAG, "++newInstance(int)");
    ListFragment fragment = new ListFragment();
    Bundle arguments = new Bundle();
    arguments.putInt(Utils.ARG_SUMMARY_ID, summaryId);
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
    try {
      mCallback = (OnSimpleListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      if (arguments.containsKey(Utils.ARG_SUMMARY_ID)) {
        mSummaryId = arguments.getInt(Utils.ARG_SUMMARY_ID);
      } else {
        Log.e(TAG, "Arguments are missing.");
      }
    } else {
      Log.e(TAG, "Arguments were null.");
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_simple_list, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.simple_list_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    EncounterAdapter encounterAdapter = new EncounterAdapter(getContext());
    recyclerView.setAdapter(encounterAdapter);

    String followingUserId = Utils.getFollowingUserId(getContext());
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);

    // TODO: add support for clicking of items displayed in list
    if (mSummaryId == R.id.summary_card_unique_encounters) {
      wildlifeViewModel.getUniqueEncountered(followingUserId).observe(getViewLifecycleOwner(), uniqueEncounteredList -> {

        Log.d(TAG, "Unique Encounter list is " + uniqueEncounteredList.size());
        encounterAdapter.setWildlifeSummaryList(uniqueEncounteredList);
      });
    } else if (mSummaryId == R.id.summary_card_most_encountered) {
      wildlifeViewModel.getMostEncountered(followingUserId).observe(getViewLifecycleOwner(), mostEncounteredList -> {

        Log.d(TAG, "Most Encounter list is " + mostEncounteredList.size());
        encounterAdapter.setWildlifeSummaryList(mostEncounteredList);
      });
    } else if (mSummaryId == R.id.summary_card_encounters_by_date) {
      wildlifeViewModel.getTotalEncounters(followingUserId).observe(getViewLifecycleOwner(), totalEncounterList -> {

        HashMap<String, WildlifeSummary> wildlifeSummaryMap = new HashMap<>();
        for(EncounterDetails encounterDetails : totalEncounterList) {
          String dateString = Utils.fromTimestamp(encounterDetails.Date);
          if (wildlifeSummaryMap.containsKey(dateString)) {
            WildlifeSummary dateInstance = wildlifeSummaryMap.get(dateString);
            if (dateInstance != null) {
              dateInstance.EncounterCount++;
            } else {
              Log.w(TAG, "Could not get date instance from map: " + dateString);
            }
          } else {
            WildlifeSummary wildlifeSummary = new WildlifeSummary();
            wildlifeSummary.WildlifeSpecies = dateString;
            wildlifeSummary.EncounterCount = 1;
            wildlifeSummary.WildlifeId = String.valueOf(encounterDetails.Date);
            wildlifeSummaryMap.put(dateString, wildlifeSummary);
          }
        }

        List<WildlifeSummary> sortedSummaryCollection = new ArrayList<>(wildlifeSummaryMap.values());
        sortedSummaryCollection.sort((s1, s2) -> s2.WildlifeId.compareTo(s1.WildlifeId));
        encounterAdapter.setWildlifeSummaryList(sortedSummaryCollection);
      });
    } else if (mSummaryId == R.id.menu_cleanup) {
      CleanUpAdapter cleanUpAdapter = new CleanUpAdapter(getContext());
      recyclerView.setAdapter(cleanUpAdapter);
      wildlifeViewModel.getCleanUpItems().observe(getViewLifecycleOwner(), cleanUpDetailsList -> {

        Log.d(TAG, "CleanUp list is " + cleanUpDetailsList.size());
        cleanUpAdapter.setCleanUpList(cleanUpDetailsList);
      });
    } else {
      String taskName = "";
      if (mSummaryId == R.id.summary_card_banded) {
        taskName = "Banded";
      } else if (mSummaryId == R.id.summary_card_force_fed) {
        taskName = "Force Fed";
      } else if (mSummaryId == R.id.summary_card_gavage) {
        taskName = "Gavage";
      } else if (mSummaryId == R.id.summary_card_handled_euthanasia) {
        taskName = "Handled - Euthanize";
      } else if (mSummaryId == R.id.summary_card_handled_exam) {
        taskName = "Handled - Exam";
      } else if (mSummaryId == R.id.summary_card_handled_force_fed) {
        taskName = "Handled - Force Fed";
      } else if (mSummaryId == R.id.summary_card_handled_gavage) {
        taskName = "Handled - Gavage";
      } else if (mSummaryId == R.id.summary_card_handled_medication) {
        taskName = "Handled - Medicate";
      } else if (mSummaryId == R.id.summary_card_handled_subcutaneous) {
        taskName = "Handled - Subcutaneous";
      } else if (mSummaryId == R.id.summary_card_medication_ocular) {
        taskName = "Medicate - Ocular";
      } else if (mSummaryId == R.id.summary_card_medication_oral) {
        taskName = "Medicate - Oral";
      } else if (mSummaryId == R.id.summary_card_subcutaneous) {
        taskName = "Subcutaneous";
      } else if (mSummaryId == R.id.summary_card_syringe_fed) {
        taskName = "Syringe Fed";
      }

      if (!taskName.isEmpty()) {
        String finalTaskName = taskName;
        wildlifeViewModel.getEncountersByTaskName(followingUserId, taskName).observe(getViewLifecycleOwner(), encounterDetailsList -> {

          Log.d(TAG, "Encounters list is " + encounterDetailsList.size());
          encounterAdapter.setEncounterDetailsList(encounterDetailsList);
          mCallback.onTaskListSet(finalTaskName);
        });
      } else {
        mCallback.onUnknownList();
      }
    }

    return view;
  }
}
