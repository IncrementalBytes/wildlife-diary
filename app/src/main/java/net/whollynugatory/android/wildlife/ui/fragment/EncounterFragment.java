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
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.SpinnerItemState;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.AutoCompleteAdapter;
import net.whollynugatory.android.wildlife.ui.SpinnerItemAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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

  private EditText mDateEdit;
  private NumberPicker mNumberInGroupPicker;
  private Spinner mTaskSpinner;
  private AutoCompleteTextView mWildlifeText;

  private int mEncountersAdded;
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

    mDateEdit = view.findViewById(R.id.encounter_edit_date);
    mNumberInGroupPicker = view.findViewById(R.id.encounter_picker_number_in_group);
    mTaskSpinner = view.findViewById(R.id.encounter_spinner_task);
    mWildlifeText = view.findViewById(R.id.encounter_auto_wildlife);

    mDateEdit.addTextChangedListener(mTextWatcher);
    mNumberInGroupPicker.setMinValue(1);
    mNumberInGroupPicker.setMaxValue(12);
    mWildlifeViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {

      ArrayList<SpinnerItemState> itemStates = new ArrayList<>();
      itemStates.add(new SpinnerItemState("", ""));
      for (TaskEntity taskEntity : tasks) {
        SpinnerItemState itemState = new SpinnerItemState();
        itemState.setSelected(false);
        itemState.setId(taskEntity.Id);
        itemState.setTitle(taskEntity.Name);
        itemStates.add(itemState);
      }

      itemStates.sort(new Utils.SortByName());
      SpinnerItemAdapter customAdapter = new SpinnerItemAdapter(getActivity(), 0, itemStates);
      mTaskSpinner.setAdapter(customAdapter);
      prepareWildlifeList();
    });

    mEncountersAdded = 0;
    ImageButton closeButton = view.findViewById(R.id.encounter_button_close);
    closeButton.setOnClickListener(v -> {

      if (mEncountersAdded > 0) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Utils.ENCOUNTER_ROOT, UUID.randomUUID().toString());
        FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).updateChildren(childUpdates)
          .addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
              Log.w(TAG, "Unable to update remote data stamp for changes.", task.getException());
            }
          });
      }

      mCallback.onEncounterClosed();
    });


    Button addButton = view.findViewById(R.id.encounter_button_add);
    addButton.setOnClickListener(v -> {

      EncounterEntity encounterEntity = new EncounterEntity();
      encounterEntity.Date = Utils.toTimestamp(mDateEdit.getText().toString());
      encounterEntity.EncounterId = UUID.randomUUID().toString();
      encounterEntity.NumberInGroup = mNumberInGroupPicker.getValue();
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

      int totalItems = mTaskSpinner.getAdapter().getCount();
      for (int taskCount = 0; taskCount < totalItems; taskCount++) {
        SpinnerItemState item = (SpinnerItemState) mTaskSpinner.getAdapter().getItem(taskCount);
        if (item.isSelected()) {
          encounterEntity.Id = UUID.randomUUID().toString(); // unique entry per task
          encounterEntity.TaskId = item.getId();
          if (encounterEntity.isValid()) {
            FirebaseDatabase.getInstance().getReference().child(Utils.ENCOUNTER_ROOT).child(encounterEntity.Id).setValue(encounterEntity)
              .addOnCompleteListener(task -> {

                if (!task.isSuccessful()) {
                  Log.e(TAG, "Error setting data: " + encounterEntity.toString(), task.getException());
                  mCallback.onEncounterFailure("Failed to added encounter.");
                } else {
                  mCallback.onEncounterAdded();
                  mWildlifeText.setText("");
                  mNumberInGroupPicker.setValue(1);
                  mEncountersAdded++;
                }
              });
          } else {
            mCallback.onEncounterFailure("Encounter data was unknown: " + encounterEntity.toString());
          }

          item.setSelected(false);
        }
      }
    });

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

  /*
    Private Method(s)
   */
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
}
