/*
 * Copyright 2022 Ryan Ward
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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.AutoCompleteAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EncounterFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterFragment.class.getSimpleName();

  private Button mAdditionButton;
  private EditText mDateEdit;
  private EditText mGroupCountEdit;
  private Button mMinusButton;
  private AutoCompleteTextView mWildlifeText;

  private TaskAdapter mTaskAdapter;

  private String mEncounterId;
  private String mFollowingUserId;
  private int mGroupCount = 1;
  private boolean mShowSensitive;
  private HashMap<String, TaskEntity> mTaskEntityMap;
  private List<TaskEntity> mTaskEntitySortedList;
  private HashMap<String, String> mWildlifeMap;
  private WildlifeViewModel mWildlifeViewModel;

  private final TextWatcher mTextWatcher = new TextWatcher() {

    private String currentValue = "";
    private final Calendar calendar = Calendar.getInstance();

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

      if (!s.toString().equals(currentValue)) {
        String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
        String cleanC = currentValue.replaceAll("[^\\d.]|\\.", "");

        int sel = clean.length();
        for (int i = 2; i <= clean.length() && i < 6; i += 2) {
          sel++;
        }

        if (clean.equals(cleanC)) {
          sel--;
        }

        if (clean.length() < 8) {
          String dateFormat = "MMddYYYY";
          clean = clean + dateFormat.substring(clean.length());
        } else {
          int month = Integer.parseInt(clean.substring(0, 2));
          int day = Integer.parseInt(clean.substring(2, 4));
          int year = Integer.parseInt(clean.substring(4, 8));

          month = month < 1 ? 1 : Math.min(month, 12);
          calendar.set(Calendar.MONTH, month - 1);
          year = (year < 1900) ? 1900 : Math.min(year, 2100);
          calendar.set(Calendar.YEAR, year);
          day = Math.min(day, calendar.getActualMaximum(Calendar.DATE));
          clean = String.format(Locale.US, "%02d%02d%02d", month, day, year);
        }

        clean = String.format(
          "%s/%s/%s",
          clean.substring(0, 2),
          clean.substring(2, 4),
          clean.substring(4, 8));

        sel = Math.max(sel, 0);
        currentValue = clean;
        mDateEdit.setText(currentValue);
        mDateEdit.setSelection(Math.min(sel, currentValue.length()));
      }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
  };

  /*
    Fragment Override(s)
  */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    mFollowingUserId = Utils.getFollowingUserId(getContext());
    mShowSensitive = Utils.getShowSensitive(getContext());
    mWildlifeMap = new HashMap<>();
    mWildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_encounter, container, false);
    mAdditionButton = view.findViewById(R.id.encounter_button_addition);
    mDateEdit = view.findViewById(R.id.encounter_edit_date);
    mGroupCountEdit = view.findViewById(R.id.encounter_edit_number_in_group);
    mMinusButton = view.findViewById(R.id.encounter_button_minus);
    mWildlifeText = view.findViewById(R.id.encounter_auto_wildlife);
    Button addEncounterButton = view.findViewById(R.id.encounter_button_add);
    ImageView closeImage = view.findViewById(R.id.encounter_image_close);
    Button deleteEncounterButton = view.findViewById(R.id.encounter_button_delete);
    Button updateEncounterButton = view.findViewById(R.id.encounter_button_update);

    RecyclerView recyclerView = view.findViewById(R.id.encounter_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mTaskAdapter = new EncounterFragment.TaskAdapter(getContext());
    recyclerView.setAdapter(mTaskAdapter);

    closeImage.setOnClickListener(deleteImageView ->
      NavHostFragment.findNavController(this).navigate(R.id.action_encounterFragment_to_dataFragment));

    mDateEdit.addTextChangedListener(mTextWatcher);
    mGroupCountEdit.setText(String.valueOf(mGroupCount));
    mAdditionButton.setOnClickListener(additionButtonView -> {

      mGroupCountEdit.setText(String.valueOf(++mGroupCount));
      updateUI();
    });

    mMinusButton.setEnabled(false);
    mMinusButton.setOnClickListener(minusButtonView -> {

      mGroupCountEdit.setText(String.valueOf(--mGroupCount));
      updateUI();
    });

    mEncounterId = null;
    Bundle arguments = getArguments();
    if (arguments != null) {
      mEncounterId = arguments.getString(Utils.ARG_ENCOUNTER_ID);
    }

    if (mEncounterId == null || mEncounterId.isEmpty()) { // set for adding
      deleteEncounterButton.setVisibility(View.GONE);
      updateEncounterButton.setVisibility(View.GONE);
      addEncounterButton.setOnClickListener(addEncounterButtonView -> addEncounter());
    } else {
      addEncounterButton.setVisibility(View.GONE);
      deleteEncounterButton.setOnClickListener(deleteEncounterButtonView -> deleteEncounter());
      updateEncounterButton.setOnClickListener(updateEncounterButtonView -> updateEncounter());
    }

    // populate the task list
    mWildlifeViewModel.getTasks().observe(getViewLifecycleOwner(), taskEntityList -> {

      mTaskEntitySortedList = new ArrayList<>();
      mTaskEntitySortedList.addAll(taskEntityList);
      mTaskEntityMap = new HashMap<>();
      for (TaskEntity taskEntity : taskEntityList) {
        mTaskEntityMap.put(taskEntity.Id, taskEntity);
      }

      mTaskAdapter.setTaskEntityList(mTaskEntitySortedList);
      prepareWildlifeList();
      if (mEncounterId != null && !mEncounterId.isEmpty()) {
        setupForEditing();
      }
    });

    return view;
  }

  /*
    Private Method(s)
   */
  private void addEncounter() {

    Log.d(TAG, "++addEncounter()");
    HashMap<String, Object> encounterEntities = generateEncounterMap();
    if (encounterEntities.size() > 0) {
      FirebaseDatabase.getInstance().getReference(Utils.ENCOUNTER_ROOT).updateChildren(encounterEntities)
        .addOnSuccessListener(result -> {

          if (mEncounterId == null || mEncounterId.isEmpty()) { // reset view for additional encounters
            mWildlifeText.setText("");
            mGroupCount = 1;
            mGroupCountEdit.setText(String.valueOf(mGroupCount));
            mMinusButton.setEnabled(false);
            mAdditionButton.setEnabled(true);
            Toast.makeText(getContext(), "Encounter added!", Toast.LENGTH_SHORT).show();
            hideKeyboard();
            Utils.updateRemoteDataStamp(Utils.ENCOUNTER_ROOT);
          }

          mTaskAdapter.setTaskEntityList(mTaskEntitySortedList);
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error setting data Encounter data.", e);
          Toast.makeText(getContext(), "Adding encounter failed.", Toast.LENGTH_SHORT).show();
        });
    } else {
      Toast.makeText(getContext(), "No encounters were generated.", Toast.LENGTH_SHORT).show();
    }
  }

  private void deleteEncounter() {

    Log.d(TAG, "++deleteEncounter()");
    if (mEncounterId != null && !mEncounterId.isEmpty()) {
      mWildlifeViewModel.getEncounterDetails(mFollowingUserId, mEncounterId, mShowSensitive).observe(
        getViewLifecycleOwner(),
        encounterDetailsList -> {

          HashMap<String, Object> encounterEntities = new HashMap<>();
          for (EncounterDetails encounterDetails : encounterDetailsList) {
            Log.d(TAG, "Deleting " + encounterDetails.toString());
            encounterEntities.put(encounterDetails.Id, null);
          }

          FirebaseDatabase.getInstance().getReference(Utils.ENCOUNTER_ROOT).updateChildren(encounterEntities)
            .addOnCompleteListener(task -> {

              if (!task.isSuccessful()) {
                Log.e(TAG, "Unable to delete encounter(s).", task.getException());
              } else {
                Toast.makeText(getContext(), "Encounter(s) deleted.", Toast.LENGTH_SHORT).show();
                Utils.updateRemoteDataStamp(Utils.ENCOUNTER_ROOT);
                NavHostFragment.findNavController(this).navigate(R.id.action_encounterFragment_to_dataFragment);
              }
            });
        });
    } else {
      Toast.makeText(getContext(), "Encounter data invalid.", Toast.LENGTH_SHORT).show();
    }
  }

  private HashMap<String, Object> generateEncounterMap() {

    Log.d(TAG, "generateEncounterMap()");
    EncounterEntity encounterEntity = new EncounterEntity();
    encounterEntity.Date = Utils.toTimestamp(mDateEdit.getText().toString());
    encounterEntity.EncounterId = UUID.randomUUID().toString();
    encounterEntity.UserId = Utils.getUserId(getContext());

    // TODO: add injury call-out

    String selectedWildlifeAbbreviation = mWildlifeText.getText().toString().toUpperCase();
    if (mWildlifeMap.containsKey(selectedWildlifeAbbreviation)) {
      String wildlifeId = mWildlifeMap.get(selectedWildlifeAbbreviation);
      if (wildlifeId == null || wildlifeId.isEmpty()) {
        encounterEntity.WildlifeId = Utils.UNKNOWN_ID;
      } else {
        encounterEntity.WildlifeId = wildlifeId;
      }
    } else {
      encounterEntity.WildlifeId = Utils.UNKNOWN_ID;
    }

    int totalTaskItems = mTaskAdapter.getItemCount();
    if (totalTaskItems == 0) {
      Toast.makeText(getContext(), "No tasks were selected.", Toast.LENGTH_SHORT).show();
    }

    int groupTotal = Integer.parseInt(mGroupCountEdit.getText().toString());
    HashMap<String, Object> encounterEntityMap = new HashMap<>();
    for (int taskCount = 0; taskCount < totalTaskItems; taskCount++) {
      TaskEntity taskEntity = mTaskAdapter.getItem(taskCount);
      if (taskEntity.IsComplete) {
        for (int groupCount = 0; groupCount < groupTotal; groupCount++) {
          EncounterEntity newEntity = new EncounterEntity(encounterEntity);
          newEntity.Id = UUID.randomUUID().toString(); // unique entry per task
          newEntity.TaskId = taskEntity.Id;
          if (newEntity.isValid()) {
            Log.d(TAG, "Adding " + newEntity);
            encounterEntityMap.put(newEntity.Id, newEntity);
          } else {
            Toast.makeText(getContext(), "Missing some encounter data.", Toast.LENGTH_SHORT).show();
          }
        }

        taskEntity.IsComplete = false;
      }
    }

    return encounterEntityMap;
  }

  private void hideKeyboard() {

    Log.d(TAG, "++hideKeyboard()");
    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
    View view = requireActivity().getCurrentFocus();
    if (view == null) {
      view = new View(getContext());
    }

    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  private void prepareWildlifeList() {

    mWildlifeViewModel.getWildlife().observe(getViewLifecycleOwner(), wildlifeEntityList -> {

      for (WildlifeEntity wildlifeEntity : wildlifeEntityList) {
        mWildlifeMap.put(wildlifeEntity.Abbreviation.toUpperCase(), wildlifeEntity.Id);
      }

      AutoCompleteAdapter adapter = new AutoCompleteAdapter(
        getContext(),
        android.R.layout.simple_dropdown_item_1line,
        android.R.id.text1,
        new ArrayList<>(mWildlifeMap.keySet()));
      mWildlifeText.setAdapter(adapter);
    });
  }

  private void setupForEditing() {

    Log.d(TAG, "++setupForEditing()");
    mWildlifeViewModel.getEncounterDetails(mFollowingUserId, mEncounterId, mShowSensitive).observe(
      getViewLifecycleOwner(),
      encounterDetailsList -> {

        List<String> taskIds = new ArrayList<>();
        for (EncounterDetails encounterDetails : encounterDetailsList) {
          if (!taskIds.contains(encounterDetails.TaskId) && mTaskEntityMap.containsKey(encounterDetails.TaskId)) {
            TaskEntity taskEntity = mTaskEntityMap.get(encounterDetails.TaskId);
            if (taskEntity != null) {
              taskIds.add(encounterDetails.TaskId);
              taskEntity.IsComplete = true;
            }
          }
        }

        if (taskIds.size() > 0 && encounterDetailsList.size() > 0) {
          mGroupCount = encounterDetailsList.size() / taskIds.size();
          mDateEdit.setText(Utils.fromTimestamp(encounterDetailsList.get(0).Date));
          mWildlifeText.setText(encounterDetailsList.get(0).WildlifeAbbreviation);
        } else {
          mGroupCount = 1;
        }

        mGroupCountEdit.setText(String.valueOf(mGroupCount));
        mWildlifeText.setEnabled(false);
        mTaskAdapter.setTaskEntityList(mTaskEntitySortedList);
        updateUI();
      });
  }

  private void updateEncounter() {

    Log.d(TAG, "++updateEncounter()");
    if (mEncounterId != null && !mEncounterId.isEmpty() && !mEncounterId.equals(Utils.UNKNOWN_ID)) {
      mWildlifeViewModel.getEncounterDetails(mFollowingUserId, mEncounterId, mShowSensitive).observe(
        getViewLifecycleOwner(),
        encounterDetailsList -> {

          HashMap<String, Object> deleteEncountersMap = new HashMap<>();
          for (EncounterDetails encounterDetails : encounterDetailsList) {
            Log.d(TAG, "Deleting " + encounterDetails.toString());
            deleteEncountersMap.put(encounterDetails.Id, null);
          }

          FirebaseDatabase.getInstance().getReference(Utils.ENCOUNTER_ROOT).updateChildren(deleteEncountersMap)
            .addOnCompleteListener(task -> {

              if (!task.isSuccessful()) {
                Log.e(TAG, "Unable to delete encounter(s).", task.getException());
              } else {
                HashMap<String, Object> updatedEncountersMap = generateEncounterMap();
                if (updatedEncountersMap.size() > 0) {
                  FirebaseDatabase.getInstance().getReference(Utils.ENCOUNTER_ROOT).updateChildren(updatedEncountersMap)
                    .addOnSuccessListener(result -> {
                      Utils.updateRemoteDataStamp(Utils.ENCOUNTER_ROOT);
                      NavHostFragment.findNavController(this).navigate(R.id.action_encounterFragment_to_dataFragment);
                    })
                    .addOnFailureListener(e -> {
                      Log.e(TAG, "Error setting data Encounter data.", e);
                      Toast.makeText(getContext(), "Update failed.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                  Log.w(TAG, "No encounters were collected; look for earlier messages.");
                }
              }
            });
        });
    }
  }

  private void updateUI() {

    mAdditionButton.setEnabled(mGroupCount < 12);
    mMinusButton.setEnabled(mGroupCount > 1);
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

      View itemView = mInflater.inflate(R.layout.item_task, parent, false);
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

    public TaskEntity getItem(int position) {

      return mTaskEntityList.get(position);
    }

    public void setTaskEntityList(Collection<TaskEntity> taskEntityCollection) {

      Log.d(TAG, "++setTaskEntityList(Collection<TaskEntity>)");
      int currentSize = mTaskEntityList.size();
      mTaskEntityList.clear();
      mTaskEntityList.addAll(taskEntityCollection);
      notifyItemRangeRemoved(0, currentSize);
      notifyItemRangeInserted(0, taskEntityCollection.size());
    }

    static class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final Resources mResources;
      private final Resources.Theme mTheme;
      private final TextView mDescriptionTextView;
      private final ImageView mIsCompleteImage;
      private final CardView mSummaryCardView;
      private final TextView mTitleTextView;

      private TaskEntity mTaskEntity;

      TaskHolder(View itemView) {
        super(itemView);

        mIsCompleteImage = itemView.findViewById(R.id.task_item_image);
        mSummaryCardView = itemView.findViewById(R.id.task_item_card);
        mTitleTextView = itemView.findViewById(R.id.task_item_text_name);
        mDescriptionTextView = itemView.findViewById(R.id.task_item_text_desc);

        mResources = itemView.getResources();
        mTheme = itemView.getContext().getTheme();
        itemView.setOnClickListener(this);
      }

      void bind(TaskEntity taskEntity) {

        mTaskEntity = taskEntity;
        mDescriptionTextView.setText(mTaskEntity.Description);
        mTitleTextView.setText(mTaskEntity.Name);
        if (mTaskEntity.IsComplete) {
          mIsCompleteImage.setImageResource(R.drawable.ic_complete_dark);
          mSummaryCardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
              mResources,
              R.color.primaryDarkColor,
              mTheme));
        } else {
          mIsCompleteImage.setImageResource(R.drawable.ic_incomplete_dark);
          mSummaryCardView.setCardBackgroundColor(
            ResourcesCompat.getColor(
              mResources,
              R.color.secondaryDarkColor,
              mTheme));
        }
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
