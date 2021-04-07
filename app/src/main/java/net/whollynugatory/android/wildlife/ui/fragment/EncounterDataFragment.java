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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.Locale;

public class EncounterDataFragment  extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDataFragment.class.getSimpleName();

  public interface OnEncounterDataListener {

    void onEncounterDataPopulated(int itemsPopulated);
  }

  private OnEncounterDataListener mCallback;

  public static EncounterDataFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new EncounterDataFragment();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    wildlifeViewModel.encounterCount().observe(getViewLifecycleOwner(), encounterCount -> populateEncounterTable());
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnEncounterDataListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    return inflater.inflate(R.layout.fragment_encounter_data, container, false);
  }

  private void populateEncounterTable() {

    Log.d(TAG, "++populateEncounterTable()");
    FirebaseDatabase.getInstance().getReference().child(Utils.ENCOUNTER_ROOT).get()
      .addOnCompleteListener(task -> {

        int itemsPopulated = 0;
        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data", task.getException());
        } else {
          if (task.getResult().getChildrenCount() > 0) {
            WildlifeViewModel wildlifeViewModel = new ViewModelProvider(getActivity()).get(WildlifeViewModel.class);
            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
              EncounterEntity encounterEntity = dataSnapshot.getValue(EncounterEntity.class);
              encounterEntity.Id = dataSnapshot.getKey();
              wildlifeViewModel.insertEncounter(encounterEntity);
              itemsPopulated++;
            }
          }
        }

        mCallback.onEncounterDataPopulated(itemsPopulated);
      });
  }
}
