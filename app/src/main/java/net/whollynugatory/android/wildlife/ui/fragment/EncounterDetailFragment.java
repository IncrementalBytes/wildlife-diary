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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EncounterDetailFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDetailFragment.class.getSimpleName();

  public interface OnEncounterDetailsListener {

    void onEncounterDeleted();
    void onEncounterUpdated();
    void onEncounterUpdateFailed();
  }

  private OnEncounterDetailsListener mCallback;

  private TextView mAbbreviationTextView;
  private TextView mDateTextView;
  private TextView mDbgIdTextView;
  private TextView mNumberInGroupTextView;
  private TextView mWildlifeTextView;

  private List<EncounterDetails> mEncounterDetailsList;
  private EncounterEntity mEncounterEntity;
  private String mEncounterId;
  private TaskAdapter mTaskAdapter;
  private HashMap<String, TaskEntity> mTaskEntityMap;

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

    try {
      mCallback = (OnEncounterDetailsListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(
          Locale.US,
          "Missing interface implementations for %s",
          context.toString()));
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
    mAbbreviationTextView = view.findViewById(R.id.encounter_details_text_abbreviation);
    mDateTextView = view.findViewById(R.id.encounter_details_text_date);
    mNumberInGroupTextView = view.findViewById(R.id.encounter_details_text_number_in_group);
    mWildlifeTextView = view.findViewById(R.id.encounter_details_text_wildlife);

    mTaskEntityMap = new HashMap<>();

    RecyclerView recyclerView = view.findViewById(R.id.encounter_details_recycler_tasks);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mDbgIdTextView = view.findViewById(R.id.encounter_details_text_dbg_id);
    Button updateButton = view.findViewById(R.id.encounter_details_button_update);
    updateButton.setOnClickListener(updateButtonView -> {

      Log.d(TAG, "Deleting encounterId: " + mEncounterId);
      for (EncounterDetails encounterDetails : mEncounterDetailsList) {
        String path = Utils.combine(Utils.ENCOUNTER_ROOT, encounterDetails.Id);
        FirebaseDatabase.getInstance().getReference().child(path).removeValue().addOnCompleteListener(task -> {

          if (!task.isSuccessful()) {
            Log.e(TAG, "Error removing data: " + encounterDetails.toString(), task.getException());
          } else {
            Log.d(TAG, "Successfully removed: " + encounterDetails.toString());
          }
        });
      }

      int totalItems = mTaskAdapter.getItemCount();
      mEncounterEntity.EncounterId = UUID.randomUUID().toString();
      for (int taskCount = 0; taskCount < totalItems; taskCount++) {
        TaskEntity taskEntity = mTaskAdapter.getItem(taskCount);
        if (taskEntity.IsComplete) {
          mEncounterEntity.Id = UUID.randomUUID().toString(); // unique entry per task
          mEncounterEntity.TaskId = taskEntity.Id;
          if (mEncounterEntity.isValid()) {
            String path = Utils.combine(Utils.ENCOUNTER_ROOT, mEncounterEntity.Id);
            FirebaseDatabase.getInstance().getReference().child(path).setValue(mEncounterEntity)
              .addOnCompleteListener(task -> {

                if (!task.isSuccessful()) {
                  Log.e(TAG, "Error setting data: " + mEncounterEntity.toString(), task.getException());
                } else {
                  Log.d(TAG, "Successfully added: " + mEncounterEntity.toString());
                }
              });
          } else {
            Log.e(TAG, "Encounter data was unknown: " + mEncounterEntity.toString());
          }
        }
      }

      Map<String, Object> childUpdates = new HashMap<>();
      childUpdates.put(Utils.ENCOUNTER_ROOT, UUID.randomUUID().toString());
      FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).updateChildren(childUpdates)
        .addOnCompleteListener(task -> {

          if (task.isSuccessful()) {
            mCallback.onEncounterUpdated();
          } else {
            Log.w(TAG, "Unable to update remote data stamp for changes.", task.getException());
            mCallback.onEncounterUpdateFailed();
          }
        });
    });

    Button deleteButton = view.findViewById(R.id.encounter_details_button_delete);
    deleteButton.setOnClickListener(deleteButtonView -> {

      Log.d(TAG, "Deleting encounterId: " + mEncounterId);
      for (EncounterDetails encounterDetails : mEncounterDetailsList) {
        String path = Utils.combine(Utils.ENCOUNTER_ROOT, encounterDetails.Id);
        FirebaseDatabase.getInstance().getReference().child(path).removeValue().addOnCompleteListener(task -> {

          if (!task.isSuccessful()) {
            Log.e(TAG, "Error removing data: " + encounterDetails.toString(), task.getException());
          } else {
            Log.d(TAG, "Successfully removed: " + encounterDetails.toString());
          }
        });
      }

      Map<String, Object> childUpdates = new HashMap<>();
      childUpdates.put(Utils.ENCOUNTER_ROOT, UUID.randomUUID().toString());
      FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).updateChildren(childUpdates)
        .addOnCompleteListener(task -> {

          if (task.isSuccessful()) {
            mCallback.onEncounterDeleted();
          } else {
            Log.w(TAG, "Unable to delete remote data stamp for changes.", task.getException());
            mCallback.onEncounterUpdateFailed();
          }
        });
    });

    mTaskAdapter = new TaskAdapter(getContext());
    recyclerView.setAdapter(mTaskAdapter);

    if (Utils.getIsContributor(getContext())) {
      WildlifeViewModel viewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
      viewModel.getTasks().observe(getViewLifecycleOwner(), taskEntities -> {
        for (TaskEntity taskEntity : taskEntities) {
          mTaskEntityMap.put(taskEntity.Id, taskEntity);
        }

        getEncounterDetails();
      });
    } else {
      updateButton.setVisibility(View.GONE);
      deleteButton.setVisibility(View.GONE);
      mDbgIdTextView.setVisibility(View.GONE);
      getEncounterDetails();
    }
  }

  private void getEncounterDetails() {

    Log.d(TAG, "++getEncounterDetails()");
    String followingUserId = Utils.getFollowingUserId(getContext());
    WildlifeViewModel viewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    viewModel.getEncounterDetails(followingUserId, mEncounterId).observe(getViewLifecycleOwner(), encounterDetailsList -> {

      mEncounterDetailsList = new ArrayList<>(encounterDetailsList);
      List<TaskEntity> completedTaskList = new ArrayList<>();
      if (Utils.getIsContributor(getContext())) {
        for (EncounterDetails encounterDetails : encounterDetailsList) {
          if (mTaskEntityMap.containsKey(encounterDetails.TaskId)) {
            Objects.requireNonNull(mTaskEntityMap.get(encounterDetails.TaskId)).IsComplete = true;
          }
        }

        Log.d(TAG, "Task list is " + mTaskEntityMap.values().size());
        mTaskAdapter.setTaskEntityList(mTaskEntityMap.values());
      } else {
        boolean showSensitive = Utils.getShowSensitive(getContext());
        for (EncounterDetails encounterDetails : encounterDetailsList) {
          if (!encounterDetails.TaskIsSensitive || showSensitive) {
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.Description = encounterDetails.TaskDescription;
            taskEntity.IsComplete = true;
            taskEntity.IsSensitive = encounterDetails.TaskIsSensitive;
            taskEntity.Name = encounterDetails.TaskName;
            completedTaskList.add(taskEntity);
          }
        }

        Log.d(TAG, "Task list is " + completedTaskList.size());
        mTaskAdapter.setTaskEntityList(completedTaskList, false);
      }

      mEncounterEntity = new EncounterEntity();
      mEncounterEntity.WildlifeId = mEncounterDetailsList.get(0).WildlifeId;
      mEncounterEntity.NumberInGroup = mEncounterDetailsList.get(0).NumberInGroup;
      mEncounterEntity.Date = mEncounterDetailsList.get(0).Date;
      mEncounterEntity.UserId = mEncounterDetailsList.get(0).UserId;

      mWildlifeTextView.setText(mEncounterDetailsList.get(0).WildlifeSpecies);
      mAbbreviationTextView.setText(mEncounterDetailsList.get(0).WildlifeAbbreviation);
      mDateTextView.setText(Utils.fromTimestamp(mEncounterDetailsList.get(0).Date));
      mDbgIdTextView.setText(mEncounterDetailsList.get(0).EncounterId);
      mNumberInGroupTextView.setText(
        String.format(
          Locale.US,
          getString(R.string.format_number_in_group),
          mEncounterDetailsList.get(0).WildlifeAbbreviation,
          mEncounterDetailsList.get(0).NumberInGroup));
    });
  }

  private static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    private final String TAG = Utils.BASE_TAG + TaskAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private boolean mIsContributor;
    private List<TaskEntity> mTaskEntityList;

    public TaskAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TaskAdapter.TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.task_list_item, parent, false);
      return new TaskAdapter.TaskHolder(itemView, mIsContributor);
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

    public TaskEntity getItem(int position) {

      return mTaskEntityList.get(position);
    }

    public void setTaskEntityList(Collection<TaskEntity> taskEntityCollection) {

      Log.d(TAG, "++setTaskEntityList(Collection<TaskEntity>)");
      setTaskEntityList(taskEntityCollection, true);
    }

    public void setTaskEntityList(Collection<TaskEntity> taskEntityCollection, boolean isContributor) {

      Log.d(TAG, "++setTaskEntityList(Collection<TaskEntity>, boolean)");
      mIsContributor = isContributor;
      mTaskEntityList = new ArrayList<>(taskEntityCollection);
      mTaskEntityList.sort(new Utils.SortByName());
      notifyDataSetChanged();
    }

    static class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final TextView mDescriptionTextView;
      private final ImageView mIsCompleteImage;
      private final CardView mSummaryCardView;
      private final TextView mTitleTextView;

      private TaskEntity mTaskEntity;

      TaskHolder(View itemView, boolean isContributor) {
        super(itemView);

        mDescriptionTextView = itemView.findViewById(R.id.task_item_text_desc);
        mIsCompleteImage = itemView.findViewById(R.id.task_item_image);
        mSummaryCardView = itemView.findViewById(R.id.task_item_card);
        mTitleTextView = itemView.findViewById(R.id.task_item_text_name);

        if (isContributor) {
          itemView.setOnClickListener(this);
        }
      }

      void bind(TaskEntity taskEntity) {

        mTaskEntity = taskEntity;
        mDescriptionTextView.setText(taskEntity.Description);
        if (taskEntity.IsComplete) {
          mIsCompleteImage.setImageResource(R.drawable.ic_complete_dark);
        } else {
          mIsCompleteImage.setImageResource(R.drawable.ic_incomplete_dark);
        }

        mTitleTextView.setText(taskEntity.Name);
      }

      @Override
      public void onClick(View view) {

        mTaskEntity.IsComplete = !mTaskEntity.IsComplete;
        if (mTaskEntity.IsComplete) {
          mIsCompleteImage.setImageResource(R.drawable.ic_complete_dark);
          mSummaryCardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
              view.getResources(),
              R.color.primaryDarkColor,
              view.getContext().getTheme()));
        } else {
          mIsCompleteImage.setImageResource(R.drawable.ic_incomplete_dark);
          mSummaryCardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
              view.getResources(),
              R.color.secondaryDarkColor,
              view.getContext().getTheme()));
        }
      }
    }
  }
}
