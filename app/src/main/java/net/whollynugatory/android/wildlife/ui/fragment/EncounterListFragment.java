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
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.SpinnerItemState;
import net.whollynugatory.android.wildlife.TaskItem;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterSummary;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.SpinnerItemAdapter;
import net.whollynugatory.android.wildlife.ui.TaskListItemAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EncounterListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterListFragment.class.getSimpleName();

  public interface OnEncounterListListener {

    void onEncounterListPopulated();

    void onEncounterDetailsClicked(String encounterId);
  }

  private OnEncounterListListener mCallback;

  private EncounterAdapter mEncounterAdapter;
  private RecyclerView mRecyclerView;

  private String mFollowingUserId;

  public static EncounterListFragment newInstance(String followingUserId) {

    Log.d(TAG, "++newInstance(String)");
    EncounterListFragment fragment = new EncounterListFragment();
    Bundle arguments = new Bundle();
    arguments.putSerializable(Utils.ARG_FOLLOWING_USER_ID, followingUserId);
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
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mEncounterAdapter = new EncounterAdapter(getContext());
    mRecyclerView.setAdapter(mEncounterAdapter);
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    wildlifeViewModel.getEncounterSummaries(mFollowingUserId).observe(getViewLifecycleOwner(), encounterSummaries -> {

      HashMap<String, EncounterSummary> encounterSummaryHashMap = new HashMap<>();
      for (EncounterSummary encounterSummary : encounterSummaries) {
        if (encounterSummaryHashMap.containsKey(encounterSummary.EncounterId)) {
          if (!encounterSummaryHashMap.get(encounterSummary.EncounterId).Tasks.containsKey(encounterSummary.TaskId)) {
            encounterSummaryHashMap.get(encounterSummary.EncounterId).Tasks.put(
              encounterSummary.TaskId,
              new TaskEntity(encounterSummary.TaskId, encounterSummary.TaskName, encounterSummary.TaskDescription, encounterSummary.TaskIsSensitive));
          }
        } else {
          encounterSummaryHashMap.put(encounterSummary.EncounterId, encounterSummary);
          encounterSummaryHashMap.get(encounterSummary.EncounterId).Tasks.put(
            encounterSummary.TaskId,
            new TaskEntity(encounterSummary.TaskId, encounterSummary.TaskName, encounterSummary.TaskDescription, encounterSummary.TaskIsSensitive));
        }
      }

      mEncounterAdapter.setEncounterSummaryList(encounterSummaryHashMap.values());
      mCallback.onEncounterListPopulated();
    });
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnEncounterListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      if (arguments.containsKey(Utils.ARG_FOLLOWING_USER_ID)) {
        mFollowingUserId = (String) arguments.getSerializable(Utils.ARG_FOLLOWING_USER_ID);
      } else {
        mFollowingUserId = "";
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
    final View view = inflater.inflate(R.layout.fragment_encounter_list, container, false);
    mRecyclerView = view.findViewById(R.id.encounter_list_view);
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

  public class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + EncounterAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private List<EncounterSummary> mEncounterSummaries;

    public EncounterAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EncounterAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.encounter_item, parent, false);
      return new EncounterAdapter.EncounterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EncounterAdapter.EncounterHolder holder, int position) {

      if (mEncounterSummaries != null) {
        EncounterSummary encounterSummary = mEncounterSummaries.get(position);
        holder.bind(encounterSummary);
      } else {
        Log.w(TAG, "Encounter Summaries is empty at this time.");
      }
    }

    @Override
    public int getItemCount() {

      if (mEncounterSummaries != null) {
        return mEncounterSummaries.size();
      } else {
        return 0;
      }
    }

    public void setEncounterSummaryList(Collection<EncounterSummary> encounterSummaryCollection) {

      Log.d(TAG, "++setEncounterSummaryList(Collection<EncounterSummary>)");
      mEncounterSummaries = new ArrayList<>(encounterSummaryCollection);
      mEncounterSummaries.sort((a, b) -> Long.compare(b.Date, a.Date));
      notifyDataSetChanged();
    }

    class EncounterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final String TAG = Utils.BASE_TAG + EncounterAdapter.EncounterHolder.class.getSimpleName();

      private final TextView mAbbreviationTextView;
      private final TextView mEncounterDateTextView;
      private final TextView mWildlifeTextView;
      private final ListView mTaskListView;

      private EncounterSummary mEncounterSummary;

      EncounterHolder(View itemView) {
        super(itemView);

        mAbbreviationTextView = itemView.findViewById(R.id.encounter_item_abbreviation);
        mEncounterDateTextView = itemView.findViewById(R.id.encounter_item_date);
        mWildlifeTextView = itemView.findViewById(R.id.encounter_item_wildlife);
        mTaskListView = itemView.findViewById(R.id.encounter_item_list_tasks);
        itemView.setOnClickListener(this);
      }

      void bind(EncounterSummary encounterSummary) {

        mEncounterSummary = encounterSummary;

        mAbbreviationTextView.setText(mEncounterSummary.WildlifeAbbreviation);
        mEncounterDateTextView.setText(Utils.displayDate(mEncounterSummary.Date));
        mWildlifeTextView.setText(mEncounterSummary.WildlifeSpecies);
        boolean showSensitive = Utils.getShowSensitive(getActivity());

        ArrayList<TaskItem> taskItems = new ArrayList<>();
        for (TaskEntity taskEntity : mEncounterSummary.Tasks.values()) {
          if (taskEntity.IsSensitive && !showSensitive) {
            Log.d(TAG, "Skipping task based on user setting.");
            continue;
          }

          TaskItem item = new TaskItem();
          item.setDescription(taskEntity.Description);
          item.setName(taskEntity.Name);
          taskItems.add(item);
        }

        Log.d(TAG, "Task list for " + mEncounterSummary.EncounterId + " is " + taskItems.size());
        ListAdapter customAdapter = new TaskListItemAdapter(getActivity(), 0, taskItems);
        mTaskListView.setAdapter(customAdapter);
      }

      @Override
      public void onClick(View view) {

        Log.d(TAG, "++EncounterHolder::onClick(View)");
        // TODO: either remove or try show/hide descriptions of tasks
      }
    }
  }
}
