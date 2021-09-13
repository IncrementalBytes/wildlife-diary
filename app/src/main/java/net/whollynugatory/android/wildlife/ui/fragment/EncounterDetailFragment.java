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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EncounterDetailFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDetailFragment.class.getSimpleName();

  private TextView mAbbreviationText;
  private TextView mDateText;
  private TextView mImageAttributionText;
  private TextView mNumberInGroupText;
  private ImageView mWildlifeImage;
  private TextView mWildlifeText;

  private List<EncounterDetails> mEncounterDetailsList;
  private EncounterEntity mEncounterEntity;
  private String mEncounterId;
  private TaskAdapter mTaskAdapter;

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
    mAbbreviationText = view.findViewById(R.id.encounter_details_text_abbreviation);
    mDateText = view.findViewById(R.id.encounter_details_text_date);
    mImageAttributionText = view.findViewById(R.id.encounter_details_text_attribution);
    mNumberInGroupText = view.findViewById(R.id.encounter_details_text_number_in_group);
    mWildlifeImage = view.findViewById(R.id.encounter_details_image_wildlife);
    mWildlifeText = view.findViewById(R.id.encounter_details_text_wildlife);

    RecyclerView recyclerView = view.findViewById(R.id.encounter_details_recycler_tasks);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    mTaskAdapter = new TaskAdapter(getContext());
    recyclerView.setAdapter(mTaskAdapter);

    getEncounterDetails();
  }

  private void getEncounterDetails() {

    Log.d(TAG, "++getEncounterDetails()");
    String followingUserId = Utils.getFollowingUserId(getContext());
    WildlifeViewModel viewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    viewModel.getEncounterDetails(followingUserId, mEncounterId).observe(getViewLifecycleOwner(), encounterDetailsList -> {

      mEncounterDetailsList = new ArrayList<>(encounterDetailsList);
      HashMap<String, TaskEntity> taskMap = new HashMap<>();
      boolean showSensitive = Utils.getShowSensitive(getContext());
      for (EncounterDetails encounterDetails : encounterDetailsList) {
        if (!taskMap.containsKey(encounterDetails.TaskId)) {
          if (!encounterDetails.TaskIsSensitive || showSensitive) {
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.Id = encounterDetails.TaskId;
            taskEntity.Description = encounterDetails.TaskDescription;
            taskEntity.IsComplete = true;
            taskEntity.IsSensitive = encounterDetails.TaskIsSensitive;
            taskEntity.Name = encounterDetails.TaskName;
            taskMap.put(taskEntity.Id, taskEntity);
          }
        }

        Log.d(TAG, "Task list is " + taskMap.size());
        mTaskAdapter.setTaskEntityList(taskMap.values());
      }

      EncounterDetails encounterDetails = mEncounterDetailsList.get(0);
      int numberInGroup = mEncounterDetailsList.size() / taskMap.size();
      mEncounterEntity = new EncounterEntity();
      mEncounterEntity.WildlifeId = encounterDetails.WildlifeId;
      mEncounterEntity.Date = encounterDetails.Date;
      mEncounterEntity.UserId = encounterDetails.UserId;

      // update UI components
      if (!encounterDetails.ImageUrl.equals(Utils.UNKNOWN_STRING)) {
        Glide.with(this)
          .load(encounterDetails.ImageUrl)
          .placeholder(R.drawable.ic_placeholder_dark)
          .error(R.drawable.ic_error_dark)
          .into(mWildlifeImage);
        mImageAttributionText.setText(encounterDetails.ImageAttribution);
      } else {
        mWildlifeImage.setVisibility(View.GONE);
        mImageAttributionText.setVisibility(View.GONE);
      }

      mWildlifeText.setText(encounterDetails.WildlifeSpecies);
      mAbbreviationText.setText(encounterDetails.WildlifeAbbreviation);
      mDateText.setText(Utils.fromTimestamp(encounterDetails.Date));
      mNumberInGroupText.setText(
        String.format(
          Locale.US,
          getString(R.string.format_number_in_group),
          encounterDetails.WildlifeAbbreviation,
          numberInGroup));
    });
  }

  private static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    private final String TAG = Utils.BASE_TAG + TaskAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private final List<TaskEntity> mTaskEntityList;

    public TaskAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
      mTaskEntityList = new ArrayList<>();
    }

    @NonNull
    @Override
    public TaskAdapter.TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.task_list_item, parent, false);
      return new TaskAdapter.TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.TaskHolder holder, int position) {

      if (mTaskEntityList != null) {
        TaskEntity taskEntity = mTaskEntityList.get(position);
        holder.bind(taskEntity);
      } else {
        Log.w(TAG, "TaskEntityList is empty at this time.");
      }
    }

    @Override
    public int getItemCount() {

      return mTaskEntityList != null ? mTaskEntityList.size() : 0;
    }

    public void setTaskEntityList(Collection<TaskEntity> taskEntityCollection) {

      Log.d(TAG, "++setTaskEntityList(Collection<TaskEntity>, boolean)");
      int currentSize = mTaskEntityList.size();
      mTaskEntityList.clear();
      mTaskEntityList.addAll(taskEntityCollection);
      mTaskEntityList.sort(new Utils.SortByName());
      notifyItemRangeRemoved(0, currentSize);
      notifyItemRangeInserted(0, taskEntityCollection.size());
    }

    static class TaskHolder extends RecyclerView.ViewHolder {

      private final TextView mDescriptionTextView;
      private final ImageView mIsCompleteImage;
      private final TextView mTitleTextView;

      TaskHolder(View itemView) {
        super(itemView);

        mDescriptionTextView = itemView.findViewById(R.id.task_item_text_desc);
        mIsCompleteImage = itemView.findViewById(R.id.task_item_image);
        mTitleTextView = itemView.findViewById(R.id.task_item_text_name);
      }

      void bind(TaskEntity taskEntity) {

        mDescriptionTextView.setText(taskEntity.Description);
        if (taskEntity.IsComplete) {
          mIsCompleteImage.setImageResource(R.drawable.ic_complete_dark);
        } else {
          mIsCompleteImage.setImageResource(R.drawable.ic_incomplete_dark);
        }

        mTitleTextView.setText(taskEntity.Name);
      }
    }
  }
}
