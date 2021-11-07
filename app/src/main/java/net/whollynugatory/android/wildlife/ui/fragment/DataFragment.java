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

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.WildlifeDatabase;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;

import java.util.Locale;

public class DataFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + DataFragment.class.getSimpleName();

  private TextView mStatusText;

  /*
    Fragment Override(s)
  */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    View view = inflater.inflate(R.layout.fragment_data, container, false);
    ImageView pawImage = view.findViewById(R.id.data_image_paw);
    pawImage.setBackgroundResource(R.drawable.anim_paw_dark);
    AnimationDrawable pawAnimation = (AnimationDrawable) pawImage.getBackground();

    pawAnimation.start();
    executeDataProcessing(Utils.TASK_ROOT);
    return view;
  }

  private void executeDataProcessing(String dataToSync) {

    Log.d(TAG, "executeDataProcessing()");
    FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).get().addOnCompleteListener(
      task -> {

        if (!task.isSuccessful()) {
          Log.d(TAG, "Retrieving data stamps was unsuccessful.", task.getException());
          // TODO: alert to user, but not using snackbar
        } else {
          updateUI("Grabbing remote data stamps...");
          String remoteDataStamp = Utils.UNKNOWN_ID;
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
              String id = dataSnapshot.getKey();
              if (id != null && id.equals(dataToSync)) {
                Object valueObject = dataSnapshot.getValue();
                if (valueObject != null) {
                  remoteDataStamp = dataSnapshot.getValue().toString();
                  break;
                }
              }
            }

            updateUI("Comparing local data with " + dataToSync);
            String localDataStamp = Utils.UNKNOWN_ID;
            switch (dataToSync) {
              case Utils.ENCOUNTER_ROOT:
                localDataStamp = Utils.getLocalTimeStamp(getActivity(), R.string.pref_key_stamp_encounters);
                break;
              case Utils.TASK_ROOT:
                localDataStamp = Utils.getLocalTimeStamp(getActivity(), R.string.pref_key_stamp_tasks);
                break;
              case Utils.WILDLIFE_ROOT:
                localDataStamp = Utils.getLocalTimeStamp(getActivity(), R.string.pref_key_stamp_wildlife);
                break;
            }

            if (remoteDataStamp.equals(Utils.UNKNOWN_ID)) {
              Log.w(TAG, "Remote dataStamp was unexpected: " + remoteDataStamp);
              onDataMissing();
            } else if (localDataStamp.equals(Utils.UNKNOWN_ID) || !localDataStamp.equalsIgnoreCase(remoteDataStamp)) {
              populateTable(dataToSync, remoteDataStamp);
            } else if (localDataStamp.equals(remoteDataStamp)) {
              Log.d(TAG, "Local data in-sync with " + dataToSync);
              updateUI("Local data matches " + dataToSync);
              switch (dataToSync) {
                case Utils.ENCOUNTER_ROOT:
                  NavHostFragment.findNavController(this).navigate(R.id.action_DataFragment_to_RecentFragment);
                  break;
                case Utils.TASK_ROOT:
                  executeDataProcessing(Utils.WILDLIFE_ROOT);
                  break;
                case Utils.WILDLIFE_ROOT:
                  executeDataProcessing(Utils.ENCOUNTER_ROOT);
                  break;
              }
            } else {
              Log.w(TAG, "Unexpected results between local and remote data stamps.");
              // TODO: alert to user, but not using snackbar
            }
          }
        }
      });
  }

  private void onDataMissing() {

    Log.d(TAG, "++onDataMissing()");
    // TODO: alert to user, but not using snackbar
    NavHostFragment.findNavController(this).navigate(R.id.action_DataFragment_to_RecentFragment);
  }

  private void populateTable(String dataRoot, String remoteDataStamp) {

    Log.d(TAG, "++populateTable(String, String)");
    FirebaseDatabase.getInstance().getReference().child(dataRoot).get()
      .addOnCompleteListener(task -> {

        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data", task.getException());
          // TODO: alert to user, but not using snackbar
        } else {
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            if (resultSnapshot.getChildrenCount() > 0) {
              Log.d(TAG, "Attempting inserts: " + resultSnapshot.getChildrenCount());
              if (getActivity() != null) {
                for (DataSnapshot dataSnapshot : resultSnapshot.getChildren()) {
                  String id = dataSnapshot.getKey();
                  switch (dataRoot) {
                    case Utils.ENCOUNTER_ROOT:
                      EncounterEntity encounterEntity = dataSnapshot.getValue(EncounterEntity.class);
                      if (encounterEntity != null && id != null) {
                        encounterEntity.Id = id;
                        if (encounterEntity.isValid()) {
                          WildlifeDatabase.databaseWriteExecutor.execute(() ->
                            WildlifeDatabase.getInstance(getContext()).encounterDao().insert(encounterEntity));
                        } else {
                          Log.w(TAG, "Encounter entity was invalid, not adding: " + encounterEntity.toString());
                        }
                      }

                      break;
                    case Utils.TASK_ROOT:
                      TaskEntity taskEntity = dataSnapshot.getValue(TaskEntity.class);
                      if (taskEntity != null && id != null) {
                        taskEntity.Id = id;
                        if (taskEntity.isValid()) {
                          WildlifeDatabase.databaseWriteExecutor.execute(() ->
                            WildlifeDatabase.getInstance(getContext()).taskDao().insert(taskEntity));
                        } else {
                          Log.w(TAG, "Task entity was invalid, not adding: " + taskEntity.toString());
                        }
                      }

                      break;
                    case Utils.WILDLIFE_ROOT:
                      WildlifeEntity wildlifeEntity = dataSnapshot.getValue(WildlifeEntity.class);
                      if (wildlifeEntity != null && id != null) {
                        wildlifeEntity.Id = id;
                        if (wildlifeEntity.isValid()) {
                          WildlifeDatabase.databaseWriteExecutor.execute(() ->
                            WildlifeDatabase.getInstance(getContext()).wildlifeDao().insert(wildlifeEntity));
                        } else {
                          Log.w(TAG, "Wildlife entity was invalid, not adding: " + wildlifeEntity.toString());
                        }
                      }

                      break;
                  }
                }

                switch (dataRoot) {
                  case Utils.ENCOUNTER_ROOT:
                    Utils.setLocalTimeStamp(getActivity(), R.string.pref_key_stamp_encounters, remoteDataStamp);
                    NavHostFragment.findNavController(this).navigate(R.id.action_DataFragment_to_RecentFragment);
                    break;
                  case Utils.TASK_ROOT:
                    Utils.setLocalTimeStamp(getActivity(), R.string.pref_key_stamp_tasks, remoteDataStamp);
                    executeDataProcessing(Utils.WILDLIFE_ROOT);
                    break;
                  case Utils.WILDLIFE_ROOT:
                    Utils.setLocalTimeStamp(getActivity(), R.string.pref_key_stamp_wildlife, remoteDataStamp);
                    executeDataProcessing(Utils.ENCOUNTER_ROOT);
                    break;
                }
              } else {
                // TODO: alert to user, but not using snackbar
              }
            } else {
              onDataMissing();
            }
          } else {
            // TODO: alert to user, but not using snackbar
          }
        }
      });
  }

  private void updateUI(String message) {

    if (mStatusText != null) {
      mStatusText.setText(message);
    }
  }
}
