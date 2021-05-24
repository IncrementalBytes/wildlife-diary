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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EncounterDataFragment  extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDataFragment.class.getSimpleName();

  public interface OnEncounterDataListener {

    void onEncounterDataFailure(String message);

    void onEncounterDataMissing();

    /*
      Instructs caller to update local db with passed data.
     */
    void onEncounterDataPopulate(List<EncounterEntity> encounterEntityList);

    void onEncounterDataSynced();
  }

  private OnEncounterDataListener mCallback;

  public static EncounterDataFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new EncounterDataFragment();
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).get().addOnCompleteListener(
      task -> {

        if (!task.isSuccessful()) {
          Log.d(TAG, "Checking data stamp for Encounters was unsuccessful.", task.getException());
          mCallback.onEncounterDataFailure("Unable to retrieve Wildlife data stamp.");
        } else {
          String remoteStamp = Utils.UNKNOWN_ID;
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
              String id = dataSnapshot.getKey();
              if (id != null && id.equals(Utils.ENCOUNTER_ROOT)) {
                Object valueObject = dataSnapshot.getValue();
                if (valueObject != null) {
                  remoteStamp = dataSnapshot.getValue().toString();
                  break;
                }
              }
            }

            String encounterStamp = Utils.getEncountersStamp(getActivity());
            if (encounterStamp.equals(Utils.UNKNOWN_ID) || remoteStamp.equals(Utils.UNKNOWN_ID) || !encounterStamp.equalsIgnoreCase(remoteStamp)) {
              Utils.setEncountersStamp(getActivity(), remoteStamp);
              populateEncounterTable();
            } else {
              Log.d(TAG, "Encounter data in-sync.");
              mCallback.onEncounterDataSynced();
            }
          } else {
            mCallback.onEncounterDataFailure("Encounter data stamp not found.");
          }
        }
      });
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

        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data", task.getException());
          mCallback.onEncounterDataFailure("Could not retrieve Encounter data.");
        } else {
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            if (resultSnapshot.getChildrenCount() > 0) {
              Log.d(TAG, "Attempting Encounter inserts: " + resultSnapshot.getChildrenCount());
              if (getActivity() != null) {
                List<EncounterEntity> encounterEntityList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : resultSnapshot.getChildren()) {
                  EncounterEntity encounterEntity = dataSnapshot.getValue(EncounterEntity.class);
                  String id = dataSnapshot.getKey();
                  if (encounterEntity != null && id != null) {
                    encounterEntity.Id = id;
                    if (encounterEntity.isValid()) {
                      encounterEntityList.add(encounterEntity);
                    } else {
                      Log.w(TAG, "Encounter entity was invalid, not adding: " + encounterEntity.Id);
                    }
                  }
                }

                mCallback.onEncounterDataPopulate(encounterEntityList);
              } else {
                mCallback.onEncounterDataFailure("App was not ready for operation at this time.");
              }
            } else {
              mCallback.onEncounterDataMissing();
            }
          } else {
            mCallback.onEncounterDataFailure("Encounter results not found.");
          }
        }
      });
  }
}
