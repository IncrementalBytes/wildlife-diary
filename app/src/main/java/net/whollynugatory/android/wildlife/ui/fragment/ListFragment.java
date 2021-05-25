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
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + ListFragment.class.getSimpleName();

  public interface OnSimpleListListener {

    void onWildlifeItemSelected(String wildlifeId);

    void onEncounterItemSelected(String encounterId);

    void onTaskItemSelected(String taskId);

    void onTaskListSet(String titleUpdate);

    void onUnknownList();
  }

  private OnSimpleListListener mCallback;

  private EncounterAdapter mEncounterAdapter;

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
    mEncounterAdapter = new EncounterAdapter(getContext());
    recyclerView.setAdapter(mEncounterAdapter);

    String followingUserId = Utils.getFollowingUserId(getContext());
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);

    if (mSummaryId == R.id.summary_card_unique_encounters) {
      wildlifeViewModel.getUniqueEncountered(followingUserId).observe(getViewLifecycleOwner(), uniqueEncounteredList -> {

        Log.d(TAG, "Unique Encounter list is " + uniqueEncounteredList.size());
        mEncounterAdapter.setWildlifeSummaryList(uniqueEncounteredList);
      });
    } else if (mSummaryId == R.id.summary_card_most_encountered) {
      wildlifeViewModel.getMostEncountered(followingUserId).observe(getViewLifecycleOwner(), mostEncounteredList -> {

        Log.d(TAG, "Most Encounter list is " + mostEncounteredList.size());
        mEncounterAdapter.setWildlifeSummaryList(mostEncounteredList);
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
        taskName = "Handled - Euthanasia";
      } else if (mSummaryId == R.id.summary_card_handled_exam) {
        taskName = "Handled - Exam";
      } else if (mSummaryId == R.id.summary_card_handled_force_fed) {
        taskName = "Handled - Force Fed";
      } else if (mSummaryId == R.id.summary_card_handled_gavage) {
        taskName = "Handled - Gavage";
      } else if (mSummaryId == R.id.summary_card_handled_medication) {
        taskName = "Handled - Medication";
      } else if (mSummaryId == R.id.summary_card_handled_subcutaneous) {
        taskName = "Handled - Subcutaneous";
      } else if (mSummaryId == R.id.summary_card_medication_ocular) {
        taskName = "Ocular Medication";
      } else if (mSummaryId == R.id.summary_card_medication_oral) {
        taskName = "Oral Medication";
      } else if (mSummaryId == R.id.summary_card_subcutaneous) {
        taskName = "Subcutaneous";
      } else if (mSummaryId == R.id.summary_card_syringe_fed) {
        taskName = "Syringe Fed";
      }

      if (!taskName.isEmpty()) {
        String finalTaskName = taskName;
        wildlifeViewModel.getEncountersByTaskName(followingUserId, taskName).observe(getViewLifecycleOwner(), encounterDetailsList -> {

          Log.d(TAG, "Encounters list is " + encounterDetailsList.size());
          mEncounterAdapter.setEncounterDetailsList(encounterDetailsList);
          mCallback.onTaskListSet(finalTaskName);
        });
      } else {
        mCallback.onUnknownList();
      }
    }

    return view;
  }

  private static class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + EncounterAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private List<EncounterDetails> mEncounterDetails;
    private List<WildlifeSummary> mWildlifeSummaries;

    public EncounterAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EncounterAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.simple_list_item, parent, false);
      return new EncounterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EncounterAdapter.EncounterHolder holder, int position) {

      if (mEncounterDetails != null) {
        EncounterDetails encounterDetails = mEncounterDetails.get(position);
        holder.bind(encounterDetails);
      } else if (mWildlifeSummaries != null) {
        WildlifeSummary wildlifeSummary = mWildlifeSummaries.get(position);
        holder.bind(wildlifeSummary);
      } else {
        Log.w(TAG, "EncounterDetails is empty at this time.");
      }
    }

    @Override
    public int getItemCount() {

      return mEncounterDetails != null ? mEncounterDetails.size() : mWildlifeSummaries != null ? mWildlifeSummaries.size() : 0;
    }

    public void setEncounterDetailsList(Collection<EncounterDetails> encounterDetailsCollection) {

      Log.d(TAG, "++setEncounterSummaryList(Collection<EncounterDetails>)");
      mEncounterDetails = new ArrayList<>(encounterDetailsCollection);
      mEncounterDetails.sort((a, b) -> Long.compare(b.Date, a.Date));
      notifyDataSetChanged();
    }

    public void setWildlifeSummaryList(Collection<WildlifeSummary> wildlifeSummaryCollection) {

      Log.d(TAG, "++setWildlifeSummaryList(Collection<WildlifeSummary>)");
      mWildlifeSummaries = new ArrayList<>(wildlifeSummaryCollection);
      notifyDataSetChanged();
    }

    static class EncounterHolder extends RecyclerView.ViewHolder {

      private final TextView mTitleTextView;
      private final TextView mDetailsTextView;

      EncounterHolder(View itemView) {
        super(itemView);

        mTitleTextView = itemView.findViewById(R.id.simple_list_item_text_name);
        mDetailsTextView = itemView.findViewById(R.id.simple_list_item_text_desc);
        mDetailsTextView.setTextSize(18f);
      }

      void bind(EncounterDetails encounterDetails) {

        mTitleTextView.setText(encounterDetails.WildlifeSpecies);
        mDetailsTextView.setText(Utils.fromTimestamp(encounterDetails.Date));
      }

      void bind(WildlifeSummary wildlifeSummary) {

        mTitleTextView.setText(wildlifeSummary.WildlifeSpecies);
        mDetailsTextView.setText(wildlifeSummary.SummaryDetails);
      }
    }
  }
}
