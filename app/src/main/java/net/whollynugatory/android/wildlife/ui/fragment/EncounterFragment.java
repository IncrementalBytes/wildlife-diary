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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
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
import java.util.Map;
import java.util.UUID;

public class EncounterFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterFragment.class.getSimpleName();

  public interface OnEncounterListener {

    void onEncounterAdded();
    void onEncounterClosed();
    void onEncounterFailure(String message);
  }

  private OnEncounterListener mCallback;

  private Button mAdditionButton;
  private EditText mDateEdit;
  private EditText mGroupCountEdit;
  private Button mMinusButton;
  private Button mRecordEncountersButton;
  private AutoCompleteTextView mWildlifeText;

  private TaskAdapter mTaskAdapter;

  private int mEncountersAdded;
  private int mGroupCount = 1;
  private List<TaskEntity> mTaskEntityList;
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

  public static EncounterFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    EncounterFragment fragment = new EncounterFragment();
    Bundle arguments = new Bundle();
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
      mCallback = (OnEncounterListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    mWildlifeMap = new HashMap<>();
    mWildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_encounter, container, false);

    mEncountersAdded = 0;

    mAdditionButton = view.findViewById(R.id.encounter_button_addition);
    mDateEdit = view.findViewById(R.id.encounter_edit_date);
    mGroupCountEdit = view.findViewById(R.id.encounter_edit_number_in_group);
    mMinusButton = view.findViewById(R.id.encounter_button_minus);
    mWildlifeText = view.findViewById(R.id.encounter_auto_wildlife);

    RecyclerView recyclerView = view.findViewById(R.id.encounter_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mTaskAdapter = new EncounterFragment.TaskAdapter(getContext());
    recyclerView.setAdapter(mTaskAdapter);

    mDateEdit.addTextChangedListener(mTextWatcher);
    mGroupCountEdit.setText(String.valueOf(mGroupCount));

    mAdditionButton.setOnClickListener(view12 -> {

      mGroupCountEdit.setText(String.valueOf(++mGroupCount));
      updateUI();
    });

    mMinusButton.setEnabled(false);
    mMinusButton.setOnClickListener(view1 -> {

      mGroupCountEdit.setText(String.valueOf(--mGroupCount));
      updateUI();
    });

    mRecordEncountersButton = view.findViewById(R.id.encounter_button_record);
    mRecordEncountersButton.setEnabled(false);
    mRecordEncountersButton.setOnClickListener(v -> recordEncounters());

    Button addButton = view.findViewById(R.id.encounter_button_add);
    addButton.setOnClickListener(v -> {

      addEncounter();
      mTaskAdapter.setTaskEntityList(mTaskEntityList);
    });

    mWildlifeViewModel.getTasks().observe(getViewLifecycleOwner(), taskEntityList -> {

      // TODO: order list by number of times task has been used
      mTaskEntityList = new ArrayList<>(taskEntityList);
      mTaskAdapter.setTaskEntityList(mTaskEntityList);
      prepareWildlifeList();
    });

    return view;
  }

  /*
    Private Method(s)
   */
  private void addEncounter() {

    Log.d(TAG, "++addEncounter()");
    EncounterEntity encounterEntity = new EncounterEntity();
    encounterEntity.Date = Utils.toTimestamp(mDateEdit.getText().toString());
    encounterEntity.EncounterId = UUID.randomUUID().toString();
    encounterEntity.NumberInGroup = Integer.parseInt(mGroupCountEdit.getText().toString());
    encounterEntity.UserId = Utils.DEFAULT_FOLLOWING_USER_ID;

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

    int totalItems = mTaskAdapter.getItemCount();
    for (int taskCount = 0; taskCount < totalItems; taskCount++) {
      TaskEntity taskEntity = mTaskAdapter.getItem(taskCount);
      if (taskEntity.IsComplete) {
        encounterEntity.Id = UUID.randomUUID().toString(); // unique entry per task
        encounterEntity.TaskId = taskEntity.Id;
        if (encounterEntity.isValid()) {
          FirebaseDatabase.getInstance().getReference().child(Utils.ENCOUNTER_ROOT).child(encounterEntity.Id).setValue(encounterEntity)
            .addOnCompleteListener(task -> {

              if (!task.isSuccessful()) {
                Log.e(TAG, "Error setting data: " + encounterEntity.toString(), task.getException());
                mCallback.onEncounterFailure("Failed to added encounter.");
              } else {
                mWildlifeText.setText("");
                mGroupCountEdit.setText(String.valueOf(1));
                mMinusButton.setEnabled(false);
                mAdditionButton.setEnabled(true);
                mEncountersAdded++;
                mRecordEncountersButton.setEnabled(mEncountersAdded > 0);
                mCallback.onEncounterAdded();
              }
            });
        } else {
          mCallback.onEncounterFailure("Encounter data was unknown: " + encounterEntity.toString());
        }

        taskEntity.IsComplete = false;
      }
    }
  }

  private void prepareWildlifeList() {

    mWildlifeViewModel.getWildlife().observe(getViewLifecycleOwner(), wildlifeEntityList -> {

      for (WildlifeEntity wildlifeEntity : wildlifeEntityList) {
        mWildlifeMap.put(wildlifeEntity.Abbreviation.toUpperCase(), wildlifeEntity.Id);
      }

      AutoCompleteAdapter adapter = new AutoCompleteAdapter(
        getActivity(),
        android.R.layout.simple_dropdown_item_1line,
        android.R.id.text1,
        new ArrayList<>(mWildlifeMap.keySet()));
      mWildlifeText.setAdapter(adapter);
    });
  }

  private void recordEncounters() {

    Log.d(TAG, "++recordEncounters()");
    if (mEncountersAdded > 0) {
      Map<String, Object> childUpdates = new HashMap<>();
      childUpdates.put(Utils.ENCOUNTER_ROOT, UUID.randomUUID().toString());
      FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).updateChildren(childUpdates)
        .addOnCompleteListener(task -> {

          if (!task.isSuccessful()) {
            Log.w(TAG, "Unable to update remote data stamp for changes.", task.getException());
          }
        });
    } else {
      Log.w(TAG, "No encounters were recorded.");
    }

    mCallback.onEncounterClosed();
  }

  private void updateUI() {

    mAdditionButton.setEnabled(mGroupCount < 12);
    mMinusButton.setEnabled(mGroupCount > 1);
  }

  private static class TaskAdapter extends RecyclerView.Adapter<EncounterFragment.TaskAdapter.TaskHolder> {

    private final String TAG = Utils.BASE_TAG + EncounterFragment.TaskAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private List<TaskEntity> mTaskEntityList;

    public TaskAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EncounterFragment.TaskAdapter.TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.task_list_item, parent, false);
      return new EncounterFragment.TaskAdapter.TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EncounterFragment.TaskAdapter.TaskHolder holder, int position) {

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
      mTaskEntityList = new ArrayList<>(taskEntityCollection);
      notifyDataSetChanged();
    }

    static class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final TextView mDescriptionTextView;
      private final TextView mTitleTextView;
      private final ImageView mCompleteImage;

      private TaskEntity mTaskEntity;

      TaskHolder(View itemView) {
        super(itemView);

        mCompleteImage = itemView.findViewById(R.id.task_item_image);
        mTitleTextView = itemView.findViewById(R.id.task_item_text_name);
        mDescriptionTextView = itemView.findViewById(R.id.task_item_text_desc);

        itemView.setOnClickListener(this);
      }

      void bind(TaskEntity taskEntity) {

        mTaskEntity = taskEntity;
        mDescriptionTextView.setText(mTaskEntity.Description);
        mTitleTextView.setText(mTaskEntity.Name);
        if (mTaskEntity.IsComplete) {
          mCompleteImage.setImageResource(R.drawable.ic_complete_dark);
        } else {
          mCompleteImage.setImageResource(R.drawable.ic_incomplete_dark);
        }
      }

      @Override
      public void onClick(View view) {

        mTaskEntity.IsComplete = !mTaskEntity.IsComplete;
        if (mTaskEntity.IsComplete) {
          mCompleteImage.setImageResource(R.drawable.ic_complete_dark);
        } else {
          mCompleteImage.setImageResource(R.drawable.ic_incomplete_dark);
        }
      }
    }
  }
}
